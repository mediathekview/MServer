package de.mediathekview.mserver.crawler.ard.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.mediathekview.mserver.crawler.ard.ArdSendungBasicInformation;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlsDTO;

/**
 * Recursively crawls the ARD Sendungsfolgen overview pages.
 */
public class ArdSendungsfolgenOverviewPageCrawler extends AbstractArdOverviewPageCrawlerTask
{
    private static final long serialVersionUID = 4963668352629743584L;
    private static final String SELECTOR_TEXT_LINK = ".textLink[href^=/tv/]";
    private static final String SELECTOR_DACHZEILE = ".dachzeile";
    private static final String TIME_REGEX_PATTERN = "\\d{2}:\\d{2}";
    private static final String SELECTOR_SUB_PAGES_PATTERN = "div.controls.paging div.entry > a[href~=.*\\.[%s]\\b]";
    private static final String SELECTOR_SUB_PAGES_SEPPERATOR = "-";
    private static final String SUBPAGE_URL_PART = "mcontents=page.";
    private static final int FIRST_SUBPAGE_ID = 2;

    public ArdSendungsfolgenOverviewPageCrawler(final AbstractCrawler aCrawler,
            final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlsToCrawl)
    {
        super(aCrawler, aUrlsToCrawl);
    }

    @Override
    protected AbstractArdOverviewPageCrawlerTask createNewOwnInstance()
    {
        return new ArdSendungsfolgenOverviewPageCrawler(crawler, urlsToCrawl);
    }

    @Override
    protected void processDocument(final CrawlerUrlsDTO aUrlDTO, final Document aDocument)
    {
        ArdSendungsfolgenOverviewPageCrawler subpageCrawler = null;
        if (!aUrlDTO.getUrl().contains(SUBPAGE_URL_PART) && config.getMaximumSubpages() > 0)
        {
            subpageCrawler = findSubPages(aDocument);
            subpageCrawler.fork();
        }

        final Elements textLinkElements = aDocument.select(SELECTOR_TEXT_LINK);
        for (final Element element : textLinkElements)
        {
            final String url = elementToSendungUrl(element);
            crawler.incrementAndGetMaxCount();
            final String sendezeitAsText = getSendezeitFromDachzeile(element.select(SELECTOR_DACHZEILE).text());
            taskResults.add(new ArdSendungBasicInformation(url, sendezeitAsText));
        }

        if (subpageCrawler != null)
        {
            taskResults.addAll(subpageCrawler.join());
        }

        crawler.updateProgress();

    }

    private ArdSendungsfolgenOverviewPageCrawler findSubPages(final Document aDocument)
    {
        final ConcurrentLinkedQueue<CrawlerUrlsDTO> subPages = new ConcurrentLinkedQueue<>();
        final Elements elements = aDocument.select(getSelectorSubPages());
        for (final Element element : elements)
        {
            final String url = elementToSendungUrl(element);
            subPages.add(new CrawlerUrlsDTO(url));
        }
        return new ArdSendungsfolgenOverviewPageCrawler(crawler, subPages);
    }

    private String getSendezeitFromDachzeile(final String aDachzeileValue)
    {
        final Matcher matcher = Pattern.compile(TIME_REGEX_PATTERN).matcher(aDachzeileValue);
        if (matcher.find())
        {
            return matcher.group();
        }
        else
        {
            return "";
        }
    }

    private String getSelectorSubPages()
    {
        String subPageIndicator;
        if (config.getMaximumSubpages() == 1)
        {
            subPageIndicator = Integer.toString(FIRST_SUBPAGE_ID);
        }
        else
        {
            subPageIndicator = new StringBuilder().append(FIRST_SUBPAGE_ID).append(SELECTOR_SUB_PAGES_SEPPERATOR)
                    .append(2 + config.getMaximumSubpages()).toString();
        }

        return String.format(SELECTOR_SUB_PAGES_PATTERN, subPageIndicator);
    }
}