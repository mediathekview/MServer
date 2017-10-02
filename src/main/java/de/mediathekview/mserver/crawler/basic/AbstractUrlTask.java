package de.mediathekview.mserver.crawler.basic;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;

/**
 * Recursively crawls a Website.
 */
public abstract class AbstractUrlTask<T, D extends CrawlerUrlsDTO> extends RecursiveTask<Set<T>> {
  private static final long serialVersionUID = -4077156510484515410L;

  private final ConcurrentLinkedQueue<D> urlsToCrawl;
  protected final transient MServerBasicConfigDTO config;
  protected transient AbstractCrawler crawler;

  protected transient Set<T> taskResults;

  public AbstractUrlTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    crawler = aCrawler;
    urlsToCrawl = aUrlToCrawlDTOs;
    taskResults = ConcurrentHashMap.newKeySet();
    config = MServerConfigManager.getInstance().getConfig(crawler.getSender());
  }

  private void crawlPage(final ConcurrentLinkedQueue<D> aUrls) {
    D urlDTO;
    while ((urlDTO = aUrls.poll()) != null) {
      processUrl(urlDTO);
    }
  }

  private ConcurrentLinkedQueue<D> createSubSet(final ConcurrentLinkedQueue<D> aBaseQueue) {
    final int halfSize = aBaseQueue.size() / 2;
    final ConcurrentLinkedQueue<D> urlsToCrawlSubset = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < halfSize; i++) {
      urlsToCrawlSubset.offer(aBaseQueue.poll());
    }
    return urlsToCrawlSubset;
  }

  @Override
  protected Set<T> compute() {
    if (urlsToCrawl.size() <= config.getMaximumUrlsPerTask()) {
      crawlPage(urlsToCrawl);
    } else {
      final AbstractUrlTask<T, D> rightTask = createNewOwnInstance(createSubSet(urlsToCrawl));
      final AbstractUrlTask<T, D> leftTask = createNewOwnInstance(urlsToCrawl);
      leftTask.fork();
      taskResults.addAll(rightTask.compute());
      taskResults.addAll(leftTask.join());
    }
    return taskResults;
  }

  protected abstract AbstractUrlTask<T, D> createNewOwnInstance(
      ConcurrentLinkedQueue<D> aURLsToCrawl);


  protected abstract void processUrl(final D aUrlDTO);

}
