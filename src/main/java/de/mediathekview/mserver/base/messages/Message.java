package de.mediathekview.mserver.base.messages;

/**
 * Represents a message with it's key and it's type.
 */
public interface Message
{
    String getMessageKey();
    MessageTypes getMessageType();
}
