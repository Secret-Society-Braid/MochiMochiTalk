package MochiMochiTalk.lib.thread;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class ThreadCreateEventHandler extends ListenerAdapter {
   
    private static final List<String> guildsForChecking = List.of(
        "629249258960453652", // mutsu cord
        "649603185115267092" // testing server
    );
    
    // overrides
    @Override
    public void onChannelCreate(@Nonnull ChannelCreateEvent event) {
        // early return if channel type is not for thread channels
        ChannelType channelType = event.getChannelType();
        if(!channelType.isThread())
            return;
        // check if guild is marked for checking
        Guild guild = event.getGuild();
        if(!guildsForChecking.contains(guild.getId())) {
            log.info("guild {} is not marked for checking", guild.getName());
            return;
        }
        
    }
}
