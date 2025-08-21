package mServer.crawler.sender;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Base class of crawlers using ForkJoinPool
public abstract class MediathekCrawler extends MediathekReader {

  private static final Logger LOG = LogManager.getLogger(MediathekCrawler.class);

  protected final ForkJoinPool forkJoinPool;

  protected MediathekCrawler(FilmeSuchen aMSearchFilmeSuchen, String aSendername, int aSenderMaxThread, int aSenderWartenSeiteLaden, int aStartPrio) {
    super(aMSearchFilmeSuchen, aSendername, aSenderMaxThread, aSenderWartenSeiteLaden, aStartPrio);

    forkJoinPool = createForkJoinPool(aSendername);
  }

  public static ForkJoinPool createForkJoinPool(String aSenderName) {
    final ForkJoinPool.ForkJoinWorkerThreadFactory factory = (ForkJoinPool pool) -> {
      final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
      worker.setName(aSenderName + "-worker-" + worker.getPoolIndex());
      return worker;
    };

    return new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4, factory, null, true);
  }

  @Override
  protected void addToList() {
    meldungStart();

    try {
      runCrawler();

      Log.sysLog(getSendername() + ": Film einsortieren fertig");
    } catch (Exception e) {
      Log.errorLog(516516521, e);
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(60, TimeUnit.SECONDS);
    }

    Log.sysLog(getSendername() + ": fertig");

    meldungThreadUndFertig();
  }

  protected void runCrawler() {
    RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
    Set<DatenFilm> films = forkJoinPool.invoke(filmTask);

    Log.sysLog(getSendername() + ": Filme einsortieren..." + films.size());
    if (films.isEmpty()) {
      LOG.fatal("{}: no films found!", getSendername());
    }

    films.forEach(film -> {
      if (!Config.getStop()) {
        try {
          prepareFilm(film);
          addFilm(film);
        } catch (Exception e) {
          final String index = film.getIndexAddOld();
          Log.errorLog(974513456, e, index);
          LOG.error("{}: Error while processing film: {}: {}", getSendername(), index, e);
          FilmeSuchen.listeSenderLaufen.inc(film.arr[DatenFilm.FILM_SENDER], RunSender.Count.FEHLER);
        }
      }
    });
  }

  protected abstract RecursiveTask<Set<DatenFilm>> createCrawlerTask();

  protected void prepareFilm(DatenFilm film) {
  }

  private void shutdownAndAwaitTermination(long delay, TimeUnit delayUnit) {
    Log.sysLog(getSendername() + ": shutdown pool...");

    try {
      try {
        forkJoinPool.shutdown();
        if (!forkJoinPool.awaitTermination(delay, delayUnit)) {
          forkJoinPool.shutdownNow();
          if (!forkJoinPool.awaitTermination(delay, delayUnit)) {
            Log.sysLog(getSendername() + ": Pool nicht beendet");
          }
        }
      } catch (InterruptedException ie) {
        Log.errorLog(974513454, ie);
        forkJoinPool.shutdownNow();
        Thread.currentThread().interrupt();
      }
    } catch (Exception e) {
      Log.errorLog(974513455, e);
    }
  }
}
