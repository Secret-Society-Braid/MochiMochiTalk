package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;


@Slf4j
public class CommandPing extends CommandInformation {


  @Override
  public String getCommandName() {
    return "ping";
  }

  @Override
  protected String getCommandDescription() {
    return "Botのpingを計測します。";
  }

  @Override
  protected void setCommandData() {
    if (this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
      this.getCommandName(),
      this.getCommandDescription());
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    log.info("ping slash command invoked.");
    long time = System.currentTimeMillis();
    event
      .reply("ぽ…ぽんっ…！")
      .queue(
        suc -> suc.editOriginalFormat("ぽ…ぽんっ…！：ping -> %d ms",
            (System.currentTimeMillis() - time))
          .queue());
  }

}
