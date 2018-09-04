package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrLetterPageDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class MdrLetterPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public MdrLetterPageTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {

    MdrLetterPageDeserializer letterPageDeserializer = new MdrLetterPageDeserializer(MdrConstants.URL_BASE);
    taskResults.addAll(letterPageDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrLetterPageTask(crawler, aElementsToProcess);
  }
}
