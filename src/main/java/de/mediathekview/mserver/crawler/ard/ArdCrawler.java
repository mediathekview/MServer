package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.ard.tasks.*;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class ArdCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ArdCrawler.class);
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public ArdCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARD;
  }

  private Queue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final Queue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      final String day = now.minusDays(i).format(DAY_PAGE_DATE_FORMATTER);

      for (final String client : ArdConstants.CLIENTS) {
        final String url =
            String.format(ArdConstants.DAY_PAGE_URL, client, day, day, ArdConstants.DAY_PAGE_SIZE);
        dayUrlsToCrawl.offer(new CrawlerUrlDTO(url));
      }
    }
    return dayUrlsToCrawl;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      final ForkJoinTask<Set<ArdFilmInfoDto>> dayTask =
          forkJoinPool.submit(new ArdDayPageTask(this, createDayUrlsToCrawl()));

      final Set<ArdFilmInfoDto> shows = dayTask.get();
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        final Set<ForkJoinTask<Set<CrawlerUrlDTO>>> senderTopicTasks = createSenderTopicTasks();

        final Set<CrawlerUrlDTO> senderTopicUrls = new HashSet<>();
        for (final ForkJoinTask<Set<CrawlerUrlDTO>> senderTopicTask : senderTopicTasks) {
          senderTopicUrls.addAll(senderTopicTask.get());
        }
        LOG.debug("sender topic tasks: {}", senderTopicUrls.size());
        final ArdTopicPageTask topicTask =
            new ArdTopicPageTask(this, new ConcurrentLinkedQueue<>(senderTopicUrls));
        final int showsCountBefore = shows.size();
        shows.addAll(forkJoinPool.submit(topicTask).get());
        LOG.debug(
            "ARD crawler found {} topics for all sub-sender.", shows.size() - showsCountBefore);
      }

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());
      return new ArdFilmDetailTask(this, new ConcurrentLinkedQueue<>(shows));
    } catch (final InterruptedException ex) {
      LOG.fatal("Exception in ARD crawler.", ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in ARD crawler.", ex);
    }
    return null;
  }

  private Set<ForkJoinTask<Set<CrawlerUrlDTO>>> createSenderTopicTasks() {
    final Set<ForkJoinTask<Set<CrawlerUrlDTO>>> topicTasks = new HashSet<>();
    try {
      topicTasks.add(getTopicEntriesBySender(ArdConstants.DEFAULT_CLIENT));
    } catch (ExecutionException | InterruptedException e) {
      LOG.error("exception sender topic {}", ArdConstants.DEFAULT_CLIENT, e);
    }
    for (final String client : ArdConstants.CLIENTS) {
      try {
        topicTasks.add(getTopicEntriesBySender(client));
      } catch (ExecutionException | InterruptedException e) {
        LOG.error("exception sender topic {}", client, e);
      }
    }
    return topicTasks;
  }

  private ForkJoinTask<Set<CrawlerUrlDTO>> getTopicEntriesBySender(final String sender) throws ExecutionException, InterruptedException {
     Set<CrawlerUrlDTO> senderTopics = forkJoinPool.submit(
        new ArdTopicsTask(this, sender, createTopicsOverviewUrl(sender))).get();

    LOG.debug("topics task result {}", senderTopics.size());
     return forkJoinPool.submit(new ArdTopicsLetterTask(this, sender, new ConcurrentLinkedQueue<>(senderTopics)));
  }

  private Queue<CrawlerUrlDTO> createTopicsOverviewUrl(final String client) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url = String.format(ArdConstants.TOPICS_URL, client);
    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }
}
