package de.mediathekview.mserver.crawler.sr.tasks;

import com.google.common.util.concurrent.RateLimiter;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class SrRateLimitedDocumentTask<T, D extends CrawlerUrlDTO> extends
    AbstractDocumentTask<T, D> {

  private static final long serialVersionUID = -4077182368484515410L;

  private static final RateLimiter LIMITER = RateLimiter.create(
      MServerConfigManager.getInstance()
          .getSenderConfig(Sender.SR)
          .getMaximumRequestsPerSecond());

  SrRateLimitedDocumentTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<D> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processElement(D aUrlDto) {
    LIMITER.acquire();
    super.processElement(aUrlDto);
  }
}