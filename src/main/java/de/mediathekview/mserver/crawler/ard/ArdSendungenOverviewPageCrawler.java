package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import org.jsoup.nodes.Document;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * Recursively crawls the ARD Sendungen overview pages.
 */
public class ArdSendungenOverviewPageCrawler extends AbstractArdOverviewPageCrawlerTask
{


    public ArdSendungenOverviewPageCrawler(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected AbstractArdOverviewPageCrawlerTask createNewOwnInstance()
    {
        return new ArdSendungenOverviewPageCrawler(crawler,urlsToCrawl);
    }

    @Override
    protected RecursiveTask<LinkedHashSet<Film>> createTask(final ConcurrentLinkedQueue<String> aUrlsToCrawl, Map<String,String> aUrlsSendezeitenMap)
    {
        return new ArdSendungsfolgenOverviewPageCrawler(crawler,aUrlsToCrawl);
    }
}
