package de.mediathekview.mserver.crawler.orfon;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnAZTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnEpisodeTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnEpisodesTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnHistoryChildrenTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnHistoryTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnHistoryVideoItemTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnScheduleTask;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnVideoInfo2FilmTask;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class OrfOnCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(OrfOnCrawler.class);
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public OrfOnCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ORF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      // Sendungen Verpasst
      
      
      final ForkJoinTask<Set<TopicUrlDTO>> dayTask = forkJoinPool.submit(new OrfOnScheduleTask(this, createDayUrlsToCrawl()));
      final Set<TopicUrlDTO> dayTaskFilms = dayTask.get();
      final ForkJoinTask<Set<OrfOnVideoInfoDTO>> episodesFromDaysTask = forkJoinPool.submit(new OrfOnEpisodeTask(this, new ConcurrentLinkedQueue<>(dayTaskFilms)));
      final Set<OrfOnVideoInfoDTO> epsiodesFromDay = episodesFromDaysTask.get();
      
      //
      //
      
      final ForkJoinTask<Set<TopicUrlDTO>> letterTask = forkJoinPool.submit(new OrfOnAZTask(this, createAZUrlsToCrawl()));
      final Set<TopicUrlDTO> LetterTaskTopics = letterTask.get();
      final ForkJoinTask<Set<OrfOnVideoInfoDTO>> videosFromTopicsTask = forkJoinPool.submit(new OrfOnEpisodesTask(this, new ConcurrentLinkedQueue<>(LetterTaskTopics)));
      final Set<OrfOnVideoInfoDTO> videosFromTopics = videosFromTopicsTask.get();
      
      //
      //
      
      final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> histroyTask = forkJoinPool.submit(new OrfOnHistoryTask(this, createHistoryUrlToCrawl()));
      final Set<OrfOnBreadCrumsUrlDTO> historyChidrenUrls = histroyTask.get();
      //
      final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> historyChildrenTask = forkJoinPool.submit(new OrfOnHistoryChildrenTask(this, new ConcurrentLinkedQueue<>(historyChidrenUrls)));
      final Set<OrfOnBreadCrumsUrlDTO> historyItemUrls = historyChildrenTask.get();
      //
      final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> historyItemTask = forkJoinPool.submit(new OrfOnHistoryVideoItemTask(this, new ConcurrentLinkedQueue<>(historyItemUrls)));
      final Set<OrfOnBreadCrumsUrlDTO> historyEpisodesUrls = historyItemTask.get();
      //
      final ForkJoinTask<Set<OrfOnVideoInfoDTO>> historyEpisodeTask = forkJoinPool.submit(new OrfOnEpisodeTask(this, new ConcurrentLinkedQueue<>(historyEpisodesUrls)));
      final Set<OrfOnVideoInfoDTO> historyVideos = historyEpisodeTask.get();
      
      /*LOG.debug("#######");
      for (OrfOnVideoInfoDTO e : historyVideos) {
        LOG.debug("{} {} {} {}" , e.getId(), e.getTopic(), e.getTitle(), e.getVideoUrls());
      }*/
      
      //
      //printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), LetterTaskTopics.size());
      //getAndSetMaxCount(LetterTaskTopics.size());
      //
      /*
      final ForkJoinTask<Set<OrfOnVideoInfoDTO>> videos = forkJoinPool.submit(new OrfOnEpisodeTask(this, new ConcurrentLinkedQueue<>(null)));
      final Set<OrfOnVideoInfoDTO> xx = videos.get();
      for (OrfOnVideoInfoDTO e : xx) {
        LOG.debug("{} {} {} {}" , e.getId(), e.getTopic(), e.getTitle(), e.getTitleWithDate());
      }
      */
      //
      Set<OrfOnVideoInfoDTO> allVideos = new HashSet<>();
      allVideos.addAll(epsiodesFromDay);
      allVideos.addAll(videosFromTopics);
      allVideos.addAll(historyVideos);
      //
      return new OrfOnVideoInfo2FilmTask(this, new ConcurrentLinkedQueue<>(allVideos));
    } catch (final Exception ex) {
      LOG.fatal("Exception in ORFON crawler.", ex);
    }

    return null;
  }
  
  private Queue<TopicUrlDTO> createDayUrlsToCrawl() {
    final Queue<TopicUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      final String day = now.minusDays(i).format(DAY_PAGE_DATE_FORMATTER);
      final String url = OrfOnConstants.SCHEDULE + "/" + day;
      dayUrlsToCrawl.offer(new TopicUrlDTO(day,url));
    }
    return dayUrlsToCrawl;
  }
  
  private Queue<TopicUrlDTO> createAZUrlsToCrawl() {
    final Queue<TopicUrlDTO> letterUrlsToCrawl = new ConcurrentLinkedQueue<>();
    for (char letter = 'A'; letter <= 'Z'; letter++) {
      final String url = OrfOnConstants.AZ + "/" + letter + "?limit="+OrfOnConstants.PAGE_SIZE;
      letterUrlsToCrawl.offer(new TopicUrlDTO(String.valueOf(letter),url));
    }
    // 0
    final String url = OrfOnConstants.AZ + "/0" + "?limit="+OrfOnConstants.PAGE_SIZE;
    letterUrlsToCrawl.offer(new TopicUrlDTO("0",url));
    return letterUrlsToCrawl;
  }

  private Queue<OrfOnBreadCrumsUrlDTO> createHistoryUrlToCrawl() {
    final Queue<OrfOnBreadCrumsUrlDTO> history = new ConcurrentLinkedQueue<>();
    history.offer(new OrfOnBreadCrumsUrlDTO("Base",OrfOnConstants.HISTORY));
    return history;
  }


}
