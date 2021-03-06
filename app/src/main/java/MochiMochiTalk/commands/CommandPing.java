package MochiMochiTalk.commands;

// imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class CommandPing extends ListenerAdapter {
    
    private Logger logger = LoggerFactory.getLogger(CommandPing.class);

    // make ping command
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if(author.isBot()) {
            return;
        }
        if(content.equalsIgnoreCase(App.prefix + "ping")) {
            logger.info("Ping command received.");
            logger.info("Channel: {}", channel.getName());
            logger.info("Author: {}", author.getName());
            logger.info("Guild: {}", guild.getName());
            logger.info("Pong!");
            long time = System.currentTimeMillis();
            channel.sendMessage("ぽ…ぽんっ…！").queue(response -> {
                response.editMessageFormat("ぽ…ぽんっ…！: ping -> %d ms", (System.currentTimeMillis() - time)).queue();
            });
        }
    }

}
