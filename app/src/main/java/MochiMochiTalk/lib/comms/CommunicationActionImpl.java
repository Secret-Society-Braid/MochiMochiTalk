package MochiMochiTalk.lib.comms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import MochiMochiTalk.lib.WorkerThreadFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommunicationActionImpl implements CommunicationAction {

    private InterClassComms comms;
    
    private String message;

    private Class<?> sender;

    private Collection<Class<?>> recipients;

    private static final List<InterClassComms> MESSAGE_QUEUE = new ArrayList<>(); 

    private static final int DEFAULT_CONCURRENCY = 16;

    private static final ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(DEFAULT_CONCURRENCY, new WorkerThreadFactory(() -> "MochiMochiTalk", "InterClassCommunication Thread"));

    private static final ExecutorService DEFAULT_INTERNAL_EXECUTOR = Executors.newCachedThreadPool(new WorkerThreadFactory(() -> "MochiMochiTalk", "CommunicationAction Internal Process Thread"));

    public CommunicationActionImpl(InterClassComms comms) {
        this.comms = comms;
    }

    private CommunicationActionImpl(String message, Class<?> sender, Collection<Class<?>> recipients) {
        this.message = message;
        this.sender = sender;
        this.recipients = recipients;
    }

    @Override
    public synchronized void queue() {
        checkInterCommsInstance();
        CompletableFuture.runAsync(() -> MESSAGE_QUEUE.add(this.comms), DEFAULT_INTERNAL_EXECUTOR);
    }

    @Override
    public synchronized void queue(boolean acceptInterruptIfNeeded) {
        
    }

    private synchronized void checkInterCommsInstance() {
        if(comms == null) {
            List<Class<?>> tmp = this.recipients.stream().collect(Collectors.toList());
            this.comms = new InterClassComms();
            this.comms.setMessage(message);
            this.comms.setSender(sender);
            this.comms.setRecipients(tmp);
        }
    }
}
