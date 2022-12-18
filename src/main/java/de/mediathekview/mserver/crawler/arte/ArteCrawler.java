package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.arte.tasks.*;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ArteCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ArteCrawler.class);
  private static final DateTimeFormatter SENDUNG_VERPASST_DATEFORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

  private Queue<CrawlerUrlDTO> generateSendungVerpasstUrls(ArteLanguage language) {
    final Queue<CrawlerUrlDTO> sendungVerpasstUrls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
        i
            < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
                + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture();
        i++) {
      sendungVerpasstUrls.add(
          new CrawlerUrlDTO(
              String.format(
                  ArteConstants.DAY_PAGE_URL,
                  language.getLanguageCode().toLowerCase(),
                  LocalDateTime.now()
                      .plus(
                          crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(),
                          ChronoUnit.DAYS)
                      .minus(i, ChronoUnit.DAYS)
                      .format(SENDUNG_VERPASST_DATEFORMATTER))));
    }
    return sendungVerpasstUrls;
  }

  private Set<ArteFilmUrlDto> getCategoriesEntries(ArteLanguage language)
      throws ExecutionException, InterruptedException {
    final ArteSubcategoriesTask subcategoriesTask =
        new ArteSubcategoriesTask(this, createTopicsOverviewUrl(language));

    final Queue<TopicUrlDTO> subcategoriesUrl = new ConcurrentLinkedQueue<>();
    subcategoriesUrl.addAll(forkJoinPool.submit(subcategoriesTask).get());

    final ArteSubcategoryVideosTask subcategoryVideosTask =
        new ArteSubcategoryVideosTask(
            this, subcategoriesUrl, ArteConstants.BASE_URL_WWW, language);
    final Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(subcategoryVideosTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmInfos.size());

    return filmInfos;
  }

  private Queue<CrawlerUrlDTO> createTopicsOverviewUrl(ArteLanguage language) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url =
        String.format(ArteConstants.URL_SUBCATEGORIES, language.getLanguageCode().toLowerCase());

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }

  private Set<ArteFilmUrlDto> getVideoListVideos(ArteLanguage language, String videoListType)
      throws ExecutionException, InterruptedException {
    final ArteAllVideosTask videosTask =
        new ArteAllVideosTask(this, createVideoListUrls(language, videoListType), language);
    final Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(videosTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmInfos.size());

    return filmInfos;
  }

  private Queue<CrawlerUrlDTO> createVideoListUrls(ArteLanguage language, String videoListType) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    for (int i = 1; i <= getCrawlerConfig().getMaximumSubpages(); i++) {
      final String url =
          String.format(
              ArteConstants.URL_VIDEO_LIST,
              ArteConstants.BASE_URL_WWW,
              language.getLanguageCode().toLowerCase(),
              videoListType,
              i);

      urls.add(new CrawlerUrlDTO(url));
    }
    return urls;
  }

  private Set<ArteFilmUrlDto> getDaysEntries(ArteLanguage language) throws InterruptedException, ExecutionException {

    final ArteDayPageTask dayTask =
        new ArteDayPageTask(this, generateSendungVerpasstUrls(language), language);
    final Set<ArteFilmUrlDto> shows = forkJoinPool.submit(dayTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final ArteLanguage language = getLanguage();
    try {
      final Set<ArteFilmUrlDto> shows = new HashSet<>();
      if (isDayEntriesEnabled()) {
        shows.addAll(getDaysEntries(language));
      }
      getVideoListVideos(language, ArteConstants.VIDEO_LIST_TYPE_RECENT).forEach(shows::add);

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        getCategoriesEntries(language).forEach(shows::add);

        getVideoListVideos(language, ArteConstants.VIDEO_LIST_TYPE_LAST_CHANCE)
            .forEach(shows::add);
      }

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      updateProgress();
      return new ArteFilmTask(
          this, new ConcurrentLinkedQueue<>(shows), getSender(), LocalDateTime.now());
    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSender().getName(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSender().getName(), ex);
    }
    return null;
  }

  protected boolean isDayEntriesEnabled() {
    return true;
  }

  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }
}
