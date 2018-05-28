package mServer.crawler.sender.br;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;

public class BrCrawler extends MediathekReader {

  public static final String SENDERNAME = Const.BR;
  public static final String BASE_URL = "https://www.br.de/mediathek/";

  private final ForkJoinPool forkJoinPool;

  public BrCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 100, startPrio);

    forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4);
  }

  @Override
  protected void addToList() {
    meldungStart();

    try {
      RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
      Set<DatenFilm> films = forkJoinPool.invoke(filmTask);

      Log.sysLog("BR Filme einsortieren...");

      films.forEach(film -> {
        if (!Config.getStop()) {
          addFilm(film);
        }
      });

      Log.sysLog("BR Film einsortieren fertig");
    } catch (Exception e) {
      Log.errorLog(516516521, e);
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);
    }

    Log.sysLog("BR fertig");

    meldungThreadUndFertig();
  }

  void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
    Log.sysLog("BR: shutdown pool...");

    try {
      try {
        pool.shutdown();
        if (!pool.awaitTermination(delay, delayUnit)) {
          pool.shutdownNow();
          if (!pool.awaitTermination(delay, delayUnit)) {
            Log.sysLog("BR: Pool nicht beendet");
          }
        }
      } catch (InterruptedException ie) {
        Log.errorLog(974513454, ie);
        pool.shutdownNow();
        Thread.currentThread().interrupt();
      }
    } catch (Exception e) {
      Log.errorLog(974513455, e);
    }
  }

  private RecursiveTask<Set<String>> createAllSendungenOverviewCrawler() {
    return new BrAllSendungenTask(this, forkJoinPool);
  }

  private Callable<Set<String>> createMissedFilmsCrawler() {
    int maximumDays;
    if (CrawlerTool.loadLongMax()) {
      maximumDays = 21;
    } else {
      maximumDays = 7;
    }

    return new BrMissedSendungsFolgenTask(this, maximumDays);
  }

  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    final Callable<Set<String>> missedFilmsTask = createMissedFilmsCrawler();
    final RecursiveTask<Set<String>> sendungenFilmsTask = createAllSendungenOverviewCrawler();
    final Future<Set<String>> missedFilmIds = forkJoinPool.submit(missedFilmsTask);
    forkJoinPool.execute(sendungenFilmsTask);

    final ConcurrentLinkedQueue<String> brFilmIds = new ConcurrentLinkedQueue<>();
    try {
      brFilmIds.addAll(missedFilmIds.get());
      Log.sysLog("BR Anzahl verpasste Sendungen: " + missedFilmIds.get().size());
    } catch (Exception exception) {
      Log.errorLog(782346382, exception);
    }

    try {
      brFilmIds.addAll(sendungenFilmsTask.join());
      Log.sysLog("BR Anzahl: " + sendungenFilmsTask.join().size());
    } catch (Exception exception) {
      Log.errorLog(782346383, exception);
    }

    int max = (brFilmIds.size() / BrSendungDetailsTask.MAXIMUM_URLS_PER_TASK) + 1;
    meldungAddMax(max);

    return new BrSendungDetailsTask(this, brFilmIds);
  }

}
