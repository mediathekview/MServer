package de.mediathekview.mserver.base.messages;

import de.mediathekview.mserver.base.messages.listener.MessageListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MessageCreator {
  protected final Collection<MessageListener> messageListeners;

  protected MessageCreator() {
    super();
    messageListeners = ConcurrentHashMap.newKeySet();
  }

  protected MessageCreator(final MessageListener... aListeners) {
    this();
    messageListeners.addAll(Arrays.asList(aListeners));
  }

  public boolean addAllMessageListener(final Collection<MessageListener> aMessageListeners) {
    return messageListeners.addAll(aMessageListeners);
  }

  protected void publishMessage(final Message aMessage, final Object... aParams) {
    messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, aParams));
  }
}
