package mServer.crawler.sender.zdf.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.zdf.parser.ZdfDayPageHtmlDeserializer;
import org.jsoup.nodes.Document;

public class ZdfDayPageHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfDayPageHtmlDeserializer deserializer;
  private final String apiUrlBase;
  private final transient JsoupConnection jsoupConnection;

  public ZdfDayPageHtmlTask(
          final String apiUrlBase,
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs,
          final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDTOs);
    this.apiUrlBase = apiUrlBase;
    deserializer = new ZdfDayPageHtmlDeserializer(apiUrlBase);
    this.jsoupConnection = jsoupConnection;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    taskResults.addAll(deserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfDayPageHtmlTask(apiUrlBase, crawler, aElementsToProcess, jsoupConnection);
  }
}
