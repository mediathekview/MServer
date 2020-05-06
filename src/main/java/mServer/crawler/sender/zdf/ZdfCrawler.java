package mServer.crawler.sender.zdf;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.zdf.tasks.ZdfDayPageHtmlTask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class ZdfCrawler extends AbstractZdfCrawler {

  private static final int MAXIMUM_DAYS_HTML_PAST = 7;

  public ZdfCrawler(FilmeSuchen ssearch, int startPrio) {
    super(Const.ZDF, ssearch, startPrio);
  }

  @Override
  protected @NotNull String getUrlBase() {
    return ZdfConstants.URL_BASE;
  }

  @Override
  protected String getApiUrlBase() {
    return ZdfConstants.URL_API_BASE;
  }

  @Override
  protected @NotNull String getUrlDay() {
    return ZdfConstants.URL_DAY;
  }

  @Override
  protected Collection<CrawlerUrlDTO> getExtraDaysEntries()
    throws ExecutionException, InterruptedException {

    final ZdfDayPageHtmlTask dayTask =
      new ZdfDayPageHtmlTask(getApiUrlBase(), this, getExtraDayUrls(), new JsoupConnection());
    return forkJoinPool.submit(dayTask).get();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getExtraDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i <= MAXIMUM_DAYS_HTML_PAST; i++) {

      final LocalDateTime local = LocalDateTime.now().minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = String.format(ZdfConstants.URL_HTML_DAY, date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }
}
