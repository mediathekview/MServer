package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrDayPageDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class MdrDayPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public MdrDayPageTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {

    MdrDayPageDeserializer dayPageUrlDeserializer = new MdrDayPageDeserializer(MdrConstants.URL_BASE);
    taskResults.addAll(dayPageUrlDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrDayPageTask(crawler, aElementsToProcess);
  }
}
