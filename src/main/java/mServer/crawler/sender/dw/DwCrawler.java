package mServer.crawler.sender.dw;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

import de.mediathekview.mlib.tool.Log;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.dw.tasks.DWOverviewTask;
import mServer.crawler.sender.dw.tasks.DwFilmDetailTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DwCrawler extends MediathekCrawler {

  private static final Logger LOG = LogManager.getLogger(DwCrawler.class);

  public DwCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.DW, 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> shows =new ConcurrentLinkedQueue<>();
    try {
      shows.addAll(getShows());

      Log.sysLog("DW Anzahl: " + shows.size());
      meldungAddMax(shows.size());

    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSendername(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSendername(), ex);
    }

    return new DwFilmDetailTask(this,shows);
  }

  private Collection<CrawlerUrlDTO> getShows() throws ExecutionException, InterruptedException {
    final CrawlerUrlDTO url = new CrawlerUrlDTO(DwConstants.URL_BASE + DwConstants.URL_OVERVIEW);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> startUrl = new ConcurrentLinkedQueue<>();
    startUrl.add(url);

    final DWOverviewTask overviewTask = new DWOverviewTask(this, startUrl, 0);
    return forkJoinPool.submit(overviewTask).get();

  }
}
