package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrDayPageUrlDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class MdrDayPageUrlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private int maxDaysPast;

  public MdrDayPageUrlTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, int aMaxDaysPast) {
    super(aCrawler, aUrlToCrawlDtos);
    maxDaysPast = aMaxDaysPast;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {

    MdrDayPageUrlDeserializer dayPageUrlDeserializer = new MdrDayPageUrlDeserializer(MdrConstants.URL_BASE, maxDaysPast);
    taskResults.addAll(dayPageUrlDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrDayPageUrlTask(crawler, aElementsToProcess, maxDaysPast);
  }
}
