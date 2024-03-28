package mServer.crawler.sender.orfon;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.orfon.task.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class OrfOnCrawler extends MediathekCrawler {
  private static final Logger LOG = LogManager.getLogger(OrfOnCrawler.class);
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static final String SENDERNAME = Const.ORF;

  public OrfOnCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    Set<OrfOnBreadCrumsUrlDTO> allVideos = new HashSet<>();
    try {
      // Sendungen Verpasst (letzten 14 Tage)
      // TAG > Episode > Episode2Film
      final Set<OrfOnBreadCrumsUrlDTO> epsiodesFromDay = processDayUrlsToCrawl();
      allVideos.addAll(epsiodesFromDay);

      Log.sysLog("ORF Anzahl Tage: " + allVideos.size());

      if (CrawlerTool.loadLongMax()) {
        //
        // Sendungen a-z
        // Buchstabe > Episoden > Episode2Film
        final Set<OrfOnBreadCrumsUrlDTO> videosFromTopics = processAZUrlsToCrawl();
        allVideos.addAll(videosFromTopics);
        Log.sysLog("ORF Anzahl Topics: " + videosFromTopics.size());
        //
        // History (top categories) > children > VideoItem > Episode > Episode2Film
        final Set<OrfOnBreadCrumsUrlDTO> historyVideos = processHistoryUrlToCrawl();
        allVideos.addAll(historyVideos);
        Log.sysLog("ORF Anzahl History: " + historyVideos.size());
      }
      //
      Log.sysLog("ORF Anzahl: " + allVideos.size());

      meldungAddMax(allVideos.size());

    } catch (final Exception ex) {
      Log.errorLog(56146546, ex);
      LOG.fatal("Exception in ORFON crawler.", ex);
    }

    return new OrfOnEpisodeTask(this, new ConcurrentLinkedQueue<>(allVideos));
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> processDayUrlsToCrawl() throws InterruptedException, ExecutionException {
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> dayTask = forkJoinPool.submit(new OrfOnScheduleTask(this, createDayUrlsToCrawl()));
    return dayTask.get();
  }
  
  private ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> createDayUrlsToCrawl() {
    final ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i <= 8; i++) {
      final String day = now.minusDays(i).format(DAY_PAGE_DATE_FORMATTER);
      final String url = OrfOnConstants.SCHEDULE + "/" + day;
      dayUrlsToCrawl.offer(new OrfOnBreadCrumsUrlDTO(day,url));
    }
    return dayUrlsToCrawl;
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> processAZUrlsToCrawl() throws InterruptedException, ExecutionException {
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> letterTask = forkJoinPool.submit(new OrfOnAZTask(this, createAZUrlsToCrawl()));
    final Set<OrfOnBreadCrumsUrlDTO> letterTaskTopics = letterTask.get();
    final ForkJoinTask<Set<OrfOnBreadCrumsUrlDTO>> episodesFromTopicsTask = forkJoinPool.submit(new OrfOnEpisodesTask(this, new ConcurrentLinkedQueue<>(letterTaskTopics)));
    return episodesFromTopicsTask.get();
  }

  
  private ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> createAZUrlsToCrawl() {
    final ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> letterUrlsToCrawl = new ConcurrentLinkedQueue<>();
    for (char letter = 'A'; letter <= 'Z'; letter++) {
      final String url = OrfOnConstants.AZ + "/" + letter + "?limit="+OrfOnConstants.PAGE_SIZE;
      letterUrlsToCrawl.offer(new OrfOnBreadCrumsUrlDTO(String.valueOf(letter),url));
    }
    // 0 gibt es auch
    final String url = OrfOnConstants.AZ + "/0" + "?limit="+OrfOnConstants.PAGE_SIZE;
    letterUrlsToCrawl.offer(new OrfOnBreadCrumsUrlDTO("0",url));
    return letterUrlsToCrawl;
  }

  private Set<OrfOnBreadCrumsUrlDTO> processHistoryUrlToCrawl() throws InterruptedException, ExecutionException {
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
    return historyEpisodesUrls;
  }
  
  private ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> createHistoryUrlToCrawl() {
    final ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> history = new ConcurrentLinkedQueue<>();
    history.offer(new OrfOnBreadCrumsUrlDTO("Base",OrfOnConstants.HISTORY));
    return history;
  }


}
