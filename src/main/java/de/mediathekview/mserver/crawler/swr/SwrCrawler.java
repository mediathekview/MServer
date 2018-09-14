package de.mediathekview.mserver.crawler.swr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.swr.tasks.SwrDayPageTask;
import de.mediathekview.mserver.crawler.swr.tasks.SwrFilmTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SwrCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(SwrCrawler.class);

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  public SwrCrawler(ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.SWR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();
    try {
/*      shows.addAll(getTopicEntries());
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
*/
      getDaysEntries().forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new SwrFilmTask(this, shows, SwrConstants.URL_BASE);
    } catch (ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in MDR crawler.", ex);
    }
    return null;
  }

  private Set<CrawlerUrlDTO> getTopicEntries() throws ExecutionException, InterruptedException {
    /*final ConcurrentLinkedQueue<CrawlerUrlDTO> letterUrl = new ConcurrentLinkedQueue<>();
    letterUrl.add(new CrawlerUrlDTO(SwrConstants.URL_LETTER_PAGE));

    SwrLetterPageUrlTask letterUrlTask = new SwrLetterPageUrlTask(this, letterUrl);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.addAll(forkJoinPool.submit(letterUrlTask).get());

    SwrLetterPageTask letterTask = new SwrLetterPageTask(this, letterPageUrls);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> topicUrls = new ConcurrentLinkedQueue<>();
    topicUrls.addAll(forkJoinPool.submit(letterTask).get());

    SwrTopicPageTask topicTask = new SwrTopicPageTask(this, topicUrls);

    return forkJoinPool.submit(topicTask).get();*/
    return null;
  }

  private Set<CrawlerUrlDTO> getDaysEntries() throws ExecutionException, InterruptedException {
    ConcurrentLinkedQueue<CrawlerUrlDTO> dayPageUrls = getDayPageUrls();

    SwrDayPageTask dayPageTask = new SwrDayPageTask(this, dayPageUrls, SwrConstants.URL_BASE);
    Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayPageTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayPageUrls() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    LocalDateTime today = LocalDateTime.now();

    for (int i = 0; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection() && i <= SwrConstants.MAX_DAYS_PAST; i++) {
      LocalDateTime day = today.minusDays(i);
      String url = SwrConstants.URL_DAY_PAGE + day.format(DATE_TIME_FORMATTER);

      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }
}
