package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import MochiMochiTalk.util.DiscordServerOperatorUtil;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
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
    event.deferReply(true).queue();
    InteractionHook hook = event.getHook().setEphemeral(true);
    if (!DiscordServerOperatorUtil.isMutsucordOperator(event.getUser().getId())) {
      hook.editOriginal("このコマンドは管理者のみ使用できます。").queue();
      return;
    }
    log.info("confirmed identity. invoking shutdown");
    hook.editOriginal("終了しています……おやすみなさい。プロデューサーさん")
      .queue(suc -> suc.getJDA().shutdown());
  }
}
