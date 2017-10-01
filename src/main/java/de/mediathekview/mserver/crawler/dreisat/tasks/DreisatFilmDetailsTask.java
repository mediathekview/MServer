package de.mediathekview.mserver.crawler.dreisat.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlsDTO;

public class DreisatFilmDetailsTask extends AbstractUrlTask<Film, CrawlerUrlsDTO> {
  private static final long serialVersionUID = -7520416794362009338L;

  public DreisatFilmDetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlsDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlsDTO> aURLsToCrawl) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void processDocument(final CrawlerUrlsDTO aUrlDTO, final Document aDocument) {
    // TODO Auto-generated method stub

  }

}
