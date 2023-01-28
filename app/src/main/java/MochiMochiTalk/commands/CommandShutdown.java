package MochiMochiTalk.commands;

import MochiMochiTalk.App;
import java.util.List;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandShutdown extends ListenerAdapter {

  // Logger
  private static final Logger LOG = LoggerFactory.getLogger(CommandShutdown.class);

  private static final List<String> IDs = List.of("399143446939697162", "666213020653060096",
      "682079802605174794", "365695324947349505", "492145462908944422", "538702103372103681",
      "482903571625410560", "686286747084390433", "706819045286215765");

  // Shutdown command
  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    String content = event.getMessage().getContentRaw();
    if (content.equalsIgnoreCase(App.getStaticPrefix() + "shutdown")) {
      LOG.info("Message received: {}", event.getMessage().getContentRaw());
      if (IDs.contains(event.getAuthor().getId())) {
        LOG.info("Shutdown command received");
        event.getChannel().sendMessage("終了しています…おやすみなさい。プロデューサーさん").queue(suc -> {
          suc.getJDA().shutdown();
        });
      }
      event.getChannel().sendMessage("このコマンドは管理者のみ使用できます。").queue();
    }
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("shutdown")) {
      LOG.info("Shutdown has been requested by : {}", event.getUser());
      if (IDs.contains(event.getUser().getId())) {
        LOG.info("confirmed identity. invoking shutdown");
        event.reply("終了しています……おやすみなさい。プロデューサーさん").setEphemeral(true)
            .queue(suc -> event.getJDA().shutdown());
      }
      event.reply("このコマンドは管理者のみ使用できます。").setEphemeral(true).queue();
    }
  }

}
