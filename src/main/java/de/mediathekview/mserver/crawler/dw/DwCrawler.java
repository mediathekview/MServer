package de.mediathekview.mserver.crawler.dw;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.tasks.DWFilmDetailsTask;
import de.mediathekview.mserver.crawler.dw.tasks.DWUebersichtTagTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class DwCrawler extends AbstractCrawler {
  // TODO: add me to config
  private static final int MAX_RESULTS_PER_PAGE = 500;
  private static final int MAX_DAYS_PER_CHUNK = 7;
  //
  public static final String BASE_URL = "https://www.dw.com";
  private static final String ALLE_INHALTE_URL_NACH_TAG =
      BASE_URL + "/de/media-center/alle-inhalte/s-100814?filter=&type=18&from=%s&to=%s&sort=date&results=" + MAX_RESULTS_PER_PAGE;

  public DwCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.DW;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    //
    final Queue<CrawlerUrlDTO> alleSeiten = new ConcurrentLinkedQueue<>();
    final int fromDays = -1 * crawlerConfig.getMaximumDaysForSendungVerpasstSection();
    final int toDays = crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture() + MAX_DAYS_PER_CHUNK;
    for (int i = fromDays; i < toDays; i++) {
      final LocalDateTime fromDate = LocalDateTime.now().plus( i, ChronoUnit.DAYS);
      final LocalDateTime toDate = LocalDateTime.now().plus( i + MAX_DAYS_PER_CHUNK, ChronoUnit.DAYS);
      final String fromDateString = fromDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      final String toDateString = toDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
      final String url = String.format(ALLE_INHALTE_URL_NACH_TAG, fromDateString, toDateString);
      alleSeiten.add(new CrawlerUrlDTO(url));
      i += MAX_DAYS_PER_CHUNK;
    }
    //
    final DWUebersichtTagTask alleSendungenTask = new DWUebersichtTagTask(this, alleSeiten);
    final Set<URL> sendungFolgenUrls = forkJoinPool.invoke(alleSendungenTask);

    return new DWFilmDetailsTask(
        this,
        new ConcurrentLinkedQueue<>(
            sendungFolgenUrls.stream().map(CrawlerUrlDTO::new).collect(Collectors.toList())),
        DwCrawler.BASE_URL
        );
  }
}
