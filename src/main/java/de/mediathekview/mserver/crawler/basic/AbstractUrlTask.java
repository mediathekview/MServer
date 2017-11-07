package de.mediathekview.mserver.crawler.basic;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;

/**
 * This task is based on {@link RecursiveTask} and takes a {@link ConcurrentLinkedQueue} of
 * {@link D}. It splits the URLs on instances of it self based on the crawler configuration and
 * calls the {@link this#processUrl(CrawlerUrlDTO)} for each.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to create the result
 *        objects.
 */
public abstract class AbstractUrlTask<T, D extends CrawlerUrlDTO> extends RecursiveTask<Set<T>> {
  private static final long serialVersionUID = -4077156510484515410L;

  private final ConcurrentLinkedQueue<D> urlsToCrawl;

  /**
   * The configuration for the corresponding crawler.
   */
  protected final transient MServerBasicConfigDTO config;

  /**
   * The crawler which this task is for.
   */
  protected transient AbstractCrawler crawler;

  /**
   * The set of results. This set will be returned at the end of the task.
   */
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

  /**
   * In this method you just have to create a new instance of yourself.
   *
   * @param aURLsToCrawl The {@link ConcurrentLinkedQueue} of {@link D} the new instance should
   *        process.
   * @return The new instance.
   */
  protected abstract AbstractUrlTask<T, D> createNewOwnInstance(
      ConcurrentLinkedQueue<D> aURLsToCrawl);

  /**
   * In this method you have to use the DTO {@link D} to create a object of the return type
   * {@link T}. Add the results to {@link #taskResults}.
   *
   * @param aDTO A DTO containing at least the URL of the given document.
   */
  protected abstract void processUrl(final D aUrlDTO);

}
