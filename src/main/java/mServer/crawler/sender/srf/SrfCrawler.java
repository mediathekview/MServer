package mServer.crawler.sender.srf;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.srf.tasks.SrfFilmDetailTask;
import mServer.crawler.sender.srf.tasks.SrfSendungOverviewPageTask;
import mServer.crawler.sender.srf.tasks.SrfSendungenOverviewPageTask;
import java.util.Set;
import java.util.concurrent.*;

import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfCrawler extends MediathekReader {

  private static final Logger LOG = LogManager.getLogger(SrfCrawler.class);

  private final ForkJoinPool forkJoinPool;

  public SrfCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.SRF, 0, 1, startPrio);

    final ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
      final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
      worker.setName("SRF-worker-" + worker.getPoolIndex());
      return worker;
    };
    forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4,
            factory,null, true);
  }

  @Override
  protected void addToList() {
    meldungStart();

    try {
      RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
      Set<DatenFilm> films = forkJoinPool.invoke(filmTask);

      Log.sysLog("SRF Filme einsortieren..." + films.size());

      films.forEach(film -> {
        if (!Config.getStop()) {
          addFilm(film);
        }
      });

      Log.sysLog("SRF Film einsortieren fertig");
    } catch (Exception e) {
      Log.errorLog(713213215, e);
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);
    }

    Log.sysLog("SRF fertig");

    meldungThreadUndFertig();
  }

  void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
    pool.shutdown();
    Log.sysLog("SRF shutdown pool...");
    try {
      if (!pool.awaitTermination(delay, delayUnit)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(delay, delayUnit)) {
          Log.sysLog("SRF: Pool nicht beendet");
        }
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

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
