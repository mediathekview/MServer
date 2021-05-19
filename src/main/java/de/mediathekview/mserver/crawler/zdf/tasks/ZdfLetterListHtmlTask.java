package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.parser.ZdfLetterListHtmlDeserializer;
import org.jsoup.nodes.Document;

import java.util.Queue;

public class ZdfLetterListHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfLetterListHtmlDeserializer letterListDeserializer;

  public ZdfLetterListHtmlTask(
      final AbstractCrawler crawler, final Queue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
    letterListDeserializer = new ZdfLetterListHtmlDeserializer();
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    taskResults.addAll(letterListDeserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfLetterListHtmlTask(crawler, aElementsToProcess);
  }
}
