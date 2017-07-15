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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recursively crawls the ARD Sendungsfolgen overview pages.
 */
public class ArdSendungsfolgenOverviewPageCrawler extends AbstractArdOverviewPageCrawlerTask
{
    private static final String SELECTOR_TEXT_LINK = ".textLink[href^=/tv/]";
    private static final String SELECTOR_DACHZEILE = ".dachzeile";
    private static final String TIME_REGEX_PATTERN = "\\d{2}:\\d{2}";
    //private static final String SELECTOR_SUB_PAGES = "div.controls.paging div.entry > a[href~=.*\\.(10|[2-9])\\b]";
    private static final String SELECTOR_SUB_PAGES = "div.controls.paging div.entry > a[href~=.*\\.[2-3]\\b]";

    public ArdSendungsfolgenOverviewPageCrawler(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected AbstractArdOverviewPageCrawlerTask createNewOwnInstance()
    {
        return new ArdSendungsfolgenOverviewPageCrawler(crawler, urlsToCrawl);
    }

    @Override
    protected void processDocument(final String aUrl, final Document aDocument)
    {
        ConcurrentHashMap<String, String> urlsSendezeitenMap = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<String> sendungUrls = new ConcurrentLinkedQueue<>();
        findSubPages(aDocument);

        final Elements textLinkElements = aDocument.select(SELECTOR_TEXT_LINK);
        for (Element element : textLinkElements)
        {
            String url = elementToSendungUrl(element);
            sendungUrls.add(url);
            crawler.incrementAndGetMaxCount();
            String sendezeitAsText = getSendezeitFromDachzeile(element.select(SELECTOR_DACHZEILE).text());
            urlsSendezeitenMap.put(url, sendezeitAsText);
        }
        crawler.updateProgress();
        filmTasks.add(createTask(sendungUrls, urlsSendezeitenMap));


    }

    private void findSubPages(final Document aDocument)
    {
        LinkedHashSet<String> subPages = new LinkedHashSet<>();
        final Elements elements = aDocument.select(SELECTOR_SUB_PAGES);
        for (Element element : elements)
        {
            String url = elementToSendungUrl(element);
            subPages.add(url);
        }
        this.urlsToCrawl.addAll(subPages);
    }

    private String getSendezeitFromDachzeile(final String aDachzeileValue)
    {
        Matcher matcher = Pattern.compile(TIME_REGEX_PATTERN).matcher(aDachzeileValue);
        if (matcher.find())
        {
            return matcher.group();
        } else
        {
            return "";
        }
    }

    private RecursiveTask<LinkedHashSet<Film>> createTask(final ConcurrentLinkedQueue<String> aUrlsToCrawl, ConcurrentHashMap<String, String> aUrlsSendezeitenMap)
    {
        return new ArdSendungTask(crawler, aUrlsToCrawl, aUrlsSendezeitenMap);
    }

}