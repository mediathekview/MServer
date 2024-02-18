package de.mediathekview.mserver.crawler.orfon;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
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
import java.util.concurrent.ExecutionException;
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
    Set<OrfOnVideoInfoDTO> allVideos = new HashSet<>();
    try {
      // Sendungen Verpasst (letzten 14 Tage)
      // TAG > Episode > Episode2Film
      final Set<OrfOnVideoInfoDTO> epsiodesFromDay = processDayUrlsToCrawl();
      allVideos.addAll(epsiodesFromDay);
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), allVideos.size());
      getAndSetMaxCount(allVideos.size());
      //
      // Sendungen a-z
      // Buchstabe > Episoden > Episode2Film
      final Set<OrfOnVideoInfoDTO> videosFromTopics = processAZUrlsToCrawl();
      allVideos.addAll(videosFromTopics);
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), allVideos.size());
      getAndSetMaxCount(allVideos.size());
      //
      // History (top categories) > children > 
      final Set<OrfOnVideoInfoDTO> historyVideos = processHistoryUrlToCrawl();
      allVideos.addAll(historyVideos);
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), allVideos.size());
      getAndSetMaxCount(allVideos.size());
      //
      return new OrfOnVideoInfo2FilmTask(this, new ConcurrentLinkedQueue<>(allVideos));
    } catch (final Exception ex) {
      LOG.fatal("Exception in ORFON crawler.", ex);
    }

    return null;
  }
  
  private Set<OrfOnVideoInfoDTO> processDayUrlsToCrawl() throws InterruptedException, ExecutionException {
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> dayTask = forkJoinPool.submit(new OrfOnScheduleTask(this, createDayUrlsToCrawl()));
    final Set<OrfOnBreadCrumsUrlDTO> dayTaskFilms = dayTask.get();
    final ForkJoinTask<Set<OrfOnVideoInfoDTO>> episodesFromDaysTask = forkJoinPool.submit(new OrfOnEpisodeTask(this, new ConcurrentLinkedQueue<>(dayTaskFilms)));
    return episodesFromDaysTask.get();
  }
  
  private Queue<OrfOnBreadCrumsUrlDTO> createDayUrlsToCrawl() {
    final Queue<OrfOnBreadCrumsUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      final String day = now.minusDays(i).format(DAY_PAGE_DATE_FORMATTER);
      final String url = OrfOnConstants.SCHEDULE + "/" + day;
      dayUrlsToCrawl.offer(new OrfOnBreadCrumsUrlDTO(day,url));
    }
    return dayUrlsToCrawl;
  }
  
  private Set<OrfOnVideoInfoDTO> processAZUrlsToCrawl() throws InterruptedException, ExecutionException {
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> letterTask = forkJoinPool.submit(new OrfOnAZTask(this, createAZUrlsToCrawl()));
    final Set<OrfOnBreadCrumsUrlDTO> LetterTaskTopics = letterTask.get();
    final ForkJoinTask<Set<OrfOnVideoInfoDTO>> videosFromTopicsTask = forkJoinPool.submit(new OrfOnEpisodesTask(this, new ConcurrentLinkedQueue<>(LetterTaskTopics)));
    return videosFromTopicsTask.get();    
  }

  
  private Queue<OrfOnBreadCrumsUrlDTO> createAZUrlsToCrawl() {
    final Queue<OrfOnBreadCrumsUrlDTO> letterUrlsToCrawl = new ConcurrentLinkedQueue<>();
    for (char letter = 'A'; letter <= 'Z'; letter++) {
      final String url = OrfOnConstants.AZ + "/" + letter + "?limit="+OrfOnConstants.PAGE_SIZE;
      letterUrlsToCrawl.offer(new OrfOnBreadCrumsUrlDTO(String.valueOf(letter),url));
    }
    // 0 gibt es auch
    final String url = OrfOnConstants.AZ + "/0" + "?limit="+OrfOnConstants.PAGE_SIZE;
    letterUrlsToCrawl.offer(new OrfOnBreadCrumsUrlDTO("0",url));
    return letterUrlsToCrawl;
  }

  private Set<OrfOnVideoInfoDTO> processHistoryUrlToCrawl() throws InterruptedException, ExecutionException {
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> histroyTask = forkJoinPool.submit(new OrfOnHistoryTask(this, createHistoryUrlToCrawl()));
    final Set<OrfOnBreadCrumsUrlDTO> historyChidrenUrls = histroyTask.get();
    LOG.debug("Found {} entries in OrfOnHistoryTask ", historyChidrenUrls.size());
    //
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> historyChildrenTask = forkJoinPool.submit(new OrfOnHistoryChildrenTask(this, new ConcurrentLinkedQueue<>(historyChidrenUrls)));
    final Set<OrfOnBreadCrumsUrlDTO> historyItemUrls = historyChildrenTask.get();
    LOG.debug("Found {} entries in OrfOnHistoryChildrenTask ", historyItemUrls.size());
    //
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> historyItemTask = forkJoinPool.submit(new OrfOnHistoryVideoItemTask(this, new ConcurrentLinkedQueue<>(historyItemUrls)));
    final Set<OrfOnBreadCrumsUrlDTO> historyEpisodesUrls = historyItemTask.get();
    LOG.debug("Found {} entries in OrfOnHistoryVideoItemTask ", historyEpisodesUrls.size());
    //
    final ForkJoinTask<Set<OrfOnVideoInfoDTO>> historyEpisodeTask = forkJoinPool.submit(new OrfOnEpisodeTask(this, new ConcurrentLinkedQueue<>(historyEpisodesUrls)));
    final Set<OrfOnVideoInfoDTO> historyEpisodeVideos = historyEpisodeTask.get();
    LOG.debug("Found {} entries in OrfOnEpisodeTask ", historyEpisodeVideos.size());
    //
    return historyEpisodeVideos;
  }
  
  private Queue<OrfOnBreadCrumsUrlDTO> createHistoryUrlToCrawl() {
    final Queue<OrfOnBreadCrumsUrlDTO> history = new ConcurrentLinkedQueue<>();
    history.offer(new OrfOnBreadCrumsUrlDTO("Base",OrfOnConstants.HISTORY));
    return history;
  }


}
