package mServer.crawler.sender.base;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;

public abstract class AbstractRateLimitedDocumentTask<T, D extends CrawlerUrlDTO> extends AbstractDocumentTask<T, D> {

  private static final long serialVersionUID = -4077182368484515410L;

  private static final RateLimiter limiter = RateLimiter.create(1.0);

  public AbstractRateLimitedDocumentTask(MediathekReader aCrawler, ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processElement(D aUrlDTO) {
    limiter.acquire();
    super.processElement(aUrlDTO);
  }
}
