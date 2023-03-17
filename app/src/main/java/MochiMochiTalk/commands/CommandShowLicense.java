package MochiMochiTalk.commands;

import MochiMochiTalk.util.ConcurrencyUtil;
import MochiMochiTalk.util.DiscordServerOperatorUtil;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

@Slf4j
public class CommandShowLicense extends ListenerAdapter {

  private static final String LICENSE_URL = "https://github.com/Secret-Society-Braid/MochiMochiTalk/tree/main/app/src/main/resources/licenses.json";
  private static final ExecutorService serv = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("license file fetch thread")
  );

  @Nonnull
  private static synchronized MessageEmbed constructReplyEmbedMessage() {
    EmbedBuilder builder = new EmbedBuilder();
    builder
        .setTitle("使用ライブラリのライセンス情報", LICENSE_URL)
        .setDescription("Botが使用しているライブラリの情報は、上のタイトルリンクをクリックの上ご確認ください。")
        .addField("このBotのライセンス情報", String.format("このBotは <@%s> によって開発、保守されています。",
            DiscordServerOperatorUtil.getBotDevUserId()), false)
        .addField("ソースコード、コントリビューション",
            "MochiMochiTalkはOSS（オープンソースプロジェクト）です。\nソースは以下のリポジトリで公開しています。", false)
        .addField("OSSリポジトリ", "https://github.com/Secret-Society-Braid/MochiMochiTalk", false);
    return builder.build();
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (!event.getName().equals("license")) {
      return;
    }
    event.replyEmbeds(constructReplyEmbedMessage()).setEphemeral(true).submit()
        .whenCompleteAsync((ret, ex) -> {
          if (ex == null) {
            log.info("the command interaction [showLicense] has been finished successfully");
            return;
          }
          log.warn("There was an exception while invoking [showLicense] event handling.", ex);
          log.warn("constructing information message for devs...");
          EmbedBuilder builder = new EmbedBuilder();
          final String exceptionClassName = ex.getClass().getSimpleName();
          log.warn("The exception that was encountered: {}", exceptionClassName);
          final String exceptionMessage = ex.getMessage();
          log.warn("The message that was sent: {}", exceptionMessage);
          final String exceptionStackTrace = Arrays.toString(ex.getStackTrace());
          log.warn("Got stack trace.");
          builder.setTitle("イベント処理中に例外が発生しました")
              .addField("例外", exceptionClassName, false)
              .addField("例外メッセージ", (exceptionMessage == null ? "null" : exceptionMessage), false)
              .addField("スタックトレース", exceptionStackTrace, false);
          event.getJDA()
              .retrieveUserById(DiscordServerOperatorUtil.getBotDevUserId())
              .map(User::openPrivateChannel)
              .map(RestAction::complete)
              .submit()
              .thenComposeAsync(channel -> channel.sendMessageEmbeds(builder.build()).submit(),
                  serv);
        }, serv);
  }
}
