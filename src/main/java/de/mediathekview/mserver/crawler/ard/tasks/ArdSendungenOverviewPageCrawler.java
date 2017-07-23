package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.AbstractCrawler;
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

    public ArdSendungenOverviewPageCrawler(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected void processDocument(final String aUrl, final Document aDocument)
    {
        ConcurrentHashMap<String,String> urlsSendezeitenMap = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<String> sendungUrls = new ConcurrentLinkedQueue<>();
        if(aUrl.contains(URL_PART_SENDUNG_VERPASST))
        {
            final Elements entryElements = aDocument.select(SELECTOR_ENTRY);
            for(Element element : entryElements)
            {
                String url = elementToSendungUrl(element.select(SELECTOR_MEDIA_LINK).first());
                sendungUrls.add(url);
                String sendezeitAsText = element.select(SELECTOR_DATE).text();
                urlsSendezeitenMap.put(url,sendezeitAsText);
            }
        }else
        {
            Elements elements = aDocument.select(SELECTOR_MEDIA_LINK);
            for (Element mediaLinkElement : elements)
            {
                sendungUrls.add(elementToSendungUrl(mediaLinkElement));
            }
        }
        crawler.updateProgress();
        taskResults.addAll(createTask(sendungUrls,urlsSendezeitenMap).invoke());


    }

    @Override
    protected AbstractArdOverviewPageCrawlerTask createNewOwnInstance()
    {
        return new ArdSendungenOverviewPageCrawler(crawler,urlsToCrawl);
    }

    private RecursiveTask<LinkedHashSet<RecursiveTask<LinkedHashSet<Film>>>> createTask(final ConcurrentLinkedQueue<String> aUrlsToCrawl,ConcurrentHashMap<String,String> aUrlsSendezeitenMap)
    {
        return new ArdSendungsfolgenOverviewPageCrawler(crawler,aUrlsToCrawl);
    }
}
