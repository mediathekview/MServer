package de.mediathekview.mserver.crawler.dreisat;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.AbstractZdfCrawler;
import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class DreiSatCrawler extends AbstractZdfCrawler {

  private static final int MAXIMUM_DAYS_HTML_PAST = 14;

  public DreiSatCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  /**
   * Loads the api auth token. And because 3Sat only uses one token for search and videos the search
   * auth key will be set as video key too.
   *
   * @return The configuration containing the auth key.
   * @throws ExecutionException Could be thrown if something went's wrong while searching.
   * @throws InterruptedException Could be thrown if the task will be interrupted.
   */
  @Override
  protected ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfConfiguration config = super.loadConfiguration();
    config.getSearchAuthKey().ifPresent(config::setVideoAuthKey);
    return config;
  }

  @Override
  protected @NotNull String getUrlBase() {
    return DreisatConstants.URL_BASE;
  }

  @Override
  protected String getApiUrlBase() {
    return DreisatConstants.URL_API_BASE;
  }

  @Override
  protected @NotNull String getUrlDay() {
    return DreisatConstants.URL_DAY;
  }

  @Override
  public Sender getSender() {
    return Sender.DREISAT;
  }

  @Override
  protected Collection<CrawlerUrlDTO> getExtraDaysEntries()
      throws ExecutionException, InterruptedException {

    final DreisatDayPageHtmlTask dayTask =
        new DreisatDayPageHtmlTask(getApiUrlBase(), this, getExtraDayUrls(), new JsoupConnection());
    return forkJoinPool.submit(dayTask).get();
  }

  private Queue<CrawlerUrlDTO> getExtraDayUrls() {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i <= getMaximumDaysPast(); i++) {

      final LocalDateTime local = LocalDateTime.now().minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = String.format(DreisatConstants.URL_HTML_DAY, date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }

  private int getMaximumDaysPast() {
    final Integer maximumDaysForSendungVerpasstSection =
        crawlerConfig.getMaximumDaysForSendungVerpasstSection();
    if (maximumDaysForSendungVerpasstSection == null
        || maximumDaysForSendungVerpasstSection > MAXIMUM_DAYS_HTML_PAST) {
      return MAXIMUM_DAYS_HTML_PAST;
    }
    return maximumDaysForSendungVerpasstSection;
  }
}
