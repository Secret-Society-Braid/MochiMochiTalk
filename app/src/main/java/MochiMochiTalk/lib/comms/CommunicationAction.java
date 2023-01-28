package MochiMochiTalk.lib.comms;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface CommunicationAction {

  void queue();

  void queue(Runnable success);

  void queue(Runnable success, BiConsumer<Void, ? super Throwable> failure);

  CompletableFuture<Void> submit();

  CompletableFuture<Void> submit(Runnable success);

  CompletableFuture<Void> submit(Runnable success, BiConsumer<Void, ? super Throwable> failure);

  void complete();

  void complete(Runnable success);

  void complete(Runnable success, BiConsumer<Void, ? super Throwable> failure);

}
