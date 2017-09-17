package de.mediathekview.mserver.crawler.ard.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdSendungBasicInformation;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlsDTO;

/**
 * Recursively crawls the ARD overview pages.
 */
public abstract class AbstractArdOverviewPageCrawlerTask
        extends AbstractUrlTask<ArdSendungBasicInformation, CrawlerUrlsDTO>
{
    private static final long serialVersionUID = -7890265200149231518L;
    static final String SELECTOR_MEDIA_LINK = "a.mediaLink[href^=/tv/]";
    private static final String ATTR_HREF = "href";

    AbstractArdOverviewPageCrawlerTask(final AbstractCrawler aCrawler,
            final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected abstract AbstractArdOverviewPageCrawlerTask
            createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlsDTO> aURLsToCrawl);

    @Override
    protected abstract void processDocument(final CrawlerUrlsDTO aUrlDTO, final Document aDocument);

    String elementToSendungUrl(final Element aElement)
    {
        final String sendungUrl = aElement.attr(ATTR_HREF);
        return ArdCrawler.ARD_BASE_URL + sendungUrl;
    }

}
