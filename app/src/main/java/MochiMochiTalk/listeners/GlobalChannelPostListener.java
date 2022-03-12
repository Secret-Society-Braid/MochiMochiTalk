package MochiMochiTalk.listeners;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.commands.GlobalChannelAddCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GlobalChannelPostListener extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalChannelPostListener.class);

    private volatile ConcurrentHashMap<String, String> globalMap = new ConcurrentHashMap<>();

    public GlobalChannelPostListener() {
        LOG.info("GlobalChannelPostListener created.");
        globalMap = GlobalChannelAddCommand.getGlobalChannelMap();
        LOG.debug("Currently registered servers");
        globalMap.forEach((k, v) -> {
            LOG.debug("GlobalChannelPostListener: " + k + " " + v);
        });
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        globalMap = GlobalChannelAddCommand.getGlobalChannelMap();
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();

        if(event.getAuthor().isBot())
            return;
        
        if(globalMap.containsKey(guild.getId())) {
            LOG.info("Server is registered");
            if(globalMap.get(guild.getId()).equals(channel.getId())) {
                LOG.info("channel {} is registered. posting message to other channels.", channel);
                String default_avatar = event.getJDA().getSelfUser().getAvatarUrl();
                String avatar = event.getAuthor().getAvatarUrl();
                if(avatar == null)
                    avatar = default_avatar;
                String message = event.getMessage().getContentRaw();
                String name = event.getAuthor().getName() + "[MochiMochiTalk]";
                event.getJDA()
            }
        }
    }
    
}
