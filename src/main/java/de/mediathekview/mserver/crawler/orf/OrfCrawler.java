package de.mediathekview.mserver.crawler.orf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class OrfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(OrfCrawler.class);

  public OrfCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);

  }

  @Override
  public Sender getSender() {
    return Sender.ORF;
  }

  private Set<TopicUrlDTO> getArchiveEntries() throws InterruptedException, ExecutionException {
    final OrfHistoryOverviewTask historyTask = new OrfHistoryOverviewTask(this);
    final Queue<TopicUrlDTO> topics = forkJoinPool.submit(historyTask).get();

    final OrfHistoryTopicTask topicTask = new OrfHistoryTopicTask(this, topics);
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(topicTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private Set<TopicUrlDTO> getDaysEntries() throws InterruptedException, ExecutionException {
    final OrfDayTask dayTask = new OrfDayTask(this, getDayUrls());
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private Queue<CrawlerUrlDTO> getDayUrls() {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
        i
            < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
                + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture();
        i++) {
      urls.add(
          new CrawlerUrlDTO(
              OrfConstants.URL_DAY
                  + LocalDateTime.now()
                      .plus(
                          crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(),
                          ChronoUnit.DAYS)
                      .minus(i, ChronoUnit.DAYS)
                      .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    return urls;
  }

  private Queue<TopicUrlDTO> getLetterEntries() throws InterruptedException, ExecutionException {
    final OrfLetterPageTask letterTask = new OrfLetterPageTask(this);
    final Queue<TopicUrlDTO> shows = forkJoinPool.submit(letterTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {

      final Queue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        shows.addAll(getArchiveEntries());

        addShows(shows, getLetterEntries());
      }
      addShows(shows, getDaysEntries());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new OrfFilmDetailTask(this, shows);
    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ORF crawler.", ex);
    }
    return null;
  }

  private void addShows(Queue<TopicUrlDTO> shows, Collection<TopicUrlDTO> showsToAdd) {
    showsToAdd.forEach(
            show -> {
              // compare only urls because topics can be different in letter and day lists
              if (shows.stream().noneMatch(s -> s.getUrl().equals(show.getUrl()))) {
                shows.add(show);
              } else {
                LOG.debug("duplicated url {} of topic {} removed", show.getUrl(), show.getTopic());
              }
            });
  }
}
