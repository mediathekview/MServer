package mServer.crawler.progress.listeners;

import de.mediathekview.mlib.messages.MessageUtil;
import mServer.crawler.progress.CrawlerProgress;
import mServer.crawler.progress.CrawlerProgressListener;
import mServer.messages.ServerMessages;
import org.apache.logging.log4j.core.jmx.Server;

/**
 * A abstract message listner which consumes crawler progress an generates messages from it.
 */
public abstract class AbstractMessageListener implements CrawlerProgressListener
{

    @Override
    public void updateCrawlerProgess(final CrawlerProgress aCrawlerProgress)
    {
        newMessage(String.format(MessageUtil.getInstance().loadMessageText(ServerMessages.CRAWLER_PROGRESS),
                aCrawlerProgress.calcProgressInPercent(),
                aCrawlerProgress.calcActualErrorQuoteInPercent(),
                aCrawlerProgress.calcProgressErrorQuoteInPercent(),
                aCrawlerProgress.getActualCount(),
                aCrawlerProgress.getErrorCount(),
                aCrawlerProgress.getMaxCount()));
    }

    abstract void newMessage(String aMessage);
}
