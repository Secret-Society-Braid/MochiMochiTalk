package MochiMochiTalk.lib.comms;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterClassComms implements IClassComms {
    
    private List<Class<?>> recipients;

    private Class<?> sender;
    
    private String message;

    private boolean closed = false;

    public InterClassComms() {
        this(null, null);
    }

    public InterClassComms(Class<?> sender, Class<?> receiver, Class<?>... others) {
        this.sender = sender;
        this.recipients = Lists.asList(receiver, others);
        this.closed = false;
    }

    @Override
    public synchronized void setSender(Class<?> sender) throws UnsupportedOperationException {
        if(sender != null)
            this.sender = sender;
        else
            throw new UnsupportedOperationException("Sender already set");
    }

    @Nonnull
    @Override
    public Class<?> getSender() throws IllegalStateException {
        if(closed)
            return this.sender;
        else
            throw new IllegalStateException("cannot invoke this method before sending.");
    }

    @Override
    public void setSendTo(Class<?> to) throws IllegalStateException {
        if(!closed)
            this.recipients.add(to);
        else
            throw new IllegalStateException("cannot invoke this method after sending");
    }

    
}
