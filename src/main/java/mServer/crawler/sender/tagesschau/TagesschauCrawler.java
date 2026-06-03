package mServer.crawler.sender.tagesschau;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.tagesschau.tasks.TagesschauEntriesTask;
import mServer.crawler.sender.tagesschau.tasks.TagesschauVideoTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class TagesschauCrawler extends MediathekCrawler {

  private static final Logger LOG = LogManager.getLogger(TagesschauCrawler.class);

  public TagesschauCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.TAGESSCHAU24, 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    try {
      Set<CrawlerUrlDTO> videos = new HashSet<>();
      ConcurrentLinkedQueue<CrawlerUrlDTO> inputQueue = createArchiveUrl();

      // short run uses 2 recursion -> only the actual month is included
      int recursionMax = CrawlerTool.loadLongMax() ? 10 : 2;
      int recursionCount = 0;

      while (!inputQueue.isEmpty() && recursionCount < recursionMax) {
        LOG.debug("processing {} sub pages", inputQueue.size());
        TagesschauEntriesTask round1 = new TagesschauEntriesTask(this, inputQueue);
        final Set<EntryUrlDto> results = this.forkJoinPool.submit(round1).get();

        Set<CrawlerUrlDTO> subPages = new HashSet<>();
        results.forEach(
                result -> {
                  videos.addAll(result.getVideos());
                  subPages.addAll(result.getSubPages());
                });
        inputQueue = new ConcurrentLinkedQueue<>(subPages);
        recursionCount++;
      }

      Log.sysLog("Tagesschau 20 Jahre Anzahl topics: " + videos.size());
      meldungAddMax(videos.size());

      return new TagesschauVideoTask(this, new ConcurrentLinkedQueue<>(videos));

    } catch (final InterruptedException ex) {
      LOG.fatal("Exception in Tagesschau crawler.", ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in Tagesschau crawler.", ex);
    }
    return new RecursiveTask<>() {
      @Override
      protected Set<DatenFilm> compute() {
        return Set.of();
      }
    };
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createArchiveUrl() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(TagesschauConstants.ARCHIVE_START_URL));
    return urls;
  }
}


