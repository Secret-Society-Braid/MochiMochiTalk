package MochiMochiTalk.lib.thread;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
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
        // check if guild is marked for checking
        Guild guild = event.getGuild();
        if(!guildsForChecking.contains(guild.getId()))
            log.warn("this operation is not allowed for guild: {}", guild);
    }
}
