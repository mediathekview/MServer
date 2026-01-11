package mServer.crawler.sender.arte;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.arte.json.ArteVideoInfoDto;
import mServer.crawler.sender.arte.tasks.ArteDtoVideo2FilmTask;
import mServer.crawler.sender.arte.tasks.ArteVideoInfoTask;
import mServer.crawler.sender.arte.tasks.ArteVideoLinkTask;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.base.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

public class ArteCrawler extends MediathekCrawler {
  private static final Logger LOG = LogManager.getLogger(ArteCrawler.class);
  private final JsoupConnection jsoupConnection;

  public ArteCrawler(FilmeSuchen ssearch, int startPrio) {
    this(ssearch, startPrio, Const.ARTE_DE);
  }

  protected ArteCrawler(FilmeSuchen ssearch, int startPrio, String sender) {
    super(ssearch, sender,/* threads */ 1, /* urlWarten */ 200, startPrio);
    this.jsoupConnection = new JsoupConnection(60, 4);
  }

  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    try {
      final ConcurrentLinkedQueue<TopicUrlDTO> videoUrls = new ConcurrentLinkedQueue<>();
      videoUrls.addAll(createVideosQueue(getLanguage().toString().toLowerCase()));

      final ArteVideoInfoTask aArteRestVideoInfoTask;
      // DO NOT overload - maximumUrlsPerTask used to reduce threads to 4
      aArteRestVideoInfoTask = new ArteVideoInfoTask(this, videoUrls, getMaxPagesForOverview(getLanguage().toString().toLowerCase()));
      final ConcurrentLinkedQueue<ArteVideoInfoDto> videos = new ConcurrentLinkedQueue<>();
      videos.addAll(aArteRestVideoInfoTask.fork().join());
      //
      Log.sysLog(getSendername() + " Anzahl video info: " + videos.size());
      //
      final ConcurrentLinkedQueue<ArteVideoInfoDto> videosWithLink = new ConcurrentLinkedQueue<>();
      final ArteVideoLinkTask aArteRestVideosTask = new ArteVideoLinkTask(this, videos);
      videosWithLink.addAll(aArteRestVideosTask.fork().join());
      //
      Log.sysLog(getSendername() + " Anzahl video links: " + videosWithLink.size());
      //
      return new ArteDtoVideo2FilmTask(this, new ConcurrentLinkedQueue<>(videosWithLink), getSendername());

    } catch (final Exception ex) {
      LOG.fatal("Exception in {} crawler.", getSendername(), ex);
    }
    return null;
  }

  private ConcurrentLinkedQueue<TopicUrlDTO> createVideosQueue(String language) {
    final ConcurrentLinkedQueue<TopicUrlDTO> root = new ConcurrentLinkedQueue<>();
    String rootUrl = String.format(ArteConstants.VIDEOS_URL, 1, language);
    root.add(new TopicUrlDTO("all videos sorted up", rootUrl));
    return root;
  }

  private int getMaxPagesForOverview(String lang) {
    final int maxAvailablePages = getNumberOfAvailablePages(lang);
    final int configuredMaxPages = getMaximumSubpages();
    if (configuredMaxPages > maxAvailablePages) {
      return Math.min(ArteConstants.MAX_POSSIBLE_SUBPAGES, maxAvailablePages);
    } else {
      return Math.min(ArteConstants.MAX_POSSIBLE_SUBPAGES, configuredMaxPages);
    }
  }

  private int getNumberOfAvailablePages(String lang) {
    try {
      String rootUrl = String.format(ArteConstants.VIDEOS_URL, 1, lang);
      String[] path= {"meta", "videos", "pages"};
      final Map<String, String> headers = Map.of(
              "Accept", "application/json",
              "Content-Type", "application/json",
              "Authorization", ArteConstants.API_TOKEN
      );
      JsonElement element = jsoupConnection.requestBodyAsJsonElement(rootUrl, headers);
      Optional<Integer> pages = JsonUtils.getElementValueAsInteger(element, path);
      if (pages.isPresent()) {
        Log.sysLog("ARTE."+ lang + ": number of pages: " + pages.get());
        return pages.get();
      }
    } catch (IOException e) {
      Log.errorLog(45983790, e);
      LOG.error("getMaxPagesForOverview", e);
    }
    return ArteConstants.MAX_POSSIBLE_SUBPAGES;
  }

  protected int getMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return 20;
    } else {
      return 10;
    }
  }
}

  