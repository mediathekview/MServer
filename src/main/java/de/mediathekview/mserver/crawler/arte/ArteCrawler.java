package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.arte.tasks.ArteDtoVideo2FilmTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoInfoTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoLinkTask;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ArteCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(ArteCrawler.class);

  public ArteCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARTE_DE;
  }
  
  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      final ArteVideoInfoTask aArteRestVideoInfoTask;
      // DO NOT overload - maximumUrlsPerTask used to reduce threads to 4
      if (ArteConstants.USE_SAME_THREAD) {
        aArteRestVideoInfoTask = new ArteVideoInfoTask(this, createVideosQueue());
      } else {
        aArteRestVideoInfoTask = new ArteVideoInfoTask(this, createAllVideosQueue());
      }
      final Queue<ArteVideoInfoDto> videos = new ConcurrentLinkedQueue<>();
      videos.addAll(aArteRestVideoInfoTask.fork().join());
      //
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_COUNT, getSender().getName(), videos.size());
      getAndSetMaxCount(videos.size());
      updateProgress();
      //
      final Queue<ArteVideoInfoDto> videosWithLink = new ConcurrentLinkedQueue<>();
      final ArteVideoLinkTask aArteRestVideosTask = new ArteVideoLinkTask(this, videos);
      videosWithLink.addAll(aArteRestVideosTask.fork().join());
      //
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), videosWithLink.size());
      getAndSetMaxCount(videosWithLink.size());
      updateProgress();
      //
      return new ArteDtoVideo2FilmTask(this, new ConcurrentLinkedQueue<>(videosWithLink));
      
    } catch (final Exception ex) {
      LOG.fatal("Exception in {} crawler.", getSender(), ex);
    }
    return null;
  }

  private Queue<TopicUrlDTO> createAllVideosQueue() {
    int maxPages = getMaxPagesForOverview();
    final Queue<TopicUrlDTO> root = new ConcurrentLinkedQueue<>();
    for(int pages = 1; pages < maxPages; pages++) {
      String rootUrl = String.format(ArteConstants.VIDEOS_URL,pages, getLanguage().toString().toLowerCase());
      root.add(new TopicUrlDTO("all videos " + pages,rootUrl));
    }
    return root;
  }
  
  private Queue<TopicUrlDTO> createVideosQueue() {
    int maxPages = getMaxPagesForOverview();
    final Queue<TopicUrlDTO> root = new ConcurrentLinkedQueue<>();
    String rootUrl = String.format(ArteConstants.VIDEOS_URL,1, getLanguage().toString().toLowerCase());
    root.add(new TopicUrlDTO("all videos",rootUrl));
    return root;
  }
  
  private int getMaxPagesForOverview() {
    final int naturalLimit = Math.min(100, getCrawlerConfig().getMaximumSubpages());
    String rootUrl = String.format(ArteConstants.VIDEOS_URL, 1, getLanguage().toString().toLowerCase());
    String path[] = {"meta", "videos", "pages"};
    try {
      final Map<String, String> headers = Map.of(
          "Accept", "application/json",
          "Content-Type", "application/json",
          "Authorization", ArteConstants.API_TOKEN
      );
      JsonElement element = getConnection().requestBodyAsJsonElement(rootUrl, headers);
      Optional<Integer> pages = JsonUtils.getElementValueAsInteger(element, path);
      if (pages.isPresent()) {
        return Math.min(pages.get(), naturalLimit);
      }
    } catch (IOException e) {
      LOG.error("getMaxPagesForOverview", e);
    }
    return naturalLimit;
  }

}
