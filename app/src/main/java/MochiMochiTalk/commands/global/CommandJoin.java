package MochiMochiTalk.commands.global;

import MochiMochiTalk.lib.global.InvokeMethod;
import MochiMochiTalk.lib.global.types.ResponseSchema;
import MochiMochiTalk.util.ConcurrencyUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class CommandJoin extends ListenerAdapter {

  private static final ExecutorService interactionExecutor = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("bot interaction thread"));

  private static final ExecutorService internalProcessingExecutor = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("bot internal processing thread"));

  private static final ExecutorService DatabaseApiHandshakeExecutor = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("bot database api handshake thread"));

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private OkHttpClient apiClient;

  /**
   * このメソッドは、ユーザーが「/global」スラッシュ コマンドを呼び出したときに呼び出されます
   * <p>
   * コマンドがギルド内で呼び出されたかどうかをチェックし、DB API からの応答を非同期で受け取りながら早期応答を送信します。
   * <p>
   * コマンドがギルドで呼び出された場合、このメソッドは、将来のアップデートでダイレクト メッセージへの参加が計画されているというメッセージを送信します。
   * <p>
   * ギルドがすでにグローバル チャットに参加している場合は、それに応じてメッセージが送信されます。
   * <p>
   * ギルドにまだ参加していない場合は、「参加」と「拒否」の 2 つのボタンを含むメッセージが送信され、ユーザーは参加するかどうかを選択できます。
   *
   * @param event コマンドの相互作用に関する詳細を含む SlashCommandInteractionEvent オブジェクト
   */
  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    // early return if the command is not "join"
    if (!event.getName().equals("global")) {
      return;
    }

    // send early reply for asynchronously receive the response from the DB API.
    CompletableFuture<InteractionHook> earlyReply = event.reply("グローバルチャット レイドデータベースに接続しています…")
        .setEphemeral(true).submit();

    // check whether the command is invoked in the guild
    if (event.isFromGuild()) {
      earlyReply.thenComposeAsync(
              hook -> hook.editOriginal("ダイレクトメッセージからの操作は将来のアップデートで実装予定です。").submit(),
              interactionExecutor).thenAcceptAsync((suc) -> log.info(
              "The command [global] command was invoked in the Direct Message Channel by [{}]",
              event.getUser()), internalProcessingExecutor)
          .whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor);
      return;
    }

    UriConstructor searchGuildUriConstructor = new UriConstructor(InvokeMethod.SEARCH_GUILD,
        Objects.requireNonNull(event.getGuild()).getId(), event.getChannel().getId());

    apiClient =
        apiClient == null ? new OkHttpClient() : apiClient; // null check and lazy initialization

    Request request = new Request.Builder().url(searchGuildUriConstructor.construct()).get()
        .build();

    // do process below if subcommand name is "join"

    invokeJoinSubCommand(event, earlyReply, request);

    // do process below if subcommand name is "remove"

    invokeRemoveSubCommand(event, earlyReply, request);
  }

  private void invokeJoinSubCommand(@NotNull SlashCommandInteractionEvent event,
      CompletableFuture<InteractionHook> earlyReply, Request request) {

    earlyReply.thenComposeAsync(hook -> CompletableFuture.supplyAsync(() -> {
          try (ResponseBody b = apiClient.newCall(request).execute().body()) {
            return MAPPER.readValue(b.string(), ResponseSchema.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }, DatabaseApiHandshakeExecutor), internalProcessingExecutor)
        .exceptionally( // replace to "exceptionallyAsync" when update to java 12
            t -> {
              log.error("Encountered an Exception while sending a request to the DB API", t);
              return ResponseSchema.createEmpty();
            }).thenComposeAsync(response -> {
          if (!response.getInvokeMethod().equals(InvokeMethod.SEARCH_GUILD.toString())) {
            throw new IllegalStateException(
                "The response is not for the SEARCH_GUILD request. Please contact the developer.");
          }
          if (response.isExist()) {
            return event.getHook().sendMessage("このサーバーはすでにグローバルチャットに参加しています。").submit();
          } else {
            return event.getHook()
                .sendMessage("このサーバーはまだグローバルチャットに参加していません。\n以下のボタンから参加する/しないを選択してください。")
                .addActionRow(Button.primary("global_accept_join", "参加する"),
                    Button.danger("global_reject", "参加しない")).submit();
          }
        }, interactionExecutor)
        .whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor);
  }

  private void invokeRemoveSubCommand(@Nonnull SlashCommandInteractionEvent event,
      CompletableFuture<InteractionHook> earlyReply, Request request) {
    earlyReply.thenComposeAsync(hook -> CompletableFuture.supplyAsync(() -> {
          try (ResponseBody b = apiClient.newCall(request).execute().body()) {
            return MAPPER.readValue(b.string(), ResponseSchema.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }, DatabaseApiHandshakeExecutor).exceptionally(t -> {
          log.error("Encountered an Exception while sending a request to the DB API", t);
          return ResponseSchema.createEmpty();
        }).thenComposeAsync(response -> {
          if (response.isExist()) {
            return event.getHook()
                .sendMessage("データベースへの登録が確認できました。以下のボタンから削除する/しないを選択してください。")
                .addActionRow(Button.primary("global_accept_remove", "削除する"),
                    Button.danger("global_reject", "削除しない")).submit();
          } else {
            return event.getHook().sendMessage("データベースへの登録が確認できませんでした。").submit();
          }
        }, interactionExecutor)
        .whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor));
  }

  /**
   * {@link ListenerAdapter#onButtonInteraction(ButtonInteractionEvent)} メソッドをオーバーライドして、グローバル
   * チャットに関連するボタン インタラクションを処理します。
   *
   * @param event 処理するボタン インタラクション イベント
   */
  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    CompletableFuture<InteractionHook> deferReply = event.deferReply().submit();
    switch (event.getComponentId()) {
      case "global_accept_join":
        deferReply.thenComposeAsync(hook -> hook.editOriginal("グローバルチャットに参加します。").submit(),
                interactionExecutor)
            .thenComposeAsync(message -> generateResponseFuture(InvokeMethod.APPEND_INFORMATION,
                    Objects.requireNonNull(event.getGuild()).getId(), event.getChannel().getId()),
                internalProcessingExecutor).thenAcceptAsync(response -> {
              if (!response.getInvokeMethod().equals(InvokeMethod.APPEND_INFORMATION.toString())) {
                throw new IllegalStateException(
                    "The response is not for the APPEND_INFORMATION request. Please contact the developer.");
              }
              if (response.isExist()) {
                log.info("The guild [{}] has been registered to the global chat.", event.getGuild());
              } else {
                log.error("The guild [{}] has not been registered to the global chat.",
                    event.getGuild());
              }
            }, internalProcessingExecutor)
            .whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor);
        break;
      case "global_reject":
        deferReply.thenComposeAsync(hook -> hook.editOriginal("操作を終了しました。").submit(),
                interactionExecutor)
            .whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor);
        break;
      case "global_accept_remove":
        deferReply.thenComposeAsync(
                hook -> hook.sendMessage("データベースから削除します。ご参加ありがとうございました！").submit(),
                interactionExecutor).thenComposeAsync(
                message -> generateResponseFuture(InvokeMethod.DELETE_ROW,
                    Objects.requireNonNull(event.getGuild()).getId(),
                    event.getChannel().getId()), internalProcessingExecutor)
            .thenAcceptAsync(response -> {
              if (!response.getInvokeMethod().equals(InvokeMethod.DELETE_ROW.toString())) {
                throw new IllegalStateException(
                    "The response is not for the DELETE_ROW request. Please contact the developer.");
              }
              if (response.isExist()) {
                log.info("The guild [{}] has been deleted from the global chat.", event.getGuild());
              } else {
                log.error("The guild [{}] has not been deleted from the global chat.",
                    event.getGuild());
              }
            }, internalProcessingExecutor)
            .whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor);
        break;
      default:
        break;
    }
  }

  @NotNull
  private CompletableFuture<ResponseSchema> generateResponseFuture(
      InvokeMethod invokeMethod, String guildId, String channelId) {
    UriConstructor uriConstructor = new UriConstructor(
        invokeMethod, guildId, channelId);
    Request request = new Request.Builder().url(uriConstructor.construct()).get()
        .build();
    return CompletableFuture.supplyAsync(() -> {
      try (ResponseBody b = apiClient.newCall(request).execute().body()) {
        return MAPPER.readValue(b.string(), ResponseSchema.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }, DatabaseApiHandshakeExecutor);
  }

  static class UriConstructor {

    private static final String BASE_URI;

    static {
      JsonNode configNode;
      try {
        configNode = MAPPER.readTree(UriConstructor.class.getResourceAsStream("/property.json"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      BASE_URI = configNode.get("global_raid_api_uri").asText();
    }

    private final InvokeMethod invokeMethod;
    private final String guildId;
    private final String channelId;

    public UriConstructor(InvokeMethod invokeMethod, String guildId, String channelId) {
      this.invokeMethod = invokeMethod;
      this.guildId = guildId;
      this.channelId = channelId;
    }

    public String construct() {
      return BASE_URI + "?invokeMethod=" + invokeMethod + "&guildId=" + guildId + "&channelId="
          + channelId;
    }
  }
}
