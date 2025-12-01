package mServer.crawler.sender.arte.tasks;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ArteRateLimitedJsonRestTask<T, R, D extends CrawlerUrlDTO> extends AbstractJsonRestTask<T, R, D> {
  private static final long serialVersionUID = 1L;
  private static final RateLimiter rateLimiter = RateLimiter.create(10.0);

  protected ArteRateLimitedJsonRestTask(MediathekReader aCrawler, ConcurrentLinkedQueue<D> urlToCrawlDTOs, Optional<String> authKey) {
    super(aCrawler, urlToCrawlDTOs, authKey);
  }

  @Override
  protected void processRestTarget(final D aDTO, final WebTarget aTarget) {
    rateLimiter.acquire();
    super.processRestTarget(aDTO, aTarget);
  }
}