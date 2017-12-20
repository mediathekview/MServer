package de.mediathekview.mserver.crawler.hr.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class HrSendungsfolgenVerpasstOverviewPageTask extends HrSendungsfolgenOverviewPageTask {
  private static final long serialVersionUID = 550079618104128843L;

  public HrSendungsfolgenVerpasstOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected String getSendungsfoleUrlSelector() {
    return ".c-epgBroadcast__headline.text__headline";
  }

}
