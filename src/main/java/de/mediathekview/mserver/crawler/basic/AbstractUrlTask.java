package de.mediathekview.mserver.crawler.basic;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * This task is based on {@link RecursiveTask} and takes a {@link ConcurrentLinkedQueue} of
 * {@link D}. It splits the URLs on instances of it self based on the crawler configuration and
 * calls the {@link this#processUrl(CrawlerUrlDTO)} for each.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to create the result
 *        objects.
 */
public abstract class AbstractUrlTask<T, D extends CrawlerUrlDTO>
    extends AbstractRecrusivConverterTask<T, D> {
  private static final long serialVersionUID = -4077156510484515410L;

  public AbstractUrlTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return config.getMaximumUrlsPerTask();
  }

  @Deprecated
  protected void processElement(final D aDTO) {
    processElement(aDTO);
  }


}
