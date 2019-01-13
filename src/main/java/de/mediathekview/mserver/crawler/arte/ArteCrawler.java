package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.arte.tasks.ArteFilmConvertTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteFilmTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSendungVerpasstTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSubcategoriesTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSubcategoryVideosTask;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArteCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ArteCrawler.class);
  private static final String SENDUNG_VERPASST_URL_PATTERN =
      "https://api.arte.tv/api/opa/v3/videos?channel=%s&arteSchedulingDay=%s";
  private static final DateTimeFormatter SENDUNG_VERPASST_DATEFORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public ArteCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARTE_DE;
  }

  private ConcurrentLinkedQueue<ArteCrawlerUrlDto> generateSendungVerpasstUrls() {
    final ConcurrentLinkedQueue<ArteCrawlerUrlDto> sendungVerpasstUrls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
        + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(); i++) {
      sendungVerpasstUrls.add(new ArteCrawlerUrlDto(String.format(SENDUNG_VERPASST_URL_PATTERN,
          getLanguage().getLanguageCode(),
          LocalDateTime.now()
              .plus(crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(), ChronoUnit.DAYS)
              .minus(i, ChronoUnit.DAYS).format(SENDUNG_VERPASST_DATEFORMATTER))));
    }
    return sendungVerpasstUrls;
  }

  private Set<ArteFilmUrlDto> getCategoriesEntries() throws ExecutionException, InterruptedException {
    ArteSubcategoriesTask subcategoriesTask = new ArteSubcategoriesTask(this, createTopicsOverviewUrl());

    ConcurrentLinkedQueue subcategoriesUrl = new ConcurrentLinkedQueue();
    subcategoriesUrl.addAll(forkJoinPool.submit(subcategoriesTask).get());

    ArteSubcategoryVideosTask subcategoryVideosTask = new ArteSubcategoryVideosTask(this, subcategoriesUrl, ArteConstants.BASE_URL_WWW, getLanguage());
    Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(subcategoryVideosTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmInfos.size());

    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    String url = String.format(ArteConstants.URL_SUBCATEGORIES, getLanguage().toString().toLowerCase());

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }

  private Set<ArteJsonElementDto> getDaysEntries() throws InterruptedException, ExecutionException {

    ArteSendungVerpasstTask dayTask = new ArteSendungVerpasstTask(this, generateSendungVerpasstUrls(),
        Optional.of(ArteConstants.AUTH_TOKEN));
    Set<ArteJsonElementDto> shows = forkJoinPool.submit(dayTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      Set<ArteFilmUrlDto> shows = new HashSet<>();
      //shows.addAll(getDaysEntries());
      getCategoriesEntries().forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      updateProgress();
      return new ArteFilmTask(this, new ConcurrentLinkedQueue<>(shows), getSender(), LocalDateTime.now());
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ARTE crawler.", ex);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }
}
