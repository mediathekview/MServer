package de.mediathekview.mserver.crawler.swr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import de.mediathekview.mserver.crawler.swr.parser.SwrTopicsDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class SwrTopicsOverviewTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public SwrTopicsOverviewTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    SwrTopicsDeserializer topicsDeserializer = new SwrTopicsDeserializer(SwrConstants.URL_BASE);
    taskResults.addAll(topicsDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new SwrTopicsOverviewTask(crawler, aElementsToProcess);
  }
}
