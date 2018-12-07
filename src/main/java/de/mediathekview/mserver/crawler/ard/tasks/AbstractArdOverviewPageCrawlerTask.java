package de.mediathekview.mserver.crawler.ard.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.mediathekview.mserver.crawler.ard.ArdSendungBasicInformation;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

/**
 * Recursively crawls the ARD overview pages.
 */
public abstract class AbstractArdOverviewPageCrawlerTask
        extends AbstractDocumentTask<ArdSendungBasicInformation, CrawlerUrlDTO>
{
    private static final long serialVersionUID = -7890265200149231518L;
    static final String SELECTOR_MEDIA_LINK = "a.mediaLink[href^=/tv/]";
    private static final String ATTR_HREF = "href";

    AbstractArdOverviewPageCrawlerTask(final AbstractCrawler aCrawler,
            final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected abstract AbstractArdOverviewPageCrawlerTask
            createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl);

    @Override
    protected abstract void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument);

    String elementToSendungUrl(final Element aElement)
    {
        final String sendungUrl = aElement.attr(ATTR_HREF);
        return sendungUrl;
    }

}
