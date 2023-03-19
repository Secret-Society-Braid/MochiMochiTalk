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
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class CommandJoin extends ListenerAdapter {

  private static final ExecutorService interactionExecutor = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("bot interaction thread")
  );

  private static final ExecutorService internalProcessingExecutor = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("bot internal processing thread")
  );

  private static final ExecutorService DatabaseApiHandshakeExecutor = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("bot database api handshake thread")
  );

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private OkHttpClient apiClient;

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    // early return if the command is not "join"
    if (!event.getName().equals("global")) {
      return;
    }

    // send early reply for asynchronously receive the response from the DB API.
    CompletableFuture<InteractionHook> earlyReply = event
        .reply("グローバルチャット レイドデータベースに接続しています…")
        .setEphemeral(true)
        .submit();

    // check whether the command is invoked in the guild
    if (event.isFromGuild()) {
      earlyReply.thenComposeAsync(
          hook -> hook.editOriginal("ダイレクトメッセージからの参加は将来のアップデートで実装予定です。").submit(),
          interactionExecutor
      ).thenAcceptAsync(
          (suc) -> log.info(
              "The command [global] command was invoked in the Direct Message Channel by [{}]",
              event.getUser()),
          internalProcessingExecutor
      ).whenCompleteAsync(ConcurrencyUtil::postEventHandling, internalProcessingExecutor);
      return;
    }

    UriConstructor searchGuildUriConstructor = new UriConstructor(
        InvokeMethod.SEARCH_GUILD,
        Objects.requireNonNull(event.getGuild()).getId(),
        event.getChannel().getId()
    );

    apiClient = apiClient == null ? new OkHttpClient() : apiClient;

    Request request = new Request.Builder()
        .url(searchGuildUriConstructor.construct())
        .get()
        .build();

    earlyReply.thenComposeAsync(
        hook -> CompletableFuture.supplyAsync(
            () -> {
              try (ResponseBody b = apiClient.newCall(request).execute().body()) {
                return MAPPER.readValue(b.string(), ResponseSchema.class);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }, DatabaseApiHandshakeExecutor)
        , internalProcessingExecutor).exceptionally(
        t -> {
          log.error("Encountered an Exception while sending a request to the DB API", t);
          return ResponseSchema.createEmpty();
        }).thenComposeAsync(
        response ->
            event.getChannel().sendMessage(
                ""
            ).submit(),
        internalProcessingExecutor);
  }

  static class UriConstructor {

    private static final String BASE_URI;
    private final InvokeMethod invokeMethod;
    private final String guildId;
    private final String channelId;

    static {
      JsonNode configNode;
      try {
        configNode = MAPPER.readTree(
            UriConstructor.class.getResourceAsStream("/property.json"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      BASE_URI = configNode.get("global_raid_api_uri").asText();
    }

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
