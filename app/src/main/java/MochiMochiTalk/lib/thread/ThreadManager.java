package MochiMochiTalk.lib.thread;

import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

@Slf4j
public class ThreadManager {

  private static ThreadManager instance;
  private final List<ThreadChannel> threadChannels;

  private ThreadManager() {
    this.threadChannels = new LinkedList<>();
  }

  public static ThreadManager getInstance() {
    if (instance == null) {
      instance = new ThreadManager();
    }
    return instance;
  }
}
