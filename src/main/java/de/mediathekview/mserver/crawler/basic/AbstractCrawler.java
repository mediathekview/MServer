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
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

/** A basic crawler task. */
public abstract class AbstractCrawler implements Callable<Set<Film>> {
  private static final Logger LOG = LogManager.getLogger(AbstractCrawler.class);
  protected final MServerConfigDTO runtimeConfig;
  protected final MServerBasicConfigDTO crawlerConfig;
  private final Collection<SenderProgressListener> progressListeners;
  private final Collection<MessageListener> messageListeners;
  private final AtomicLong maxCount;
  private final AtomicLong actualCount;
  private final AtomicLong errorCount;
  protected ForkJoinPool forkJoinPool;
  protected RecursiveTask<Set<Film>> filmTask;
  protected Set<Film> films;
  private LocalDateTime startTime;
  protected JsoupConnection jsoupConnection;

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
    jsoupConnection = new JsoupConnection(crawlerConfig.getSocketTimeoutInSeconds());

    films = ConcurrentHashMap.newKeySet();
  }

  @Override
  public Set<Film> call() {
    final TimeoutTask timeoutRunner =
        new TimeoutTask(crawlerConfig.getMaximumCrawlDurationInMinutes()) {
          @Override
          public void shutdown() {
            printMessage(ServerMessages.CRAWLER_TIMEOUT, getSender().getName());
            forkJoinPool.shutdownNow();
          }
        };
    try {
      timeoutRunner.start();

      printMessage(ServerMessages.CRAWLER_START, getSender());
      startTime = LocalDateTime.now();

      updateProgress();
      filmTask = createCrawlerTask();
      if (null != filmTask) {
        films.addAll(forkJoinPool.invoke(filmTask));
      }
      removeInvalidEntries();

      final LocalDateTime endTime = LocalDateTime.now();
      final Progress progress =
          new Progress(
              maxCount.get(),
              actualCount.get(),
              errorCount.get(),
              startTime,
              crawlerConfig.getMaximumCrawlDurationInMinutes());
      timeoutRunner.stopTimeout();
      printMessage(
          ServerMessages.CRAWLER_END,
          getSender(),
          Duration.between(startTime, endTime).toMinutes(),
          actualCount.get(),
          errorCount.get(),
          progress.calcActualErrorQuoteInPercent());
    } catch (final Exception exception){
      printErrorMessage();
      LOG.error("Something went wrong crawling {}.",getSender().getName(),exception);
      timeoutRunner.stopTimeout();
    }
    return films;
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
  
  public JsoupConnection getConnection() {
    return jsoupConnection;
  }

  public void setConnection(JsoupConnection connection) {
    jsoupConnection = connection;
  }

  public String requestBodyAsString(String url) throws IOException {
    return getConnection().requestBodyAsString(url);
  }
  
  public Document requestBodyAsHtmlDocument(String url) throws IOException {
    return getConnection().requestBodyAsHtmlDocument(url);
  }

  public Document requestBodyAsXmlDocument(String url) throws IOException {
    return getConnection().requestBodyAsXmlDocument(url);
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

  public long decrementAndGetErrorCount() {
    return errorCount.decrementAndGet();
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

  public void printParseDebugMessage(final String textToParse, final String pattern) {
    incrementAndGetErrorCount();
    updateProgress();
    printMessage(ServerMessages.DEBUG_PARSE_FAILURE, getSender().getName(), textToParse, pattern);
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
    final Progress progress =
        new Progress(
            maxCount.get(),
            actualCount.get(),
            errorCount.get(),
            startTime,
            crawlerConfig.getMaximumCrawlDurationInMinutes());
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
