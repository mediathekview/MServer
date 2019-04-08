package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

/** A basic crawler task. */
public abstract class AbstractCrawler implements Callable<Set<Film>> {
  protected final MServerConfigDTO runtimeConfig;
  protected final MServerBasicConfigDTO crawlerConfig;
  protected ForkJoinPool forkJoinPool;
  private final Collection<SenderProgressListener> progressListeners;
  private final Collection<MessageListener> messageListeners;

  protected RecursiveTask<Set<Film>> filmTask;
  protected Set<Film> films;

  private final AtomicLong maxCount;
  private final AtomicLong actualCount;
  private final AtomicLong errorCount;

  public AbstractCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    forkJoinPool = aForkJoinPool;
    maxCount = new AtomicLong(0);
    actualCount = new AtomicLong(0);
    errorCount = new AtomicLong(0);

    progressListeners = aProgressListeners;

    messageListeners = aMessageListeners;

    runtimeConfig = rootConfig.getConfig();
    crawlerConfig = rootConfig.getSenderConfig(getSender());

    films = ConcurrentHashMap.newKeySet();
  }

  @Override
  public Set<Film> call() {
    final TimeoutTask timeoutRunner =
        new TimeoutTask(crawlerConfig.getMaximumCrawlDurationInMinutes()) {
          @Override
          public void shutdown() {
            forkJoinPool.shutdownNow();
            printMessage(ServerMessages.CRAWLER_TIMEOUT, getSender().getName());
          }
        };
    try {
      timeoutRunner.start();

      printMessage(ServerMessages.CRAWLER_START, getSender());
      final LocalTime startTime = LocalTime.now();

      updateProgress();
      filmTask = createCrawlerTask();
      if (null != filmTask) {
        films.addAll(forkJoinPool.invoke(filmTask));
      }
      removeInvalidEntries();

      final LocalTime endTime = LocalTime.now();
      final Progress progress = new Progress(maxCount.get(), actualCount.get(), errorCount.get());
      timeoutRunner.stopTimeout();
      printMessage(
          ServerMessages.CRAWLER_END,
          getSender(),
          Duration.between(startTime, endTime).toMinutes(),
          actualCount.get(),
          errorCount.get(),
          progress.calcActualErrorQuoteInPercent());
    } finally {
      return films;
    }
  }

  private void removeInvalidEntries() {
    // Removes entries without url or neither thema nor title.
    films.removeIf(
        enty ->
            enty.getUrls().isEmpty() || (enty.getThema().isEmpty() && enty.getTitel().isEmpty()));
  }

  public long getAndSetMaxCount(final long aNewMaxValue) {
    return maxCount.getAndSet(aNewMaxValue);
  }

  public synchronized MServerBasicConfigDTO getCrawlerConfig() {
    return crawlerConfig;
  }

  public synchronized MServerConfigDTO getRuntimeConfig() {
    return runtimeConfig;
  }

  /**
   * This method should just return the entry of {@link Sender} for the Sender which this crawler is
   * for.
   *
   * @return The sender which this crawler is for.
   */
  public abstract Sender getSender();

  public long incrementAndGetActualCount() {
    return actualCount.incrementAndGet();
  }

  public long incrementAndGetErrorCount() {
    return errorCount.incrementAndGet();
  }

  public long incrementAndGetMaxCount() {
    return maxCount.incrementAndGet();
  }

  public long incrementMaxCountBySizeAndGetNewSize(final long sizeToAdd) {
    return maxCount.addAndGet(sizeToAdd);
  }

  public void printErrorMessage() {
    printMessage(ServerMessages.CRAWLER_ERROR, getSender());
  }

  public void printInvalidUrlErrorMessage(final String aInvalidUrl) {
    incrementAndGetErrorCount();
    updateProgress();
    printMessage(ServerMessages.DEBUG_INVALID_URL, getSender().getName(), aInvalidUrl);
  }

  public void printMessage(final Message aMessage, final Object... args) {
    messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
  }

  public void printMissingElementErrorMessage(final String aMissingElementName) {
    incrementAndGetErrorCount();
    updateProgress();
    printMessage(ServerMessages.DEBUG_MISSING_ELEMENT, getSender().getName(), aMissingElementName);
  }

  public void printMissingElementsErrorMessage(final String... aMissingElementNames) {
    incrementAndGetErrorCount();
    updateProgress();
    for (final String missingElementName : aMissingElementNames) {
      printMessage(ServerMessages.DEBUG_MISSING_ELEMENT, getSender().getName(), missingElementName);
    }
  }

  public void stop() {
    filmTask.cancel(true);
  }

  public void updateProgress() {
    final Progress progress = new Progress(maxCount.get(), actualCount.get(), errorCount.get());
    progressListeners.parallelStream().forEach(l -> l.updateProgess(getSender(), progress));
  }

  /**
   * This the method where the "magic" happens. In this method you have to create a {@link
   * RecursiveTask} which gathers a set of {@link Film}.
   *
   * @return The found films.
   */
  protected abstract RecursiveTask<Set<Film>> createCrawlerTask();
}
