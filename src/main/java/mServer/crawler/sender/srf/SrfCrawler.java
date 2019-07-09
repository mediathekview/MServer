package mServer.crawler.sender.srf;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.srf.tasks.SrfFilmDetailTask;
import mServer.crawler.sender.srf.tasks.SrfSendungOverviewPageTask;
import mServer.crawler.sender.srf.tasks.SrfSendungenOverviewPageTask;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfCrawler extends MediathekCrawler {

  private static final Logger LOG = LogManager.getLogger(SrfCrawler.class);

  public SrfCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.SRF, 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    try {
      SrfSendungenOverviewPageTask overviewTask = new SrfSendungenOverviewPageTask();
      ConcurrentLinkedQueue<CrawlerUrlDTO> ids = forkJoinPool.submit(overviewTask).get();

      SrfSendungOverviewPageTask task = new SrfSendungOverviewPageTask(this, ids);
      forkJoinPool.execute(task);

      final ConcurrentLinkedQueue<CrawlerUrlDTO> dtos
              = new ConcurrentLinkedQueue<>();
      dtos.addAll(task.join());

      Log.sysLog("SRF Anzahl: " + dtos.size());
      meldungAddMax(dtos.size());

      return new SrfFilmDetailTask(this, dtos);

    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in SRF crawler.", ex);
    }
    return null;
  }
}
