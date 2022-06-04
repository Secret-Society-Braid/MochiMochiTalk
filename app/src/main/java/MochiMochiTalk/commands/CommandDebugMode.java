package MochiMochiTalk.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandDebugMode extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandDebugMode.class);

    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        
        if(author.isBot()) {
            return;
        }

        if(content.startsWith(App.prefix + "debug ")) {
            String[] split = content.split(" ");
            if(split.length == 2) {
                if(split[1].equals("true")) {
                    channel.sendMessage("デバッグモードをONにしました。").queue();
                } else {
                    channel.sendMessage("デバッグモードをOFFにしました。").queue();
                }
            }
        }
    }

    
}
