package MochiMochiTalk.lib.comms;

import MochiMochiTalk.util.ConcurrencyUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

@Slf4j
public class CommunicationActionImpl implements CommunicationAction {

  public static final Runnable DEFAULT_ON_SUCCESS = () -> {
    log.info("work succeeded.");
  };
  public static final BiConsumer<Void, ? super Throwable> DEFAULT_ON_FAILURE = (ret, t) -> {
    if (t == null) {
      return;
    }
    log.warn("communication process has been finished with error.");
    if (t instanceof ExecutionException || t instanceof RejectedExecutionException) {
      log.warn("Exception while processing communication asynchronously.", t.getCause());
    } else if (t instanceof InterruptedException) {
      log.warn("Execution thread has been interrupted by someone.", t.getCause());
    } else if (t instanceof Exception) {
      log.warn("Exception while processing communication.", t);
    } else if (t instanceof Error) {
      log.error("VM Error has been encountered while processing communication.", t);
    } else {
      throw new RuntimeException(t);
    }
  };
  private static final List<InterClassComms> MESSAGE_QUEUE = new ArrayList<>();
  private static final int DEFAULT_CONCURRENCY = 16;
  private static final ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
      DEFAULT_CONCURRENCY,
      new CountingThreadFactory(() -> "MochiMochiTalk", "InterClassCommunication Thread", false));
  private static final ExecutorService DEFAULT_INTERNAL_EXECUTOR = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("CommunicationAction Internal Process Thread"));
  private InterClassComms comms;
  private String message;
  private Class<?> sender;
  private Collection<Class<?>> recipients;

  public CommunicationActionImpl(InterClassComms comms) {
    this.comms = comms;
  }

  // queue()

  @Override
  public synchronized void queue() {
    queue(DEFAULT_ON_SUCCESS);
  }

  @Override
  public synchronized void queue(Runnable success) {
    queue(success, DEFAULT_ON_FAILURE);
  }

  @Override
  public synchronized void queue(Runnable success, BiConsumer<Void, ? super Throwable> failure) {
    checkInterCommsInstance();
    CompletableFuture.runAsync(() -> MESSAGE_QUEUE.add(this.comms), DEFAULT_INTERNAL_EXECUTOR)
        .thenRunAsync(success, DEFAULT_INTERNAL_EXECUTOR)
        .whenCompleteAsync(failure, DEFAULT_INTERNAL_EXECUTOR);
  }

  // submit()

  @Override
  public CompletableFuture<Void> submit() {
    return submit(DEFAULT_ON_SUCCESS);
  }

  @Override
  public CompletableFuture<Void> submit(Runnable success) {
    return submit(success, DEFAULT_ON_FAILURE);
  }

  @Override
  public CompletableFuture<Void> submit(Runnable success,
      BiConsumer<Void, ? super Throwable> failure) {
    checkInterCommsInstance();
    return CompletableFuture.runAsync(() -> MESSAGE_QUEUE.add(this.comms), DEFAULT_EXECUTOR)
        .thenRunAsync(success, DEFAULT_EXECUTOR)
        .whenCompleteAsync(failure, DEFAULT_EXECUTOR);
  }

  // complete()

  @Override
  public synchronized void complete() {
    complete(DEFAULT_ON_SUCCESS);
  }

  @Override
  public synchronized void complete(Runnable success) {
    complete(success, DEFAULT_ON_FAILURE);
  }

  @Override
  public synchronized void complete(Runnable success, BiConsumer<Void, ? super Throwable> failure) {
    checkInterCommsInstance();
    MESSAGE_QUEUE.add(this.comms);
    success.run();
    failure.accept(null, null);
  }

  private synchronized void checkInterCommsInstance() {
    if (comms == null) {
      List<Class<?>> tmp = this.recipients.stream().collect(Collectors.toList());
      this.comms = new InterClassComms();
      this.comms.setMessage(message);
      this.comms.setSender(sender);
      this.comms.setRecipients(tmp);
    }
  }
}
