package de.mediathekview.mserver.base.progress;

import de.mediathekview.mserver.base.messages.Message;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractManager {
  protected final Collection<SenderProgressListener> progressListeners;
  protected final Collection<MessageListener> messageListeners;

  protected AbstractManager() {
    progressListeners = new ArrayList<>();
    messageListeners = new ArrayList<>();
  }

  public boolean addMessageListener(final MessageListener aMessageListener) {
    return messageListeners.add(aMessageListener);
  }

  public boolean addAllProgressListener(final Collection<? extends SenderProgressListener> c) {
    return progressListeners.addAll(c);
  }

  public boolean addAllMessageListener(final Collection<? extends MessageListener> c) {
    return messageListeners.addAll(c);
  }

  protected void printMessage(final Message aMessage, final Object... args) {
    messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
  }
}
