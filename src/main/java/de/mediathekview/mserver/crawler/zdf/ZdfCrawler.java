package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ZdfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ZdfCrawler.class);

  public ZdfCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ZDF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      final ZdfConfiguration configuration = loadConfiguration();

      final ConcurrentLinkedQueue<ZdfEntryDto> shows = new ConcurrentLinkedQueue<>();
      shows.addAll(getDaysEntries(configuration));

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new ZdfFilmDetailTask(this, shows, configuration.getVideoAuthKey());
    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ZDF crawler.", ex);
    }
    return null;
  }

  private ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfIndexPageTask task = new ZdfIndexPageTask(this);
    return forkJoinPool.submit(task).get();
  }

  private Set<ZdfEntryDto> getDaysEntries(final ZdfConfiguration configuration)
      throws InterruptedException, ExecutionException {
    final ZdfDayPageTask dayTask =
        new ZdfDayPageTask(this, getDayUrls(), configuration.getSearchAuthKey());
    final Set<ZdfEntryDto> shows = forkJoinPool.submit(dayTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
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
      final String url = String.format(ZdfConstants.URL_DAY, date, date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }
}
