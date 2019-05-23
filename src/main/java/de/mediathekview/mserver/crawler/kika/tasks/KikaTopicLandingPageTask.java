package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaTopicLandingPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String SELECTOR_TOPIC_OVERVIEW1 =
      ".sectionArticleWrapperRight span.moreBtn > a";
  private static final String SELECTOR_TOPIC_OVERVIEW2 = "div.teaserMultiGroup > a.linkAll";

  private final String baseUrl;

  public KikaTopicLandingPageTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDTOs);
    baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    Elements overviewUrlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW1);
    parseOverviewLink(overviewUrlElements);

    overviewUrlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW2);
    parseOverviewLink(overviewUrlElements);
  }

  private void parseOverviewLink(final Elements overviewUrlElements) {
    for (final Element overviewUrlElement : overviewUrlElements) {
      final String url = overviewUrlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaTopicLandingPageTask(crawler, aElementsToProcess, baseUrl);
  }
}
