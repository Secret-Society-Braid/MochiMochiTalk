package MochiMochiTalk.lib.thread;

import MochiMochiTalk.util.ConcurrencyUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

@Slf4j
public class ThreadManager {

  private static ThreadManager instance;
  private static final ExecutorService executorService = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("Concurrent ThreadChannel Manager")
  );
  @Getter
  private final Map<Guild, List<ThreadChannel>> threadChannels;
  private final AtomicBoolean initialized = new AtomicBoolean(false);

  private ThreadManager(JDA jda, boolean awaitInitialize) {
    this.threadChannels = new ConcurrentHashMap<>();
    CompletableFuture<Void> initializeFuture = init(jda);
    if (awaitInitialize) {
      initializeFuture.join();
    }
  }

  public static ThreadManager getInstance(JDA jda, boolean awaitInitialize) {
    if (instance == null) {
      instance = new ThreadManager(jda, awaitInitialize);
    }
    return instance;
  }

  private CompletableFuture<Void> init(JDA jda) {
    return CompletableFuture
        .runAsync(() -> this.updateTask(jda), executorService)
        .thenRunAsync(() -> {
          log.info("ThreadChannel Manager initialized");
          initialized.set(true);
        }, executorService);
  }

  public boolean isInitialized() {
    return initialized.get();
  }

  public boolean update(JDA jda) {
    if (!isInitialized()) {
      return false;
    }
    updateTask(jda);
    return true;
  }

  public List<ThreadChannel> getThreadChannels(Guild guild) {
    return threadChannels.get(guild);
  }

  private void updateTask(JDA jda) {
    for (Guild guild : jda.getGuilds()) {
      List<ThreadChannel> threadChannels = guild.getThreadChannels();
      this.threadChannels.put(guild, threadChannels);
    }
  }
}
