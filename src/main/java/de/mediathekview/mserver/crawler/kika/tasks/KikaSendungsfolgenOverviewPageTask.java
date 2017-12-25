package de.mediathekview.mserver.crawler.kika.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaSendungsfolgenOverviewPageTask extends KikaPagedOverviewPageTask {
  private static final long serialVersionUID = 6547545848227907886L;

  public KikaSendungsfolgenOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    setIncrementMaxCount(true);
  }
}
