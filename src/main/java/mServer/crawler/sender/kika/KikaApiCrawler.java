package mServer.crawler.sender.kika;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.kika.tasks.KikaApiFilmTask;
import mServer.crawler.sender.kika.tasks.KikaApiTopicTask;
import mServer.crawler.sender.orf.TopicUrlDTO;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

public class KikaApiCrawler extends MediathekCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaApiCrawler.class);
  
  JsoupConnection jsoupConnection;

  public KikaApiCrawler(FilmeSuchen ssearch, int startPrio) {
       super(ssearch, Const.KIKA, 0, 1, startPrio);
    jsoupConnection = new JsoupConnection();
  }
  

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    int maxPages = 3;
    
    if (CrawlerTool.loadShort()) {
      maxPages = 3;
    } else if (CrawlerTool.loadLong()) {
      maxPages = 10;
    } else if (CrawlerTool.loadMax()) {
      maxPages = 999;
    } else if (CrawlerTool.loadLongMax()) {
      maxPages = 999;
    }
    
    try {
      final ConcurrentLinkedQueue<TopicUrlDTO> overivew = new ConcurrentLinkedQueue<>();
      overivew.add(new TopicUrlDTO("starting point", KikaApiConstants.ALL_VIDEOS));
      //
      KikaApiTopicTask allvideosTask = new KikaApiTopicTask(this, overivew, 0, maxPages);
      final ConcurrentLinkedQueue<KikaApiFilmDto> allVideos = new ConcurrentLinkedQueue<>();
      allVideos.addAll(forkJoinPool.invoke(allvideosTask));
      Log.sysLog("KIKA: Anzahl sendungsfolgen urls: " + allVideos.size());
      meldungAddMax(allVideos.size());
      return new KikaApiFilmTask(
          this, new ConcurrentLinkedQueue<>(allVideos));
    } catch (final Exception ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    
    return null;

    
  } 
}