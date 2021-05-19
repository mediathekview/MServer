package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.parser.ZdfTopicPageHtmlDeserializer;
import org.jsoup.nodes.Document;

import java.util.Queue;

public class ZdfTopicPageHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfTopicPageHtmlDeserializer topicsDeserializer;

  public ZdfTopicPageHtmlTask(
      final AbstractCrawler crawler, final Queue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
    topicsDeserializer = new ZdfTopicPageHtmlDeserializer(ZdfConstants.URL_API_BASE);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    taskResults.addAll(topicsDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfTopicPageHtmlTask(crawler, aElementsToProcess);
  }
}
