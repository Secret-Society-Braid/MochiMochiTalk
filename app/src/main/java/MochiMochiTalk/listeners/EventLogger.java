package MochiMochiTalk.listeners;

import javax.annotation.Nonnull;

import MochiMochiTalk.App;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class EventLogger extends ListenerAdapter {

    private static EventLogger instance;
    private static boolean isEnabled = false;

    public static EventLogger getInstance() {
        if(instance == null)
            instance = new EventLogger();
        return instance;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();

        String contentRaw = message.getContentRaw();

        if(!author.isBot() && contentRaw.startsWith(App.getStaticPrefix() + "eventlog ")) {
            String[] split = contentRaw.split(" ");
            if(split.length == 1) {
                log.warn("None parameter has been requested. do nothing.");
                return;
            } else if("on".equals(split[1])) {
                setLogMode(true);
                log.info("event logger is now enabled by {}", author);
		channel.sendMessage("イベントの記録を開始します。").queue();
            } else if("off".equals(split[1])) {
                setLogMode(false);
                log.info("event logger is now disabled by {}", author);
		channel.sendMessage("イベントの記録を中断します。").queue();
            } else {
                /* do nothing */
            }
        }

        if(isEnabled) {
            log.debug("Discord event log");
            log.debug("--------------------------------");
            log.debug("Guild : {}", guild);
            log.debug("Channel : {}", channel);
            log.debug("author: {}", author);
            log.debug("message: {}", message);
            log.debug("--------------------------------");
            log.debug("End of Discord event log");
        }
    }

    private static void setLogMode(boolean val) {
        isEnabled = val;
    }
}
