package MochiMochiTalk.commands;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandPing extends ListenerAdapter {

  private final Logger logger = LoggerFactory.getLogger(CommandPing.class);

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("ping")) {
      logger.info("ping slash command invoked.");
      long time = System.currentTimeMillis();
      event.reply("ぽ…ぽんっ…！").queue(suc -> suc.editOriginalFormat("ぽ…ぽんっ…！：ping -> %d ms",
              (System.currentTimeMillis() - time))
          .queue());
    }
  }

}
