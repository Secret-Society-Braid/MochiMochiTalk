package MochiMochiTalk.lib;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class WorkerThreadFactory implements ThreadFactory {

    private final AtomicInteger count = new AtomicInteger(1);

    private final Supplier<String> identifier;

    private final boolean isDaemon;

    public WorkerThreadFactory(Supplier<String> identifier, String specifier) {
        this(identifier, specifier, true);
    }

    public WorkerThreadFactory(Supplier<String> identifier, String specifier, boolean isDaemon) {
        this.identifier = () -> identifier.get() + " " + specifier;
        this.isDaemon = isDaemon;
    }

    @Nonnull
    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Thread thread = new Thread(r, this.identifier.get() + "-Worker " + count.getAndIncrement());
        thread.setDaemon(this.isDaemon);
        return thread;
    }
}
