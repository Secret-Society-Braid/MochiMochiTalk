package MochiMochiTalk.commands;

import java.util.List;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandShutdown extends ListenerAdapter {

  // Logger
  private static final Logger LOG = LoggerFactory.getLogger(CommandShutdown.class);

  private static final List<String> IDs = List.of("399143446939697162", "666213020653060096",
      "682079802605174794", "365695324947349505", "492145462908944422", "538702103372103681",
      "482903571625410560", "686286747084390433", "706819045286215765");

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("shutdown")) {
      LOG.info("Shutdown has been requested by : {}", event.getUser());
      if (!IDs.contains(event.getUser().getId())) {
        event.reply("このコマンドは管理者のみ使用できます。").setEphemeral(true).queue();
        return;
      }
      LOG.info("confirmed identity. invoking shutdown");
      event.reply("終了しています……おやすみなさい。プロデューサーさん").setEphemeral(true)
          .queue(suc -> suc.getJDA().shutdown());
    }
  }

}
