package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import com.google.api.client.util.Strings;
import hajimeapi4j.api.endpoint.EndPoint;
import hajimeapi4j.api.endpoint.ListEndPoint;
import hajimeapi4j.api.endpoint.MusicEndPoint;
import hajimeapi4j.internal.builder.ListEndPointBuilder;
import hajimeapi4j.internal.builder.MusicEndPointBuilder;
import hajimeapi4j.util.enums.ListParameter;
import hajimeapi4j.util.enums.MusicParameter.Hide;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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

  private static MessageEmbed createSongDetailMessage(MusicEndPoint response) {
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

  private void invokeId(SlashCommandInteractionEvent event) {
    log.info("got interaction with following command: {} in {}", event.getSubcommandName(),
        event.getName());
    final InteractionHook hook = event.getHook();
    final String subCommandName = Objects.requireNonNull(event.getSubcommandName());
    int id = event.getOption(subCommandName, () -> -1, m -> m.getAsInt() <= 0 ? -1 : m.getAsInt());

    if (id == -1) {
      hook.editOriginal(
          "楽曲IDが指定されていない可能性があります。IDは自然数の範囲でご指定ください。").queue();
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
                return;
              }
              hook.editOriginal("楽曲情報の取得に成功しました。表示します……")
                  .flatMap(m -> m.editMessageEmbeds(createSongDetailMessage(r)))
                  .queue();
            }
        );
  }

  private void invokeKeyword(SlashCommandInteractionEvent event) {
    log.info("invoked keyword search command.");
    final InteractionHook hook = event.getHook();
    final String subCommandName = Objects.requireNonNull(event.getSubcommandName());
    String searchQuery = event.getOption(subCommandName, OptionMapping::getAsString);

    if (Strings.isNullOrEmpty(searchQuery)) {
      hook.editOriginal("検索キーワードを正しく指定してください。").queue();
      log.warn(
          "User {} specified illegal search query. this log is for unintended behavior recording purpose.",
          event.getUser());
      return;
    }

    hook.editOriginal("楽曲情報を検索しています……しばらくお待ちください。").queue();

    ListEndPointBuilder builder = ListEndPointBuilder.createFor(ListParameter.Type.MUSIC);
    builder
        .setMusicType(ListParameter.MusicType.CINDERELLA_GIRLS)
        .setSearch(searchQuery)
        .setLimit(1);

    builder
        .build()
        .handleAsync(
            r -> {
              if (r == null) {
                Throwable t = new NullPointerException("returned null response.");
                log.error("unexpected null response from API.", t);
                hook.editOriginal("楽曲情報の取得に失敗しました。")
                    .flatMap(m -> m.editMessageEmbeds(createErrorReportMessage(t)))
                    .queue();
                return;
              }
              hook.editOriginal("楽曲情報の取得に成功しました。表示します……")
                  .flatMap(m -> m.editMessageEmbeds(createSearchResultMessage(r)))
                  .queue();
            }
        );
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
