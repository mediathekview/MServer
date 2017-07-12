package mServer.messages;

import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;

/**
 * Created by nicklas on 12.07.17.
 */
public enum ServerMessages implements Message
{
    CRAWLER_START("crawlerStart", MessageTypes.INFO),
    CRAWLER_PROGRESS("crawlerProgress", MessageTypes.INFO),
    CRAWLER_END("crawlerEnd", MessageTypes.INFO),
    CRAWLER_ERROR("crawlerError", MessageTypes.ERROR);

    private String messageKey;
    private MessageTypes messageType;

    ServerMessages(String aMessageKey, MessageTypes aMessageType)
    {
        messageKey = aMessageKey;
        messageType = aMessageType;
    }

    @Override
    public String getMessageKey()
    {
        return messageKey;
    }

    @Override
    public MessageTypes getMessageType()
    {
        return messageType;
    }
}
