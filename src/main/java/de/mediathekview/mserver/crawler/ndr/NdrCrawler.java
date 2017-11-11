package de.mediathekview.mserver.crawler.ndr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.ndr.tasks.NdrSendungVerpasstTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class NdrCrawler extends AbstractCrawler {
  static final String NDR_BASE_URL = "http://www.ndr.de/mediathek";
  private static final String SENDUNG_VERPASST_URL_TEMPLATE =
      NDR_BASE_URL + "/sendung_verpasst/epg1490_date-%s_display-all.html";
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  public NdrCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners);
  }

  @Override
  public Sender getSender() {
    return Sender.NDR;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getSendungVerpasstStartUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < config.getMaximumDaysForSendungVerpasstSection(); i++) {
      urls.add(new CrawlerUrlDTO(String.format(SENDUNG_VERPASST_URL_TEMPLATE,
          LocalDateTime.now().minus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER))));
    }

    return urls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final NdrSendungVerpasstTask sendungVerpasstTask =
        new NdrSendungVerpasstTask(this, getSendungVerpasstStartUrls());
    return null;
  }

}
