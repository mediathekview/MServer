package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class SrfSendungOverviewPageTask extends AbstractRestTask {
  // id, month-year, number of pages to search
  private static final String URL = "https://www.srf.ch/play/v2/tv/show/{0}/latestEpisodes/tillMonth/{1}?numberOfEpisodes=[2]&layout=json";

  public SrfSendungOverviewPageTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.empty());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected AbstractUrlTask createNewOwnInstance(ConcurrentLinkedQueue aURLsToCrawl) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
