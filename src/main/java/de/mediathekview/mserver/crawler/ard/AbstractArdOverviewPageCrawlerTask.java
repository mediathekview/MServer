package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mserver.crawler.AbstractUrlTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.image.ImageWatched;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recursively crawls the ARD overview pages.
 */
public abstract class AbstractArdOverviewPageCrawlerTask extends AbstractUrlTask
{
    private static final Logger LOG = LogManager.getLogger(AbstractArdOverviewPageCrawlerTask.class);
    private static final String SELECTOR_MEDIA_LINK = "a.mediaLink[href^=/tv/]";
    private static final String ATTR_HREF = "href";
    private static final String SELECTOR_DATE = ".date";
    private static final String SELECTOR_ENTRY = ".entry";
    private static final String URL_PART_SENDUNG_VERPASST = "sendungVerpasst";
    private static final String SELECTOR_TEXT_LINK = ".textLink[href^=/tv/]";
    private static final String URL_PART_SENDUNG = "/Sendung?";
    private static final String SELECTOR_DACHZEILE = ".dachzeile";
    private static final String TIME_REGEX_PATTERN = "\\d{2}:\\d{2}";
    private static final String SELECTOR_SUB_PAGES = "div.controls.paging div.entry > a[href~=.*\\.(10|[2-9])\\b]";


    public AbstractArdOverviewPageCrawlerTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }


    protected abstract AbstractArdOverviewPageCrawlerTask createNewOwnInstance();

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
        }else if(aUrl.contains(URL_PART_SENDUNG)){
            findSubPages(aDocument);

            final Elements textLinkElements = aDocument.select(SELECTOR_TEXT_LINK);
            for(Element element : textLinkElements)
            {
                String url = elementToSendungUrl(element);
                sendungUrls.add(url);
                String sendezeitAsText = getSendezeitFromDachzeile(element.select(SELECTOR_DACHZEILE).text());
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
        crawledFilms.addAll(createTask(sendungUrls,urlsSendezeitenMap).invoke());


    }

    private void findSubPages(final Document aDocument)
    {
        LinkedHashSet<String> subPages = new LinkedHashSet<>();
        final Elements elements = aDocument.select(SELECTOR_SUB_PAGES);
        for(Element element : elements)
        {
            String url = elementToSendungUrl(element);
            subPages.add(url);
        }
        this.urlsToCrawl.addAll(subPages);
    }

    private String getSendezeitFromDachzeile(final String aDachzeileValue)
    {
        Matcher matcher = Pattern.compile(TIME_REGEX_PATTERN).matcher(aDachzeileValue);
        if(matcher.find())
        {
            return matcher.group();
        }else {
            return "";
        }
    }

    private String elementToSendungUrl(final Element aElement)
    {
        String sendungUrl = aElement.attr(ATTR_HREF);
        return ArdCrawler.ARD_BASE_URL + sendungUrl;
    }


    protected abstract RecursiveTask<LinkedHashSet<Film>> createTask(final ConcurrentLinkedQueue<String> aUrlsToCrawl,ConcurrentHashMap<String,String> aUrlsSendezeitenMap);
}
