package de.mediathekview.mserver.crawler.hr.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class HrSendungsfolgenOverviewPageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public HrSendungsfolgenOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    // TODO Auto-generated method stub

  }

}
