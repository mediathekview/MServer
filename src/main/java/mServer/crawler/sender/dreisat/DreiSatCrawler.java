package mServer.crawler.sender.dreisat;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.zdf.AbstractZdfCrawler;
import mServer.crawler.sender.zdf.ZdfConfiguration;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class DreiSatCrawler extends AbstractZdfCrawler {

  private static final int MAXIMUM_DAYS_HTML_PAST = 14;

  public DreiSatCrawler(FilmeSuchen ssearch, int startPrio) {
    super(Const.DREISAT, ssearch, startPrio);
  }

  /**
   * Loads the api auth token. And because 3Sat only uses one token for search
   * and videos the search auth key will be set as video key too.
   *
   * @return The configuration containing the auth key.
   * @throws ExecutionException   Could be thrown if something went's wrong while
   *                              searching.
   * @throws InterruptedException Could be thrown if the task will be
   *                              interrupted.
   */
  @Override
  protected ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfConfiguration config = super.loadConfiguration();
    config.getSearchAuthKey().ifPresent(config::setVideoAuthKey);
    return config;
  }

  @Override
  protected @NotNull
  String getUrlBase() {
    return DreisatConstants.URL_BASE;
  }

  @Override
  protected String getApiUrlBase() {
    return DreisatConstants.URL_API_BASE;
  }

  @Override
  protected @NotNull
  String getUrlDay() {
    return DreisatConstants.URL_DAY;
  }

  @Override
  protected Collection<CrawlerUrlDTO> getExtraDaysEntries()
    throws ExecutionException, InterruptedException {

    final DreisatDayPageHtmlTask dayTask =
      new DreisatDayPageHtmlTask(getApiUrlBase(), this, getExtraDayUrls(), new JsoupConnection());
    return forkJoinPool.submit(dayTask).get();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getExtraDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i <= MAXIMUM_DAYS_HTML_PAST; i++) {

      final LocalDateTime local = LocalDateTime.now().minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = (DreisatConstants.URL_HTML_DAY).formatted(date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }
}
