package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * This task is based on {@link RecursiveTask} and takes a {@link Queue} of {@link D}. It splits the
 * elements to process on instances of it self based on the crawler configuration and calls the
 * {@link AbstractRecursiveConverterTask#processElement(Object)} for each.
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A result objects type.
 */
public abstract class AbstractRecursiveConverterTask<T, D> extends RecursiveTask<Set<T>> {
  private static final Logger LOG = LogManager.getLogger(AbstractRecursiveConverterTask.class);
  private static final long serialVersionUID = 8416254950859957820L;

  private final transient Queue<D> elementsToProcess;

  /** The configuration for the corresponding crawler. */
  protected final transient MServerBasicConfigDTO config;

  /** The crawler which this task is for. */
  protected transient AbstractCrawler crawler;

  /** The set of results. This set will be returned at the end of the task. */
  protected transient Set<T> taskResults;

  protected AbstractRecursiveConverterTask(
      final AbstractCrawler aCrawler, final Queue<D> aUrlToCrawlDTOs) {
    crawler = aCrawler;
    elementsToProcess = aUrlToCrawlDTOs;
    taskResults = ConcurrentHashMap.newKeySet();
    config = crawler.getCrawlerConfig();
  }

  private Queue<D> createSubSet(final Queue<D> aBaseQueue) {
    final int halfSize = aBaseQueue.size() / 2;
    final Queue<D> elementsToCompute = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < halfSize; i++) {
      elementsToCompute.offer(aBaseQueue.poll());
    }
    return elementsToCompute;
  }

  private void processElements(final Queue<D> aElementsToProcess) {
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
        final AbstractRecursiveConverterTask<T, D> rightTask =
            createNewOwnInstance(createSubSet(elementsToProcess));
        final AbstractRecursiveConverterTask<T, D> leftTask =
            createNewOwnInstance(elementsToProcess);
        leftTask.fork();
        taskResults.addAll(rightTask.compute());
        taskResults.addAll(leftTask.join());
      }
    } catch (final Exception exception) {
      crawler.printErrorMessage();
      LOG.error("Something went wrong.", exception);
    }
    return taskResults;
  }

  protected void addElementToProcess(final D newElementToProcess) {
    elementsToProcess.add(newElementToProcess);
  }

  /**
   * In this method you just have to create a new instance of yourself.
   *
   * @param aElementsToProcess The {@link Queue} of {@link D} the new instance should process.
   * @return The new instance.
   */
  protected abstract AbstractRecursiveConverterTask<T, D> createNewOwnInstance(
      Queue<D> aElementsToProcess);

  protected abstract Integer getMaxElementsToProcess();

  /**
   * In this method you have to use the element {@link D} to create a object of the return type
   * {@link T}. Add the results to {@link #taskResults}.
   *
   * @param aElement A element to be processed.
   */
  protected abstract void processElement(final D aElement);
}
