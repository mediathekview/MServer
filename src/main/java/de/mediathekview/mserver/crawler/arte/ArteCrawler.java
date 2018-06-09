package de.mediathekview.mserver.crawler.arte;

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
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.arte.tasks.ArteFilmConvertTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSendungVerpasstTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSubcategoryVideosTask;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSubcategorysTask;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class ArteCrawler extends AbstractCrawler {
  private static final String AUTH_TOKEN =
      "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
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

  private Set<ArteCrawlerUrlDto> generateSendungVerpasstUrls() {
    final Set<ArteCrawlerUrlDto> sendungVerpasstUrls = new HashSet<>();
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

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Set<ArteJsonElementDto> sendungsfolgen = new HashSet<>();
    final ForkJoinTask<Set<ArteCrawlerUrlDto>> subcategoryVideoUrls =
        forkJoinPool.submit(new ArteSubcategorysTask(this, getLanguage(), AUTH_TOKEN));


    final ArteSendungVerpasstTask sendungVerpasstTask = new ArteSendungVerpasstTask(this,
        new ConcurrentLinkedQueue<>(generateSendungVerpasstUrls()), Optional.of(AUTH_TOKEN));
    forkJoinPool.execute(sendungVerpasstTask);

    Optional<ArteSubcategoryVideosTask> subcategoryVideosTask;
    try {
      subcategoryVideosTask = Optional.of(new ArteSubcategoryVideosTask(this,
          new ConcurrentLinkedQueue<>(subcategoryVideoUrls.get()), Optional.of(AUTH_TOKEN)));
    } catch (final ExecutionException | InterruptedException exception) {
      printErrorMessage();
      LOG.fatal("Somethign went really wrong on getting the subcategory video urls for ARTE",
          exception);
      subcategoryVideosTask = Optional.empty();
    }


    if (subcategoryVideosTask.isPresent()) {
      forkJoinPool.execute(subcategoryVideosTask.get());
    }

    sendungsfolgen.addAll(sendungVerpasstTask.join());
    if (subcategoryVideosTask.isPresent()) {
      sendungsfolgen.addAll(subcategoryVideosTask.get().join());
    }
    updateProgress();
    return new ArteFilmConvertTask(this, new ConcurrentLinkedQueue<>(sendungsfolgen), AUTH_TOKEN,
        getLanguage());
  }

  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }
}
