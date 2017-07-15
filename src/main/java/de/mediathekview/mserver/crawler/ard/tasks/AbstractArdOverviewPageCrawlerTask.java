package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mserver.crawler.AbstractUrlTask;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * Recursively crawls the ARD overview pages.
 */
public abstract class AbstractArdOverviewPageCrawlerTask extends AbstractUrlTask<RecursiveTask<LinkedHashSet<Film>>>
{
    static final String SELECTOR_MEDIA_LINK = "a.mediaLink[href^=/tv/]";
    private static final String ATTR_HREF = "href";

    AbstractArdOverviewPageCrawlerTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }


    protected abstract AbstractArdOverviewPageCrawlerTask createNewOwnInstance();

    @Override
    protected abstract void processDocument(final String aUrl, final Document aDocument);


    String elementToSendungUrl(final Element aElement)
    {
        String sendungUrl = aElement.attr(ATTR_HREF);
        return ArdCrawler.ARD_BASE_URL + sendungUrl;
    }

}
