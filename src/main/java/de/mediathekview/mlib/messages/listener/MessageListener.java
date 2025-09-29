package de.mediathekview.mlib.messages.listener;
import de.mediathekview.mlib.messages.Message;

/**
 * Defines a listener which consumes messages.
 */
public interface MessageListener
{
    void consumeMessage(Message aMessage, Object... args);
}
