package MochiMochiTalk.commands.global;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandJoinTest {

  @Test
  void onSlashCommandInteractionTest() {
    SlashCommandInteractionEvent mockEvent = mock(SlashCommandInteractionEvent.class);
    ReplyCallbackAction mockReplyCallbackAction = mock(ReplyCallbackAction.class);

    when(mockEvent.getName()).thenReturn("unknown");

    when(mockEvent.reply(anyString())).thenReturn(mockReplyCallbackAction);
    when(mockReplyCallbackAction.setEphemeral(true)).thenReturn(mockReplyCallbackAction);
    when(mockReplyCallbackAction.submit());

    CommandJoin commandJoin = new CommandJoin();
    commandJoin.onSlashCommandInteraction(mockEvent);

    verify(mockEvent).getName(); // ensure that getName() is called once
  }
}