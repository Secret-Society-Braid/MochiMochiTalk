package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import MochiMochiTalk.util.DiscordServerOperatorUtil;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class CommandShutdown extends CommandInformation {

  @Override
  public String getCommandName() {
    return "shutdown";
  }

  @Override
  protected String getCommandDescription() {
    return "Botを強制的に停止させます。";
  }

  @Override
  protected void setCommandData() {
    if (this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
      this.getCommandName(),
      this.getCommandDescription()
    ).setGuildOnly(true);
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    log.info("Shutdown has been requested by : {}", event.getUser());
    if (!DiscordServerOperatorUtil.isMutsucordOperator(event.getUser().getId())) {
      event.reply("このコマンドは管理者のみ使用できます。").setEphemeral(true).queue();
      return;
    }
    log.info("confirmed identity. invoking shutdown");
    event.reply("終了しています……おやすみなさい。プロデューサーさん").setEphemeral(true)
      .queue(suc -> suc.getJDA().shutdown());
  }
}
