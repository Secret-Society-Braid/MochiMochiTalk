package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import MochiMochiTalk.util.ConcurrencyUtil;
import com.google.api.client.util.Strings;
import hajimeapi4j.api.endpoint.EndPoint;
import hajimeapi4j.api.endpoint.ListEndPoint;
import hajimeapi4j.api.endpoint.MusicEndPoint;
import hajimeapi4j.internal.builder.ListEndPointBuilder;
import hajimeapi4j.internal.builder.MusicEndPointBuilder;
import hajimeapi4j.util.enums.ListParameter;
import hajimeapi4j.util.enums.MusicParameter;
import hajimeapi4j.util.enums.MusicParameter.Hide;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

// no fixes need for potentially null access
@Slf4j
public class CommandSong extends CommandInformation {

  @Override
  public String getCommandName() {
    return "song";
  }

  @Override
  protected String getCommandDescription() {
    return "デレステの楽曲情報を検索します (Powered by ふじわらはじめ楽曲DB)";
  }

  @Override
  protected void setCommandData() {
    if (this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
            this.getCommandName(),
            this.getCommandDescription())
        .setGuildOnly(true)
        .addSubcommands(
            new SubcommandData(
                "keyword",
                "指定したキーワードを基に、楽曲を検索します。")
                .addOptions(
                    new OptionData(
                        OptionType.STRING,
                        "keyword",
                        "検索する文字列を入力してください"
                    ).setRequired(true)
                ),
            new SubcommandData(
                "id",
                "指定した楽曲IDの楽曲情報を表示します。")
                .addOptions(
                    new OptionData(
                        OptionType.INTEGER,
                        "id",
                        "楽曲IDを入力してください。"
                    ).setRequired(true)
                ));
  }

  private static final Executor concurrentExecutor = Executors.newCachedThreadPool(
      new CountingThreadFactory(() -> "MochiMochiTalk", "Song detail integration thread", true));

  private static void invokeId(SlashCommandInteractionEvent event) {
    log.info("got interaction with following command: {} in {}", event.getSubcommandName(),
        event.getName());
    final InteractionHook hook = event.getHook();
    final String subCommandName = Objects.requireNonNull(event.getSubcommandName());
    int id = event.getOption(subCommandName, () -> -1, OptionMapping::getAsInt);

    if (id == -1) {
      hook.editOriginal("楽曲IDが指定されていない可能性があります。IDは整数の範囲でご指定ください。").queue();
      log.warn("User {} specified illegal song ID. this log is for unintended behavior recording purpose.",
          event.getUser());
      return;
    }

    hook.editOriginal("楽曲情報を取得しています……しばらくお待ちください。").queue();

    MusicEndPointBuilder result = MusicEndPointBuilder.createWith(id)
    .setHide(
        Hide.CD_MEMBER,
        Hide.LIVE_MEMBER);

    result
        .build()
        .handleAsync(
            r -> {
              if(r == null) {
                Throwable t = new NullPointerException("returned null response.");
                log.error("unexpected null response from API.", t);
                hook.editOriginal("楽曲情報の取得に失敗しました。")
                    .flatMap(m -> m.editMessageEmbeds(createErrorReportMessage(t)))
                    .queue();
              }
              hook.editOriginal("楽曲情報の取得に成功しました。表示します……")
                  .flatMap(m -> m.editMessageEmbeds(createSongDetailMessage(r)))
                  .queue();
            }
        );
  }

