package MochiMochiTalk.lib.thread;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

@Slf4j
public class ThreadManager {

    @Getter
    private static List<ThreadChannel> threadChannels = new LinkedList<>();

    private ThreadManager() {
    }

    public static synchronized boolean addThreadChannel(ThreadChannel threadChannel) {
        if (threadChannels.contains(threadChannel)) {
            log.warn("thread channel {} is already added", threadChannel.getName());
            return false;
        }
        threadChannels.add(threadChannel);
        log.info("thread channel {} is added", threadChannel.getName());
        return true;
    }
}
