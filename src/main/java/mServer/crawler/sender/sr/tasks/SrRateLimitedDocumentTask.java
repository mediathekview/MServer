package mServer.crawler.sender.sr.tasks;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

public abstract class SrRateLimitedDocumentTask<T, D extends CrawlerUrlDTO> extends AbstractDocumentTask<T, D> {

  private static final long serialVersionUID = -4077182368484515410L;

  private static final RateLimiter LIMITER = RateLimiter.create(1.0);

  public SrRateLimitedDocumentTask(MediathekReader aCrawler, ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processElement(D aUrlDTO) {
    LIMITER.acquire();
    super.processElement(aUrlDTO);
  }
}
