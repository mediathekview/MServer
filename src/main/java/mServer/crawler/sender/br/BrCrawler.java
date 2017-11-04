package mServer.crawler.sender.br;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrCrawler extends MediathekReader {

  public final static String SENDERNAME = Const.BR;
  public final static String BASE_URL = "https://www.br.de/mediathek/";
  private final static Logger LOG = LogManager.getLogger(BrCrawler.class);

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

      LOG.info("BR Filme einsortieren...");

      films.forEach(film -> {
        if(!Config.getStop()) {
          addFilm(film);
        }
      });

      LOG.info("BR Film einsortieren fertig");
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);
    }
    
    LOG.info("BR fertig");
    
    meldungThreadUndFertig();
  }

  void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
    pool.shutdown();
    try {
      if (!pool.awaitTermination(delay, delayUnit)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(delay, delayUnit)) {
          LOG.info("Pool nicht beendet");
        }
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
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
      LOG.debug("BR Anzahl verpasste Sendungen: " + missedFilmIds.get().size());
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong on gathering the missed Films", exception);
    }
    brFilmIds.addAll(sendungenFilmsTask.join());
    LOG.debug("BR Anzahl: " + sendungenFilmsTask.join().size());

    int max = (brFilmIds.size() / BrSendungDetailsTask.MAXIMUM_URLS_PER_TASK) + 1;
    meldungAddMax(max);

    return new BrSendungDetailsTask(this, brFilmIds);
  }

}
