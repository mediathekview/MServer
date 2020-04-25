package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * This task is based on {@link RecursiveTask} and takes a {@link ConcurrentLinkedQueue} of {@link
 * D}. It splits the elements to process on instances of it self based on the crawler configuration
 * and calls the {@link AbstractRecrusivConverterTask#processElement(Object)} for each.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Jabber:</b> nicklas2751@elaon.de<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A result objects type.
 */
public abstract class AbstractRecrusivConverterTask<T, D> extends RecursiveTask<Set<T>> {
  private static final long serialVersionUID = 8416254950859957820L;

  private final ConcurrentLinkedQueue<D> elementsToProcess;

  /** The configuration for the corresponding crawler. */
  protected final transient MServerBasicConfigDTO config;

  /** The crawler which this task is for. */
  protected transient AbstractCrawler crawler;

  /** The set of results. This set will be returned at the end of the task. */
  protected transient Set<T> taskResults;

  public AbstractRecrusivConverterTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    crawler = aCrawler;
    elementsToProcess = aUrlToCrawlDTOs;
    taskResults = ConcurrentHashMap.newKeySet();
    config = MServerConfigManager.getInstance().getSenderConfig(crawler.getSender());
  }

  private ConcurrentLinkedQueue<D> createSubSet(final ConcurrentLinkedQueue<D> aBaseQueue) {
    final int halfSize = aBaseQueue.size() / 2;
    final ConcurrentLinkedQueue<D> elementsToCompute = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < halfSize; i++) {
      elementsToCompute.offer(aBaseQueue.poll());
    }
    return elementsToCompute;
  }

  private void processElements(final ConcurrentLinkedQueue<D> aElementsToProcess) {
    D elementToProcess;
    while ((elementToProcess = aElementsToProcess.poll()) != null) {
      processElement(elementToProcess);
    }
  }

  @Override
  protected Set<T> compute() {
    try {
      if (elementsToProcess.size() <= getMaxElementsToProcess()) {
        processElements(elementsToProcess);
      } else {
        final AbstractRecrusivConverterTask<T, D> rightTask =
            createNewOwnInstance(createSubSet(elementsToProcess));
        final AbstractRecrusivConverterTask<T, D> leftTask =
            createNewOwnInstance(elementsToProcess);
        leftTask.fork();
        taskResults.addAll(leftTask.join());
        taskResults.addAll(rightTask.compute());
      }
    } finally {
      return taskResults;
    }
  }

  /**
   * In this method you just have to create a new instance of yourself.
   *
   * @param aElementsToProcess The {@link ConcurrentLinkedQueue} of {@link D} the new instance
   *     should process.
   * @return The new instance.
   */
  protected abstract AbstractRecrusivConverterTask<T, D> createNewOwnInstance(
      ConcurrentLinkedQueue<D> aElementsToProcess);

  protected abstract Integer getMaxElementsToProcess();

  /**
   * In this method you have to use the element {@link D} to create a object of the return type
   * {@link T}. Add the results to {@link #taskResults}.
   *
   * @param aElement A element to be processed.
   */
  protected abstract void processElement(final D aElement);
}
