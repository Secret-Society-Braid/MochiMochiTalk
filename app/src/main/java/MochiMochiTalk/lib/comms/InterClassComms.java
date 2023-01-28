package MochiMochiTalk.lib.comms;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

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

  @Nonnull
  @Override
  public synchronized Class<?> getSender() throws IllegalStateException {
    if (closed) {
      return this.sender;
    } else {
      throw new IllegalStateException("cannot invoke this method before sending.");
    }
  }

  @Override
  public synchronized void setSender(Class<?> sender) throws UnsupportedOperationException {
    if (sender != null) {
      this.sender = sender;
    } else {
      throw new UnsupportedOperationException("Sender already set");
    }
  }

  @Nonnull
  @Override
  public synchronized Class<?> getSendTo()
      throws IllegalStateException, UnsupportedOperationException {
    if (closed) {
      return this.recipients.get(0);
    } else if (recipients == null) {
      throw new UnsupportedOperationException(
          "cannot invoke this method because this has no data.");
    } else {
      throw new IllegalStateException("cannot invoke this method before sending.");
    }
  }

  @Override
  public synchronized void setSendTo(Class<?> to) throws IllegalStateException {
    if (!closed) {
      this.recipients.add(to);
    } else {
      throw new IllegalStateException("cannot invoke this method after sending");
    }
  }

  @Nonnull
  @Override
  public synchronized Collection<Class<?>> getRecipients()
      throws IllegalStateException, UnsupportedOperationException {
    if (closed) {
      return this.recipients;
    } else if (recipients == null) {
      throw new UnsupportedOperationException("cannot invoke this method because this has no data");
    } else {
      throw new IllegalStateException("cannot invoke this method before sending");
    }
  }

  @Override
  public synchronized void setRecipients(Collection<Class<?>> recipients)
      throws IllegalStateException {
    if (closed) {
      throw new IllegalStateException("cannot invoke this method after sending");
    } else {
      this.recipients = recipients.parallelStream().collect(Collectors.toList());
    }
  }

  @Nonnull
  @Override
  public synchronized String getMessage()
      throws IllegalStateException, UnsupportedOperationException {
    if (closed) {
      return this.message;
    } else if (Strings.isNullOrEmpty(this.message)) {
      throw new UnsupportedOperationException("cannot invoke this method because this has no data");
    } else {
      throw new IllegalStateException("cannot invoke this method before sending");
    }
  }

  @Override
  public synchronized void setMessage(String message) throws IllegalStateException {
    if (closed) {
      throw new IllegalStateException("cannot invoke this method after sending");
    }
    this.message = message;
  }

  @Override
  public synchronized CommunicationAction sendMessage()
      throws IllegalStateException, UnsupportedOperationException {
    if (closed) {
      throw new IllegalStateException("cannot invoke this method after sending");
    } else if (Strings.isNullOrEmpty(this.message)) {
      throw new UnsupportedOperationException("cannot invoke this method because this has no data");
    } else {
      return new CommunicationActionImpl(this);
    }
  }

  @Override
  public synchronized CommunicationAction sendMessage(String message)
      throws IllegalStateException, UnsupportedOperationException {
    if (closed) {
      throw new IllegalStateException("cannot invoke this method after sending");
    } else if (!Strings.isNullOrEmpty(this.message)) {
      throw new UnsupportedOperationException(
          "cannot invoke this method because the message is already set.");
    } else {
      this.message = message;
    }
    return sendMessage();
  }

  @Nonnull
  @Override
  public synchronized boolean isClosed() {
    return this.closed;
  }
}
