package de.mediathekview.mserver.progress.listeners;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mserver.progress.CrawlerProgress;
import de.mediathekview.mserver.progress.CrawlerProgressListener;
import de.mediathekview.mserver.messages.ServerMessages;

/**
 * A abstract message listner which consumes crawler progress an generates messages from it.
 */
public abstract class AbstractMessageListener implements CrawlerProgressListener
{

    @Override
    public void updateCrawlerProgess(Sender aSender, final CrawlerProgress aCrawlerProgress)
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
