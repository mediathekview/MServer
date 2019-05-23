package de.mediathekview.mserver.crawler.rbb.tasks;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class RbbTopicsOverviewTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String SELECTOR_TOPIC_ENTRY =
      "div.elementWrapper div.teaser > div.textWrapper > a.textLink";

  public RbbTopicsOverviewTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    final Elements topics = aDocument.select(SELECTOR_TOPIC_ENTRY);
    topics.forEach(
        topic -> {
          final String url = topic.attr(ATTRIBUTE_HREF);
          taskResults.add(
              new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE)));
        });
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new RbbTopicsOverviewTask(crawler, aElementsToProcess);
  }
}
