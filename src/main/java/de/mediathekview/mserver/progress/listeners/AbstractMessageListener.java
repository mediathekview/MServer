package de.mediathekview.mserver.progress.listeners;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mlib.progress.ProgressListener;
import de.mediathekview.mlib.progress.SenderProgressListener;
import de.mediathekview.mserver.base.messages.ServerMessages;

/**
 * A abstract message listner which consumes crawler progress and generates messages from it.
 */
public abstract class AbstractMessageListener implements SenderProgressListener
{


    @Override
    public void updateProgess(Sender aSender,Progress aCrawlerProgress)
    {
        newMessage(String.format(MessageUtil.getInstance().loadMessageText(ServerMessages.CRAWLER_PROGRESS),
                aSender.getName(),
                aCrawlerProgress.calcProgressInPercent(),
                aCrawlerProgress.calcProgressErrorQuoteInPercent(),
                aCrawlerProgress.getActualCount(),
                aCrawlerProgress.getMaxCount(),
                aCrawlerProgress.getErrorCount(),
                aCrawlerProgress.calcActualErrorQuoteInPercent()));
    }

    abstract void newMessage(String aMessage);
}
