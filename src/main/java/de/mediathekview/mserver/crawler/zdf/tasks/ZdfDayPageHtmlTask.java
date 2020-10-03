package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.parser.ZdfDayPageHtmlDeserializer;
import org.jsoup.nodes.Document;

import java.util.Queue;

public class ZdfDayPageHtmlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final transient ZdfDayPageHtmlDeserializer deserializer;
  private final String apiUrlBase;

  public ZdfDayPageHtmlTask(
      final String apiUrlBase,
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDTOs, jsoupConnection);
    this.apiUrlBase = apiUrlBase;
    deserializer = new ZdfDayPageHtmlDeserializer(apiUrlBase);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    taskResults.addAll(deserializer.deserialize(aDocument));
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfDayPageHtmlTask(apiUrlBase, crawler, aElementsToProcess, getJsoupConnection());
  }
}
