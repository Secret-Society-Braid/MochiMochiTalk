package MochiMochiTalk.commands;

import MochiMochiTalk.App;
import java.util.Objects;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class CommandDebugMode extends ListenerAdapter {

  private static CommandDebugMode singleton;
  private static volatile boolean debugState = false;

  public static CommandDebugMode getInstance() {
    if (singleton == null) {
      singleton = new CommandDebugMode();
    }
    return singleton;
  }

  public synchronized boolean getDebugState() {
    return debugState;
  }

  private static synchronized void setDebugState(boolean bool) {
    debugState = bool;
  }

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    User author = event.getAuthor();
    Message message = event.getMessage();
    String content = message.getContentRaw();
    MessageChannel channel = event.getChannel();

    if (author.isBot()) {
      return;
    }

    if (content.startsWith(App.getStaticPrefix() + "debug ")) {
      String[] split = content.split(" ");
      if (split.length == 2) {
        if (split[1].equals("on")) {
          channel.sendMessage("デバッグモードをONにしました。").queue();
          setDebugState(true);
          log.debug(
              "debug is enabled by {} , in {} channel, in the {} guild. now we will output things necessary.",
              author, channel, event.getGuild());
        } else {
          channel.sendMessage("デバッグモードをOFFにしました。").queue();
          setDebugState(false);
          log.debug("debug is disabled. now we stop debugging...");
        }
      }
    }
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("debugmode")) {
      log.debug("debugmode slash command invoked.");
      String subcommandName = Objects.requireNonNull(event.getSubcommandName());
      if (subcommandName.equals("on")) {
        event.reply("デバッグモードをONにしました。").setEphemeral(true).queue();
        setDebugState(true);
        log.debug(
            "debug is enabled by {}, in {} channel, in the {} guild. now we will output things necessary.",
            event.getUser(), event.getChannel(), event.getGuild());
      } else if (subcommandName.equals("off")) {
        event.reply("デバッグモードをOFFにしました。").setEphemeral(true).queue();
        setDebugState(false);
        log.debug("debug is disabled. now we stop debugging...");
      } else {
        event.reply("You cannot reach this... how did you do that!?").setEphemeral(true).queue();
      }
    }
  }


}
