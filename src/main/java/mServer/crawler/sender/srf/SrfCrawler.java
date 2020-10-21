package mServer.crawler.sender.srf;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.srf.tasks.SrfFilmDetailTask;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import mServer.crawler.sender.srf.tasks.SrfTopicOverviewTask;
import mServer.crawler.sender.srf.tasks.SrfTopicsOverviewTask;
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
      final ConcurrentLinkedQueue<CrawlerUrlDTO> topicsUrls = new ConcurrentLinkedQueue<>();
      topicsUrls.add(new CrawlerUrlDTO(SrfConstants.OVERVIEW_PAGE_URL));
      final SrfTopicsOverviewTask overviewTask = new SrfTopicsOverviewTask(this, topicsUrls);
      final ConcurrentLinkedQueue<TopicUrlDTO> topicUrls = new ConcurrentLinkedQueue<>(forkJoinPool.submit(overviewTask).get());

      final SrfTopicOverviewTask task = new SrfTopicOverviewTask(this, topicUrls, SrfConstants.BASE_URL, getMaxSubPages());
      forkJoinPool.execute(task);

      final ConcurrentLinkedQueue<CrawlerUrlDTO> dtos
              = new ConcurrentLinkedQueue<>();
      dtos.addAll(task.join());

      Log.sysLog("SRF Anzahl: " + dtos.size());
      meldungAddMax(dtos.size());

      return new SrfFilmDetailTask(this, dtos);

    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in SRF crawler.", ex);
      Log.errorLog(745615611, ex);
    }
    return null;
  }

  private static int getMaxSubPages() {
    if (CrawlerTool.loadLongMax()) {
      return 5;
    }

    return 1;
  }
}
