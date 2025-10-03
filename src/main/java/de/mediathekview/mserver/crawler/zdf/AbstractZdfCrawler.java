package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfDayPageTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfFilmDetailTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfIndexPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public abstract class AbstractZdfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(AbstractZdfCrawler.class);
  private final Map<String, Sender> partner2Sender;

  protected AbstractZdfCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig,
      final Map<String, Sender> partner2Sender) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
    this.partner2Sender = partner2Sender;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      Set<CrawlerUrlDTO> shows = new HashSet<>();

      final ZdfConfiguration configuration = loadConfiguration();
      if (configuration.getSearchAuthKey().isPresent()
          && configuration.getVideoAuthKey().isPresent()) {

        shows = new HashSet<>(getDaysEntries(configuration));

        if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
          shows.addAll(getTopicsEntries());
        }

        getAndSetMaxCount(shows.size());
      }
      return new ZdfFilmDetailTask(
          this,
          getApiUrlBase(),
          new ConcurrentLinkedQueue<>(shows),
          configuration.getVideoAuthKey().orElse(null),
          partner2Sender);
    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSender().getName(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSender().getName(), ex);
    }
    return null;
  }

  protected ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfIndexPageTask task = new ZdfIndexPageTask(this, getUrlBase());
    return forkJoinPool.submit(task).get();
  }

  protected Queue<CrawlerUrlDTO> getTopicsEntries()
      throws ExecutionException, InterruptedException {
    return new ConcurrentLinkedQueue<>();
  }

  @NotNull
  protected abstract String getUrlBase();

  private Set<CrawlerUrlDTO> getDaysEntries(final ZdfConfiguration configuration)
      throws InterruptedException, ExecutionException {
    final ZdfDayPageTask dayTask =
        new ZdfDayPageTask(
            this, getApiUrlBase(), getDayUrls(), configuration.getSearchAuthKey().orElse(null));
    final Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    final Collection<? extends CrawlerUrlDTO> extraDaysEntries = getExtraDaysEntries();
    shows.addAll(extraDaysEntries);

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  protected Collection<CrawlerUrlDTO> getExtraDaysEntries()
      throws ExecutionException, InterruptedException {
    return new HashSet<>();
  }

  protected abstract String getApiUrlBase();

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

  @NotNull
  protected abstract String getUrlDay();
}
