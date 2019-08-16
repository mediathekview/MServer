package mServer.crawler.sender.sr;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.sr.tasks.SrFilmDetailTask;
import mServer.crawler.sender.sr.tasks.SrTopicArchivePageTask;
import mServer.crawler.sender.sr.tasks.SrTopicsOverviewPageTask;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;

public class SrCrawler extends MediathekCrawler {

  public static final String SENDERNAME = Const.SR;

  public SrCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    final ConcurrentLinkedQueue<SrTopicUrlDTO> filmDtos = new ConcurrentLinkedQueue<>();
    try {
      final SrTopicsOverviewPageTask overviewTask = new SrTopicsOverviewPageTask();
      final ConcurrentLinkedQueue<SrTopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

      final SrTopicArchivePageTask archiveTask = new SrTopicArchivePageTask(this, shows);
      filmDtos.addAll(forkJoinPool.submit(archiveTask).get());

      Log.sysLog("SR Anzahl: " + filmDtos.size());

      meldungAddMax(filmDtos.size());

    } catch (InterruptedException | ExecutionException exception) {
      Log.errorLog(56146546, exception);
    }
    return new SrFilmDetailTask(this, filmDtos);
  }
}
