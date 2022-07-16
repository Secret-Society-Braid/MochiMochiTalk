package MochiMochiTalk.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class EventLogger extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        Channel channel = event.getChannel();
        Message message = event.getMessage();
        User user = event.getAuthor();

        log.debug("guild that event fired: {}", guild);
        log.debug("channel: {}", channel);
        log.debug("message content: {}", message);
        log.debug("user data: {}", user);

    }
    
}
