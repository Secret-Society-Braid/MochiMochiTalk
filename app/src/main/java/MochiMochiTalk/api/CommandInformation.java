package MochiMochiTalk.api;

import java.util.Objects;
import javax.annotation.Nonnull;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class CommandInformation {

  protected CommandData commandData;

  public abstract String getCommandName();

  protected abstract String getCommandDescription();

  public CommandData getCommandData() {
    this.setCommandData();
    return this.commandData;
  }

  protected abstract void setCommandData();

  public abstract void slashCommandHandler(@NotNull SlashCommandInteractionEvent event);

  protected boolean shouldHandle(@Nonnull SlashCommandInteractionEvent e) {
    return Objects.equals(e.getName(), this.getCommandName());
  }
}
