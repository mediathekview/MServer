package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mserver.crawler.AbstractUrlTask;
import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Recursively crawls the ARD Sendungsfolge page.
 */
public class ArdSendungTask extends AbstractUrlTask
{

    private final ConcurrentHashMap<String, String> urlsSendezeitenMap;

    public ArdSendungTask(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<String> aUrlsToCrawl, ConcurrentHashMap<String,String> aUrlsSendezeitenMap)
    {
        super(aCrawler, aUrlsToCrawl);
        urlsSendezeitenMap = aUrlsSendezeitenMap;
    }

    @Override
    protected AbstractUrlTask createNewOwnInstance()
    {
        return new ArdSendungTask(crawler, urlsToCrawl,urlsSendezeitenMap);
    }

    @Override
    protected void processDocument(final String aUrl, final Document aDocument)
    {
        String sendezeitAsText = urlsSendezeitenMap.get(aUrl);
        System.out.println(aUrl+" "+sendezeitAsText);
    }

}
