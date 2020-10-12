package de.mediathekview.mserver.crawler.basic;

import java.util.Queue;
import java.util.concurrent.RecursiveTask;

/**
 * This task is based on {@link RecursiveTask} and takes a {@link Queue} of {@link D}. It splits the
 * URLs on instances of it self based on the crawler configuration and calls the {@link
 * AbstractRecursiveConverterTask#processElement(Object)} for each.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to create the result
 *     objects.
 */
public abstract class AbstractUrlTask<T, D extends CrawlerUrlDTO>
    extends AbstractRecursiveConverterTask<T, D> {
  private static final long serialVersionUID = -4077156510484515410L;

  public AbstractUrlTask(final AbstractCrawler aCrawler, final Queue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return config.getMaximumUrlsPerTask();
  }
}
