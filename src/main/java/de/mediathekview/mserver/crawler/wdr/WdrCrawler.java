package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class WdrCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(WdrCrawler.class);

  JsoupConnection jsoupConnection;

  public WdrCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);

    this.jsoupConnection = new JsoupConnection();
  }

  @Override
  public Sender getSender() {
    return Sender.WDR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      final ConcurrentLinkedQueue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();
      shows.addAll(getOrchestraEntries());
      shows.addAll(getDaysEntries());

      getLetterPageEntries()
          .forEach(
              show -> {
                if (!shows.contains(show)) {
                  shows.add(show);
                }
              });

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new WdrFilmDetailTask(this, shows, jsoupConnection);
    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in WDR crawler.", ex);
    }
    return null;
  }

  private Set<TopicUrlDTO> getDaysEntries() throws InterruptedException, ExecutionException {
    final WdrDayPageTask dayTask = new WdrDayPageTask(this, getDayUrls(), jsoupConnection);
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private Set<TopicUrlDTO> getLetterPageEntries() throws InterruptedException, ExecutionException {
    final WdrLetterPageTask letterTask = new WdrLetterPageTask(this, jsoupConnection);
    final ConcurrentLinkedQueue<WdrTopicUrlDto> letterPageEntries = new ConcurrentLinkedQueue<>();
    letterPageEntries.addAll(forkJoinPool.submit(letterTask).get());

    final WdrTopicOverviewTask overviewTask = new WdrTopicOverviewTask(this, letterPageEntries, jsoupConnection, 0);
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
        i
            <= crawlerConfig.getMaximumDaysForSendungVerpasstSection()
                + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture();
        i++) {
      urls.add(
          new CrawlerUrlDTO(
              String.format(
                  WdrConstants.URL_DAY,
                  LocalDateTime.now()
                      .plus(
                          crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(),
                          ChronoUnit.DAYS)
                      .minus(i, ChronoUnit.DAYS)
                      .format(DateTimeFormatter.ofPattern("ddMMyyyy")))));
    }

    return urls;
  }

  private Set<TopicUrlDTO> getOrchestraEntries() throws InterruptedException, ExecutionException {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawl = new ConcurrentLinkedQueue<>();
    final ConcurrentLinkedQueue<WdrTopicUrlDto> topicOverviews = new ConcurrentLinkedQueue<>();

    urlToCrawl.add(new CrawlerUrlDTO(WdrConstants.URL_RADIO_ORCHESTRA));

    final WdrRadioPageTask radioPageTask = new WdrRadioPageTask(this, urlToCrawl, jsoupConnection);
    topicOverviews.addAll(forkJoinPool.submit(radioPageTask).get());

    final WdrTopicOverviewTask overviewTask = new WdrTopicOverviewTask(this, topicOverviews, jsoupConnection, 0);
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
    return shows;
  }
}
