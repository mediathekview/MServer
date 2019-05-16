package de.mediathekview.mserver.crawler.mdr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class MdrCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(MdrCrawler.class);

  public MdrCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.MDR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();
    try {
      shows.addAll(getTopicEntries());
      updateMaxCount(shows);

      shows.addAll(
          getDaysEntries().stream()
              .filter(show -> !shows.contains(show))
              .collect(Collectors.toSet()));
      updateMaxCount(shows);

      return new MdrFilmTask(this, shows, MdrConstants.URL_BASE);
    } catch (final ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in MDR crawler.", ex);
    }
    return null;
  }

  private void updateMaxCount(final ConcurrentLinkedQueue<CrawlerUrlDTO> shows) {
    incrementMaxCountBySizeAndGetNewSize(shows.size());
    updateProgress();
    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
  }

  private Set<CrawlerUrlDTO> getTopicEntries() throws ExecutionException, InterruptedException {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> letterUrl = new ConcurrentLinkedQueue<>();
    letterUrl.add(new CrawlerUrlDTO(MdrConstants.URL_LETTER_PAGE));

    final ConcurrentLinkedQueue<CrawlerUrlDTO> gebaerdenLetterUrl = new ConcurrentLinkedQueue<>();
    gebaerdenLetterUrl.add(new CrawlerUrlDTO(MdrConstants.URL_GEBAERDEN_DES_TAGES));

    final MdrLetterPageUrlTask letterUrlTask = new MdrLetterPageUrlTask(this, letterUrl);
    final MdrLetterPageUrlTask gebaerdenLetterUrlTask =
        new MdrLetterPageUrlTask(this, gebaerdenLetterUrl);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> letterPageUrls =
        new ConcurrentLinkedQueue<>(forkJoinPool.submit(letterUrlTask).get());

    final MdrLetterPageTask letterTask = new MdrLetterPageTask(this, letterPageUrls);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> topicUrls =
        new ConcurrentLinkedQueue<>(forkJoinPool.submit(letterTask).get());
    // A Gebaerden Letter Page is equal to a topic page.
    topicUrls.addAll(forkJoinPool.submit(gebaerdenLetterUrlTask).get());

    final MdrTopicPageTask topicTask = new MdrTopicPageTask(this, topicUrls);

    return forkJoinPool.submit(topicTask).get();
  }

  private Set<CrawlerUrlDTO> getDaysEntries() throws ExecutionException, InterruptedException {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrl = new ConcurrentLinkedQueue<>();
    dayUrl.add(new CrawlerUrlDTO(MdrConstants.URL_DAY_PAGE));

    final MdrDayPageUrlTask dayUrlTask =
        new MdrDayPageUrlTask(
            this, dayUrl, crawlerConfig.getMaximumDaysForSendungVerpasstSection());

    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayPageUrls =
        new ConcurrentLinkedQueue<>(forkJoinPool.submit(dayUrlTask).get());

    final MdrDayPageTask dayPageTask = new MdrDayPageTask(this, dayPageUrls);
    final Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayPageTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }
}
