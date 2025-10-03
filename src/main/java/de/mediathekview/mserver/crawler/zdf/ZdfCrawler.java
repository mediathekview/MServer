package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ZdfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ZdfCrawler.class);
  private static final int MAX_LETTER_PAGEGS = 27;

  private static final String AUTH_KEY = "aa3noh4ohz9eeboo8shiesheec9ciequ9Quah7el";

  public ZdfCrawler(
      ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ZDF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        final Set<ZdfFilmDto> shows = new HashSet<>();

        ZdfLetterPageTask letterPageTask =
            new ZdfLetterPageTask(this, createLetterPageUrls(), AUTH_KEY);
        final Set<ZdfTopicUrlDto> topicUrls = forkJoinPool.submit(letterPageTask).get();

        printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), topicUrls.size());

        final ZdfPubFormTask pubFormTask = new ZdfPubFormTask(this, createPubFormUrls(), AUTH_KEY);
        final Set<ZdfPubFormResult> pubFormUrls = forkJoinPool.submit(pubFormTask).get();

        printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT,
            getSender().getName() + " - PubForm:",
            pubFormUrls.size());

        pubFormUrls.forEach(
            pubFormResult -> {
              topicUrls.addAll(pubFormResult.getTopics().getElements());
              shows.addAll(pubFormResult.getFilms());
            });
        printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT,
            getSender().getName() + " - PubForm-Topics integrated: ",
            topicUrls.size());

        ZdfTopicSeasonTask topicSeasonTask =
            new ZdfTopicSeasonTask(this, new ConcurrentLinkedQueue<>(topicUrls), AUTH_KEY);
        shows.addAll(forkJoinPool.submit(topicSeasonTask).get());

        printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

        return new ZdfFilmTask(this, new ConcurrentLinkedQueue<>(shows), AUTH_KEY);
      } else {
        final ZdfConfiguration configuration = loadConfiguration();
        if (configuration.getSearchAuthKey().isPresent()
            && configuration.getVideoAuthKey().isPresent()) {
          Set<CrawlerUrlDTO> shows = new HashSet<>(getDaysEntries(configuration));
          printMessage(
                  ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

          return new ZdfFilmDetailTask(
              this,
              getApiUrlBase(),
              new ConcurrentLinkedQueue<>(shows),
              configuration.getVideoAuthKey().orElse(""), ZdfConstants.PARTNER_TO_SENDER);
        }
      }
    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSender().getName(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSender().getName(), ex);
    }
    return null;
  }

  private Queue<ZdfPubFormDto> createPubFormUrls() {
    Queue<ZdfPubFormDto> urls = new ConcurrentLinkedQueue<>();
    ZdfConstants.SPECIAL_COLLECTION_IDS.forEach(
        (collectionId, topic) -> {
          final String url =
              ZdfUrlBuilder.buildTopicNoSeasonUrl(
                  ZdfConstants.EPISODES_PAGE_SIZE, collectionId, ZdfConstants.NO_CURSOR);
          urls.add(new ZdfPubFormDto(topic, collectionId, url));
        });
    return urls;
  }

  private Queue<ZdfLetterDto> createLetterPageUrls() {
    final Queue<ZdfLetterDto> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < MAX_LETTER_PAGEGS; i++) {
      urls.add(new ZdfLetterDto(i, ZdfUrlBuilder.buildLetterPageUrl(ZdfConstants.NO_CURSOR, i)));
    }

    return urls;
  }

  protected ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfIndexPageTask task = new ZdfIndexPageTask(this, getUrlBase());
    return forkJoinPool.submit(task).get();
  }

  private Set<CrawlerUrlDTO> getDaysEntries(final ZdfConfiguration configuration)
          throws InterruptedException, ExecutionException {
    final ZdfDayPageTask dayTask =
            new ZdfDayPageTask(
                    this, getApiUrlBase(), getDayUrls(), configuration.getSearchAuthKey().orElse(null));
    final Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private Queue<CrawlerUrlDTO> getDayUrls() {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
         i
                 <= crawlerConfig.getMaximumDaysForSendungVerpasstSection()
                 + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture();
         i++) {

      final LocalDateTime local =
              LocalDateTime.now()
                      .plus(crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(), ChronoUnit.DAYS)
                      .minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = String.format(getUrlDay(), date, date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }

  private @NotNull String getUrlBase() {
    return ZdfConstants.URL_BASE;
  }

  private String getApiUrlBase() {
    return ZdfConstants.URL_API_BASE;
  }

  private @NotNull String getUrlDay() {
    return ZdfConstants.URL_DAY;
  }
}
