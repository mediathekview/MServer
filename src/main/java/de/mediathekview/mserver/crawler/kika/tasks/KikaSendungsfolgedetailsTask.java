package de.mediathekview.mserver.crawler.kika.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  public KikaSendungsfolgedetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaSendungsfolgedetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    // Sendungsfolge:http://www.kika.de/durch-die-wildnis-zwei/sendungen/sendung75574.html
    // Videodetails:
    // https://www.kika.de/durch-die-wildnis-zwei/sendungen/videos/video30772-avCustom.xml
    // TODO read the video url from the Sendungsfolgen page.
    // TODO read all needed Information from XML page.
    // TODO Auto-generated method stub
  }
}
