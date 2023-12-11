package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Slf4j
public class CommandDebugMode extends CommandInformation {

  private final DebugModeModerator debugModeModerator = DebugModeModerator.getInstance();

  @Override
  public String getCommandName() {
    return "debugmode";
  }

  @Override
  protected String getCommandDescription() {
    return "Botの開発を容易にするデバッグモードを切り替えます";
  }

  @Override
  protected void setCommandData() {
    if (this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
        this.getCommandName(),
        this.getCommandDescription())
      .addSubcommands(
        new SubcommandData("on", "デバッグモードをONにします。"),
        new SubcommandData("off", "デバッグモードをOFFにします")
      );
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    log.debug("debugmode slash command invoked.");
    String subcommandName = Objects.requireNonNull(event.getSubcommandName());

    switch (Objects.requireNonNullElseGet(event.getSubcommandName(), () -> {
      log.warn("No subcommand name found. what caused to reach here?");
      log.warn("Falling back to \"off\" subcommand for preventing excessive log output.");
      return "off";
    })) {
      case "on":
        log.trace("subcommand: on");
        event.reply("デバッグモードをONにしました。").setEphemeral(true).queue();
        debugModeModerator.setDebugState(true);
        log.debug(
          "debug is enabled by {}, in {} channel, in the {} guild. now we will output things necessary.",
          event.getUser(), event.getChannel(), event.getGuild());
        break;
      case "off":
        log.trace("subcommand: off");
        event.reply("デバッグモードをOFFにしました。").setEphemeral(true).queue();
        debugModeModerator.setDebugState(false);
        log.debug("debug is disabled. now we stop debugging...");
        break;
      default:
        throw new IllegalStateException("Unexpected subcommand value: " + subcommandName);
    }
  }

  public static class DebugModeModerator {

    private static DebugModeModerator singleton;
    private final AtomicBoolean debugState = new AtomicBoolean(false);

    public static DebugModeModerator getInstance() {
      if (singleton == null) {
        singleton = new DebugModeModerator();
      }
      return singleton;
    }

    public synchronized boolean getDebugState() {
      return debugState.get();
    }

    public synchronized void setDebugState(boolean bool) {
      debugState.set(bool);
    }
  }
}