  private static void invokeKeyword(SlashCommandInteractionEvent event) {
    CompletableFuture<InteractionHook> earlyReplyFuture = event.reply(
            "検索を開始します…お待ちください。（Powered by ふじわらはじめAPI）")
        .submit();
    final String subCommandName = Objects.requireNonNull(event.getSubcommandName());
    String searchQuery = event.getOption(subCommandName, OptionMapping::getAsString);

    if (Strings.isNullOrEmpty(searchQuery)) {
      earlyReplyFuture.thenApplyAsync(
          hook -> hook.editOriginal("検索キーワードが指定されていない可能性があります。処理を中止しました。").complete(),
          concurrentExecutor).thenRunAsync(
          () -> log.warn(
              "it seems user {} specified no search query. this log is for unintended behavior recording purpose.",
              event.getUser()),
          concurrentExecutor);
      return; // early return for rejecting task
    }

    ListEndPointBuilder builder = ListEndPointBuilder.createFor(ListParameter.Type.MUSIC);
    builder
        .setMusicType(ListParameter.MusicType.CINDERELLA_GIRLS)
        .setSearch(searchQuery)
        .setLimit(1);

    CompletableFuture<Message> sendResultMessage = earlyReplyFuture.thenCombineAsync(
      builder.build().handleAsync(),
        (hook, response) -> {
          MessageEmbed resultEmbed = createSearchResultMessage(response);
          hook.editOriginal("検索完了。表示します……").complete();
          return hook.editOriginalEmbeds(resultEmbed).complete();
        },
        concurrentExecutor);

    // post process
    sendResultMessage.whenCompleteAsync(
        ConcurrencyUtil::postEventHandling,
        concurrentExecutor);
  }

  private static MessageEmbed createSongDetailMessage(@Nullable MusicEndPoint response) {
    EmbedBuilder builder = new EmbedBuilder();
    builder
        .setTitle(String.format("ID:%d の楽曲情報", response.getSongId()), response.getLink())
        .setDescription("ブラウザでこの情報を見るにはこのメッセージのタイトルをクリック")
        .addField("楽曲名", response.getName(), false)
        .setFooter("MochiMochiTalk Song detail integration powered by ふじわらはじめAPI");
    setInheritListedInformation(builder, response.getComposer().orElse(Collections.emptyList()),
        "作曲者名");
    setInheritListedInformation(builder, response.getLyrics().orElse(Collections.emptyList()),
        "作詞者名");
    setInheritListedInformation(builder, response.getArrange().orElse(Collections.emptyList()),
        "編曲者名");
    setInheritListedInformation(builder, response.getMember(), "歌唱メンバー");
    return builder.build();
  }

  private static MessageEmbed createSearchResultMessage(List<ListEndPoint> response) {
    EmbedBuilder builder = new EmbedBuilder();
    builder
        .setTitle("検索結果")
        .setDescription("最も関連性のある1曲が表示されます。")
        .setFooter("MochiMochiTalk Song detail integration powered by ふじわらはじめAPI");

    if (response.size() != 1) {
      throw new IllegalStateException("unexpected response size: " + response.size());
    }

    setSearchedInformation(builder, response.get(0));
    return builder.build();
  }

  private static void setInheritListedInformation(EmbedBuilder target,
      List<? extends EndPoint> information,
      @Nonnull String fieldTitle) {
    if (information == null || information.isEmpty()) {
      return;
    }
    if (Strings.isNullOrEmpty(fieldTitle)) {
      return;
    }
    information
        .parallelStream()
        .map(EndPoint::getName)
        .forEach(name -> target.addField(fieldTitle, Objects.requireNonNull(name), true));
  }

  @Nonnull
  private static MessageEmbed createErrorReportMessage(Throwable t) {
    EmbedBuilder builder = new EmbedBuilder();
    builder
        .setTitle("楽曲情報の取得中にエラーが発生した模様です。")
        .setDescription("発生個所：CommandSong#onSlashCommandInteractionEvent")
        .addField("例外", Objects.requireNonNull(t.getClass().getName()), false)
        .addField("エラーメッセージ",
            t.getMessage() == null ? "null" : Objects.requireNonNull(t.getMessage()), false)
        .addField("エラーのスタックトレース", Objects.requireNonNull(Arrays.toString(t.getStackTrace())), false)
        .setFooter("MochiMochiTalk Automatic Error Reporter");
    return builder.build();
  }

  private static void setSearchedInformation(EmbedBuilder target, ListEndPoint information) {
    if (information == null) {
      return;
    }
    target.addField(
        information.getName() + "(内部管理ID" + information.getSongId() + ")",
        information.getLink(),
        false);
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    final String subCommandName = Objects.requireNonNull(event.getSubcommandName());
    log.info("got interaction with following command: {} in {}", subCommandName, event.getName());

    switch (subCommandName) {
      case "id":
        invokeId(event);
        break;
      case "keyword":
        invokeKeyword(event);
        break;
      default:
        log.error("unexpected subcommand name: {}", subCommandName);
        throw new UnsupportedOperationException("unexpected subcommand name: " + subCommandName);
    }
  }
}
