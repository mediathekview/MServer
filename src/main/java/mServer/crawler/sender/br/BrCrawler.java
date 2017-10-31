package mserver.crawler.sender.br;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrCrawler extends MediathekReader {
  public final static String SENDERNAME = Const.BR;
  private static final Logger LOG = LogManager.getLogger(BrCrawler.class);
  public static final String BASE_URL = "https://www.br.de/mediathek/";

  private final ForkJoinPool forkJoinPool;
  
  public BrCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 1, 100, startPrio);
        
    forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4);
  }

  @Override
  protected void addToList() {
    meldungStart();
    
    RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
    Set<DatenFilm> films = forkJoinPool.invoke(filmTask);
    
    films.forEach(film -> {
      if(!Config.getStop()) {
        addFilm(film);
      }
    });
    
    meldungThreadUndFertig();
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
