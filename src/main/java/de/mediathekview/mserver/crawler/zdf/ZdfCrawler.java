package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfDayPageHtmlTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTopicPageHtmlTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTopicsPageHtmlTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class ZdfCrawler extends AbstractZdfCrawler {

  private static final int MAXIMUM_DAYS_HTML_PAST = 7;

  public ZdfCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
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
  public Sender getSender() {
    return Sender.ZDF;
  }

  @Override
  public Queue<CrawlerUrlDTO> getTopicsEntries() throws ExecutionException, InterruptedException {
    final Queue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();

    final Queue<CrawlerUrlDTO> letterListUrl = new ConcurrentLinkedQueue<>();
    //letterListUrl.add(new CrawlerUrlDTO(ZdfConstants.URL_TOPICS));
    letterListUrl.add(new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=h"));
    /*final ZdfLetterListHtmlTask letterTask = new ZdfLetterListHtmlTask(this, letterListUrl);
    final Set<CrawlerUrlDTO> letterUrls = forkJoinPool.submit(letterTask).get();
*/
    final ZdfTopicsPageHtmlTask topicsTask = new ZdfTopicsPageHtmlTask(this, new ConcurrentLinkedQueue<>(letterListUrl));
    final Set<CrawlerUrlDTO> topicsUrls = forkJoinPool.submit(topicsTask).get();
    System.out.println(topicsUrls.size());
    final ZdfTopicPageHtmlTask topicTask = new ZdfTopicPageHtmlTask(this, new ConcurrentLinkedQueue<>(topicsUrls));
    shows.addAll(forkJoinPool.submit(topicTask).get());
    return shows;
  }

  @Override
  protected Collection<CrawlerUrlDTO> getExtraDaysEntries()
      throws ExecutionException, InterruptedException {

    final ZdfDayPageHtmlTask dayTask =
        new ZdfDayPageHtmlTask(getApiUrlBase(), this, getExtraDayUrls());
    return forkJoinPool.submit(dayTask).get();
  }

  private Queue<CrawlerUrlDTO> getExtraDayUrls() {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i <= getMaximumDaysPast(); i++) {

      final LocalDateTime local = LocalDateTime.now().minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = String.format(ZdfConstants.URL_HTML_DAY, date);
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
