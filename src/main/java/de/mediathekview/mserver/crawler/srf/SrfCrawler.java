package de.mediathekview.mserver.crawler.srf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.srf.tasks.SrfFilmDetailTask;
import de.mediathekview.mserver.crawler.srf.tasks.SrfScheduleOverviewTask;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTopicOverviewTask;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTopicsOverviewTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SrfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(SrfCrawler.class);

  private static final DateTimeFormatter ISO_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public SrfCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.SRF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      final Set<CrawlerUrlDTO> dtos = new HashSet<CrawlerUrlDTO>();
      //
      final Queue<CrawlerUrlDTO> schedulePageUrls = createScheduleUrls();
      final SrfScheduleOverviewTask schedulePageTask = new SrfScheduleOverviewTask(this, schedulePageUrls);
      final Set<CrawlerUrlDTO> scheduleFilmUrls = forkJoinPool.submit(schedulePageTask).get();
      dtos.addAll(scheduleFilmUrls);
      //

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        final Queue<CrawlerUrlDTO> topicsUrls = new ConcurrentLinkedQueue<>();
        topicsUrls.add(new CrawlerUrlDTO(SrfConstants.OVERVIEW_PAGE_URL));
        final SrfTopicsOverviewTask overviewTask = new SrfTopicsOverviewTask(this, topicsUrls);
        final Queue<TopicUrlDTO> topicUrls =
            new ConcurrentLinkedQueue<>(forkJoinPool.submit(overviewTask).get());
  
        printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_COUNT, getSender().getName(), topicUrls.size());
  
        final SrfTopicOverviewTask task =
            new SrfTopicOverviewTask(this, topicUrls, SrfConstants.BASE_URL);
        final Set<CrawlerUrlDTO> topicSearchUrls = forkJoinPool.submit(task).get();

        dtos.addAll(topicSearchUrls);
      }
      //
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), dtos.size());
      getAndSetMaxCount(dtos.size());

      return new SrfFilmDetailTask(this, new ConcurrentLinkedQueue<>(dtos));

    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in SRF crawler.", ex);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  private Queue<CrawlerUrlDTO> createScheduleUrls() {
    final Queue<CrawlerUrlDTO> scheduleUrls = new ConcurrentLinkedQueue<>();
    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      final String day = now.minusDays(i).format(ISO_DATE_FORMAT);
      final String url = String.format(SrfConstants.SCHEDULE_PER_DAY, day);
      scheduleUrls.offer(new CrawlerUrlDTO(url));
    }
    return scheduleUrls;
  }
  
}
