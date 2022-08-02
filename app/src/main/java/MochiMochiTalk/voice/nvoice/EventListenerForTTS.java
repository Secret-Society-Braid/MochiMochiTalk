package MochiMochiTalk.voice.nvoice;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

@Slf4j
public class EventListenerForTTS extends ListenerAdapter {

    private static final CountingThreadFactory factory = new CountingThreadFactory(() -> "MochiMochiTalk", "AFK-Checker");

    private MessageChannel boundedChannel;
    private AudioManager audioManager;
    private GoogleTTSEngine engine;
    private boolean flag;
    
    private static ScheduledExecutorService schedulerService;
    private static final List<String> allowed; 
    
    static {
        allowed = readWhiteList();
    }

    // TODO: Complete implementation for others

    private static List<String> readWhiteList() {
        // TODO: complete implementation
    }

}
