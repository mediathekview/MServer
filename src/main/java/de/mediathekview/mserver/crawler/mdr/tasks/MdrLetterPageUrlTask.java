package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrLetterPageUrlDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class MdrLetterPageUrlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public MdrLetterPageUrlTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {

    MdrLetterPageUrlDeserializer letterPageUrlDeserializer = new MdrLetterPageUrlDeserializer(MdrConstants.URL_BASE);
    taskResults.addAll(letterPageUrlDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrLetterPageUrlTask(crawler, aElementsToProcess);
  }
}
