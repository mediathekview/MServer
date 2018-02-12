package de.mediathekview.mserver.crawler.arte;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.arte.tasks.ArteSendungVerpasstTask;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class ArteCrawler extends AbstractCrawler {
  /*
   * Informationen zu den ARTE-URLs: {} sind nur Makierungen, dass es Platzhalter sind, sie gehören
   * nicht zur URL.
   *
   * Allgemeine URL eines Films: (050169-002-A = ID des Films); (die-spur-der-steine = Titel)
   * http://www.arte.tv/de/videos/{050169-002-A}/{die-spur-der-steine}
   *
   * Alle Sendungen: (Deutsch = DE; Französisch = FR)
   * https://api.arte.tv/api/opa/v3/videos?channel={DE}
   *
   * Informationen zum Film: (050169-002-A = ID des Films); (de für deutsch / fr für französisch)
   * https://api.arte.tv/api/player/v1/config/{de}/{050169-002-A}?platform=ARTE_NEXT
   *
   * Zweite Quelle für Informationen zum Film: (050169-002-A = ID des Films); (de für deutsch / fr
   * für französisch) https://api.arte.tv/api/opa/v3/programs/{de}/{050169-002-A}
   */
  private static final String SENDUNG_VERPASST_URL_PATTERN =
      "https://www.arte.tv/guide/api/api/pages/%s/web/tv_guide/?day=%s";
  private static final DateTimeFormatter SENDUNG_VERPASST_DATEFORMATTER =
      DateTimeFormatter.ofPattern("yy-MM-dd");

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

  private Set<CrawlerUrlDTO> generateSendungVerpasstUrls() {
    final Set<CrawlerUrlDTO> sendungVerpasstUrls = new HashSet<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
        + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(); i++) {
      sendungVerpasstUrls.add(new CrawlerUrlDTO(String.format(SENDUNG_VERPASST_URL_PATTERN,
          getLanguage().getLanguageCode(),
          LocalDateTime.now()
              .plus(crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(), ChronoUnit.DAYS)
              .minus(i, ChronoUnit.DAYS).format(SENDUNG_VERPASST_DATEFORMATTER))));
    }
    return sendungVerpasstUrls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    // TODO Search films based on categories.
    return new ArteSendungVerpasstTask(this,
        new ConcurrentLinkedQueue<>(generateSendungVerpasstUrls()));
  }

  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }
}
