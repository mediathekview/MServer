package de.mediathekview.mserver.base.progress;

import java.util.ArrayList;
import java.util.Collection;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public abstract class AbstractManager
{
    protected final Collection<SenderProgressListener> progressListeners;
    protected final Collection<MessageListener> messageListeners;

    public AbstractManager()
    {
        progressListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
    }

    public boolean addSenderProgressListener(final SenderProgressListener aCrawlerSenderProgressListener)
    {
        return progressListeners.add(aCrawlerSenderProgressListener);
    }

    public boolean addMessageListener(final MessageListener aMessageListener)
    {
        return messageListeners.add(aMessageListener);
    }

    public boolean addAllProgressListener(final Collection<? extends SenderProgressListener> c)
    {
        return progressListeners.addAll(c);
    }

    public boolean addAllMessageListener(final Collection<? extends MessageListener> c)
    {
        return messageListeners.addAll(c);
    }
}
