package mServer.crawler.sender.phoenix;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.MediathekZdf;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.phoenix.tasks.PhoenixFilmDetailTask;
import mServer.crawler.sender.phoenix.tasks.PhoenixOverviewTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhoenixCrawler extends MediathekReader {

  private static final Logger LOG = LogManager.getLogger(PhoenixCrawler.class);

  public static final String SENDERNAME = Const.PHOENIX;

  private final ForkJoinPool forkJoinPool;

  public PhoenixCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);

    final ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
      final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
      worker.setName("PHOENIX-worker-" + worker.getPoolIndex());
      return worker;
    };
    forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4,
            factory, null, true);
  }

  @Override
  protected void addToList() {
    meldungStart();

    try {
      RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
      Set<DatenFilm> films = forkJoinPool.invoke(filmTask);

      Log.sysLog("PHÖNIX Filme einsortieren...");

      films.forEach(film -> {
        if (!Config.getStop()) {
          MediathekZdf.urlTauschen(film, film.getUrl(), mlibFilmeSuchen);
          addFilm(film);
        }
      });

      Log.sysLog("PHÖNIX Film einsortieren fertig");
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);
    }

    Log.sysLog("PHÖNIX fertig");

    meldungThreadUndFertig();
  }

  void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
    pool.shutdown();
    Log.sysLog("PHÖNIX shutdown pool...");
    try {
      if (!pool.awaitTermination(delay, delayUnit)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(delay, delayUnit)) {
          Log.sysLog("PHÖNIX: Pool nicht beendet");
        }
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();

    try {
      shows.addAll(getShows());

      Log.sysLog("PHÖNIX Anzahl: " + shows.size());

      meldungAddMax(shows.size());

      return new PhoenixFilmDetailTask(this, shows, Optional.empty(), PhoenixConstants.URL_BASE, PhoenixConstants.URL_VIDEO_DETAILS_HOST);
    } catch (ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in Phönix crawler.", ex);
    }

    return null;
  }

  private Collection<CrawlerUrlDTO> getShows() throws ExecutionException, InterruptedException {
    // load sendungen page
    CrawlerUrlDTO url = new CrawlerUrlDTO(PhoenixConstants.URL_BASE + PhoenixConstants.URL_OVERVIEW_JSON);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(url);

    final Set<CrawlerUrlDTO> overviewUrls = loadOverviewPages(queue);

    // load sendung overview pages
    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue1 = new ConcurrentLinkedQueue<>();
    queue1.addAll(overviewUrls);
    final Set<CrawlerUrlDTO> filmUrls = loadOverviewPages(queue1);

    return filmUrls;
  }

  private Set<CrawlerUrlDTO> loadOverviewPages(final ConcurrentLinkedQueue<CrawlerUrlDTO> aQueue)
          throws ExecutionException, InterruptedException {
    PhoenixOverviewTask overviewTask = new PhoenixOverviewTask(this, aQueue, Optional.empty(), PhoenixConstants.URL_BASE);
    final Set<CrawlerUrlDTO> urls = forkJoinPool.submit(overviewTask).get();

    return urls;
  }
}
