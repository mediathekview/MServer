package de.mediathekview.mserver.crawler.sr.tasks;

import com.google.common.util.concurrent.RateLimiter;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.Queue;

public abstract class SrRateLimitedDocumentTask<T, D extends CrawlerUrlDTO>
    extends AbstractDocumentTask<T, D> {

  private static final long serialVersionUID = -4077182368484515410L;

  private static RateLimiter LIMITER = null;

  SrRateLimitedDocumentTask(
      final AbstractCrawler crawler,
      final Queue<D> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
  }

  @Override
  protected void processElement(final D urlDTO) {
    if (LIMITER== null) {
      LIMITER = RateLimiter.create(crawler.getRuntimeConfig().getSenderConfig(Sender.SR).getMaximumRequestsPerSecond());
    }
    LIMITER.acquire();
    super.processElement(urlDTO);
  }
}
