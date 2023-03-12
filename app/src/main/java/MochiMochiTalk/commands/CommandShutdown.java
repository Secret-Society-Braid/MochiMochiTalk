package MochiMochiTalk.commands;

import MochiMochiTalk.util.DiscordServerOperatorUtil;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandShutdown extends ListenerAdapter {

  // Logger
  private static final Logger LOG = LoggerFactory.getLogger(CommandShutdown.class);

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("shutdown")) {
      LOG.info("Shutdown has been requested by : {}", event.getUser());
      if (!DiscordServerOperatorUtil.isMutsucordOperator(event.getUser().getId())) {
        event.reply("このコマンドは管理者のみ使用できます。").setEphemeral(true).queue();
        return;
      }
      LOG.info("confirmed identity. invoking shutdown");
      event.reply("終了しています……おやすみなさい。プロデューサーさん").setEphemeral(true)
          .queue(suc -> suc.getJDA().shutdown());
    }
  }

}
