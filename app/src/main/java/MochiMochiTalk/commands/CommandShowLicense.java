package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import MochiMochiTalk.util.DiscordServerOperatorUtil;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class CommandShowLicense extends CommandInformation {

  private static final String LICENSE_URL = "https://github.com/Secret-Society-Braid/MochiMochiTalk/tree/main/app/src/main/resources/licenses.json";

  @Override
  public String getCommandName() {
    return "showLicense";
  }

  @Override
  protected String getCommandDescription() {
    return "このBotが使用しているライブラリのライセンス情報を表示します。";
  }

  @Override
  protected void setCommandData() {
    if(this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
        this.getCommandName(),
        this.getCommandDescription()
    );
  }

  @Nonnull
  private synchronized MessageEmbed constructReplyEmbedMessage() {
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
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    log.info("showLicense command invoked.");
    event
        .getHook()
        .setEphemeral(true)
        .editOriginalEmbeds(constructReplyEmbedMessage())
        .queue();
  }
}
