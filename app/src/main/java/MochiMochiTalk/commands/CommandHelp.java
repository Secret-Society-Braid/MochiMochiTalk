package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class CommandHelp extends CommandInformation {

  @Override
  public String getCommandName() {
    return "help";
  }

  @Override
  protected String getCommandDescription() {
    return "（廃止済み）：コマンドの一覧と説明を表示します。";
  }

  @Override
  protected void setCommandData() {
    if (this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
      this.getCommandName(),
      this.getCommandDescription()
    );
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    log.info("help slash command invoked.");
    event
      .deferReply()
      .setEphemeral(true)
      .queue(
        suc -> suc
          .setEphemeral(true)
          .editOriginal("コマンドの各詳細はスラッシュコマンド一覧の説明をご参照ください。")
          .queue(),
        err -> log.error("failed to interact with slash command.", err));
  }
}
