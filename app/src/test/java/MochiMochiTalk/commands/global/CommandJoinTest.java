package MochiMochiTalk.commands.global;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandJoinTest {

  @Test
  void notFromGuildTest() {
    SlashCommandInteractionEvent mockEvent = mock(SlashCommandInteractionEvent.class,
        RETURNS_DEEP_STUBS);
    InteractionHook mockHook = mock(InteractionHook.class, RETURNS_DEEP_STUBS);
    Message mockMessage = mock(Message.class, RETURNS_DEEP_STUBS);

    when(mockEvent.isFromGuild()).thenReturn(false);
    when(mockEvent.getName()).thenReturn("global");
    when(mockEvent.reply(anyString()).setEphemeral(anyBoolean()).submit()).thenReturn(
        CompletableFuture.completedFuture(mockHook));
    when(mockHook.editOriginal(anyString()).submit()).thenReturn(
        CompletableFuture.completedFuture(mockMessage));

    CommandJoin commandJoin = new CommandJoin();
    commandJoin.onSlashCommandInteraction(mockEvent);

    verify(mockEvent).isFromGuild();
  }
}