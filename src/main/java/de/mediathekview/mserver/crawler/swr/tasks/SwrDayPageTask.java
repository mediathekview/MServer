package de.mediathekview.mserver.crawler.swr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import de.mediathekview.mserver.crawler.swr.parser.SwrDayPageDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class SwrDayPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private String baseUrl;

  public SwrDayPageTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDTOs);
    this.baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    SwrDayPageDeserializer dayPageUrlDeserializer = new SwrDayPageDeserializer(SwrConstants.URL_BASE);
    taskResults.addAll(dayPageUrlDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new SwrDayPageTask(crawler, aElementsToProcess, baseUrl);
  }
}
