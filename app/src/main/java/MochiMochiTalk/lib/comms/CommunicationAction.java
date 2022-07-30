package MochiMochiTalk.lib.comms;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface CommunicationAction {

    void queue();

    void queue(boolean acceptInterruptIfneeded);

    void queue(Runnable success);

    void queue(Runnable success, Consumer<? super Throwable> failure);

    void queue(Runnable success, Consumer<? super Throwable> failure, boolean acceptInterruptIfNeeded);
    
    CompletableFuture<Void> submit();

    CompletableFuture<Void> submit(boolean acceptInterruptIfNeeded);

    CompletableFuture<Void> submit(Runnable success);

    CompletableFuture<Void> submit(Runnable success, Consumer<? super Throwable> failure);

    CompletableFuture<Void> submit(Runnable success, Consumer<? super Throwable> failure, boolean acceptInterruptIfNeeded);
}
