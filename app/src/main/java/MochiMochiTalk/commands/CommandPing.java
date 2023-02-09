package MochiMochiTalk.commands;

import MochiMochiTalk.App;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandPing extends ListenerAdapter {

  private final Logger logger = LoggerFactory.getLogger(CommandPing.class);

  // make ping command
  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    User author = event.getAuthor();
    Message message = event.getMessage();
    String content = message.getContentRaw();
    MessageChannel channel = event.getChannel();
    Guild guild = event.getGuild();
    if (author.isBot()) {
      return;
    }
    if (content.equalsIgnoreCase(App.getStaticPrefix() + "ping")) {
      logger.info("Ping command received.");
      logger.info("Channel: {}", channel.getName());
      logger.info("Author: {}", author.getName());
      logger.info("Guild: {}", guild.getName());
      logger.info("Pong!");
      long time = System.currentTimeMillis();
      channel.sendMessage("ぽ…ぽんっ…！").queue(response -> {
        response.editMessageFormat("ぽ…ぽんっ…！: ping -> %d ms", (System.currentTimeMillis() - time))
            .queue();
      });
    }
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("ping")) {
      logger.info("ping slash command invoked.");
      long time = System.currentTimeMillis();
      event.reply("ぽ…ぽんっ…！").queue(suc -> {
        suc.editOriginalFormat("ぽ…ぽんっ…！：ping -> %d ms", (System.currentTimeMillis() - time))
            .queue();
      });
    }
  }

}
