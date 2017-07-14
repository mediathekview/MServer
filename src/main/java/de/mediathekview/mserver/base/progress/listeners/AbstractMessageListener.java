package de.mediathekview.mserver.base.progress.listeners;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.progress.CrawlerProgress;
import de.mediathekview.mserver.base.progress.CrawlerProgressListener;

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
