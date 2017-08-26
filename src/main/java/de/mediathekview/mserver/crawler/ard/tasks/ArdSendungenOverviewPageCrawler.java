package de.mediathekview.mserver.crawler.ard.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.mediathekview.mserver.crawler.ard.ArdSendungBasicInformation;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlsDTO;

/**
 * Recursively crawls the ARD Sendungen overview pages.
 */
public class ArdSendungenOverviewPageCrawler extends AbstractArdOverviewPageCrawlerTask
{
    private static final long serialVersionUID = -328270165305498249L;
    private static final String URL_PART_SENDUNG_VERPASST = "sendungVerpasst";
    private static final String SELECTOR_DATE = ".date";
    private static final String SELECTOR_ENTRY = ".entry";

    public ArdSendungenOverviewPageCrawler(final AbstractCrawler aCrawler,
            final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected void processDocument(final CrawlerUrlsDTO aUrlDTO, final Document aDocument)
    {
        if (aUrlDTO.getUrl().contains(URL_PART_SENDUNG_VERPASST))
        {
            final Elements entryElements = aDocument.select(SELECTOR_ENTRY);
            for (final Element element : entryElements)
            {
                final String url = elementToSendungUrl(element.select(SELECTOR_MEDIA_LINK).first());
                final String sendezeitAsText = element.select(SELECTOR_DATE).text();
                taskResults.add(new ArdSendungBasicInformation(url, sendezeitAsText));
            }
        }
        else
        {
            final ConcurrentLinkedQueue<CrawlerUrlsDTO> sendungUrls = new ConcurrentLinkedQueue<>();
            final Elements elements = aDocument.select(SELECTOR_MEDIA_LINK);
            for (final Element mediaLinkElement : elements)
            {
                sendungUrls.add(new CrawlerUrlsDTO(elementToSendungUrl(mediaLinkElement)));
            }
            taskResults.addAll(createTask(sendungUrls).invoke());
        }
        crawler.updateProgress();

    }

    @Override
    protected AbstractArdOverviewPageCrawlerTask createNewOwnInstance()
    {
        return new ArdSendungenOverviewPageCrawler(crawler, urlsToCrawl);
    }

    private AbstractArdOverviewPageCrawlerTask createTask(final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlsToCrawl)
    {
        return new ArdSendungsfolgenOverviewPageCrawler(crawler, aUrlsToCrawl);
    }
}
