package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mserver.crawler.CrawlerUrlsDTO;
import de.mediathekview.mserver.crawler.ard.ArdSendungBasicInformation;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * Recursively crawls the ARD Sendungen overview pages.
 */
public class ArdSendungenOverviewPageCrawler extends AbstractArdOverviewPageCrawlerTask
{
    private static final String URL_PART_SENDUNG_VERPASST = "sendungVerpasst";
    private static final String SELECTOR_DATE = ".date";
    private static final String SELECTOR_ENTRY = ".entry";

    public ArdSendungenOverviewPageCrawler(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected void processDocument(final CrawlerUrlsDTO aUrlDTO, final Document aDocument)
    {
        if(aUrlDTO.getUrl().contains(URL_PART_SENDUNG_VERPASST))
        {
            final Elements entryElements = aDocument.select(SELECTOR_ENTRY);
            for(Element element : entryElements)
            {
                String url = elementToSendungUrl(element.select(SELECTOR_MEDIA_LINK).first());
                String sendezeitAsText = element.select(SELECTOR_DATE).text();
                taskResults.add(new ArdSendungBasicInformation(url,sendezeitAsText));
            }
        }else
        {
            ConcurrentLinkedQueue<CrawlerUrlsDTO> sendungUrls = new ConcurrentLinkedQueue<>();
            Elements elements = aDocument.select(SELECTOR_MEDIA_LINK);
            for (Element mediaLinkElement : elements)
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
        return new ArdSendungenOverviewPageCrawler(crawler,urlsToCrawl);
    }

    private AbstractArdOverviewPageCrawlerTask createTask(final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlsToCrawl)
    {
        return new ArdSendungsfolgenOverviewPageCrawler(crawler,aUrlsToCrawl);
    }
}
