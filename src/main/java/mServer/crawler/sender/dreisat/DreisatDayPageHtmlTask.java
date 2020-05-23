package mServer.crawler.sender.dreisat;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import org.jsoup.nodes.Document;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DreisatDayPageHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient DreisatDayPageHtmlDeserializer deserializer;
  private final String apiUrlBase;
  private final transient JsoupConnection jsoupConnection;

  public DreisatDayPageHtmlTask(
    final String apiUrlBase,
    final MediathekReader crawler,
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs,
    final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDTOs);
    this.apiUrlBase = apiUrlBase;
    deserializer = new DreisatDayPageHtmlDeserializer(apiUrlBase);
    this.jsoupConnection = jsoupConnection;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    taskResults.addAll(deserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
    ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new DreisatDayPageHtmlTask(apiUrlBase, crawler, aElementsToProcess, jsoupConnection);
  }
}
