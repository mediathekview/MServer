package de.mediathekview.mserver.base.messages.listener;
import de.mediathekview.mserver.base.messages.Message;

/**
 * Defines a listener which consumes messages.
 */
public interface MessageListener
{
    void consumeMessage(Message aMessage, Object... args);
}
