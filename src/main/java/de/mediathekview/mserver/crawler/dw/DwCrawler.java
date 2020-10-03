package de.mediathekview.mserver.crawler.dw;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.tasks.DWFilmDetailsTask;
import de.mediathekview.mserver.crawler.dw.tasks.DWUebersichtTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.net.URL;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class DwCrawler extends AbstractCrawler {

  public static final String BASE_URL = "https://www.dw.com";
  private static final String SENDUNG_VERPASST_URL =
      BASE_URL + "/de/media-center/sendung-verpasst/s-100815";
  private static final String ALLE_INHALTE_URL =
      BASE_URL + "/de/media-center/alle-inhalte/s-100814";

  JsoupConnection jsoupConnection;

  public DwCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);

    jsoupConnection = new JsoupConnection();
  }

  @Override
  public Sender getSender() {
    return Sender.DW;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Queue<CrawlerUrlDTO> sendungVerpasstStartUrls = new ConcurrentLinkedQueue<>();
    sendungVerpasstStartUrls.offer(new CrawlerUrlDTO(SENDUNG_VERPASST_URL));
    final DWUebersichtTask sendungverpasstUebersichtTask =
        new DWUebersichtTask(this, sendungVerpasstStartUrls, jsoupConnection);
    final Set<URL> sendungverpasstFolgenUrls = forkJoinPool.invoke(sendungverpasstUebersichtTask);

    final Queue<CrawlerUrlDTO> startUrls = new ConcurrentLinkedQueue<>();
    startUrls.offer(new CrawlerUrlDTO(ALLE_INHALTE_URL));
    startUrls.addAll(
        sendungverpasstFolgenUrls.stream().map(CrawlerUrlDTO::new).collect(Collectors.toList()));
    final DWUebersichtTask uebersichtTask = new DWUebersichtTask(this, startUrls, jsoupConnection);
    final Set<URL> sendungFolgenUrls = forkJoinPool.invoke(uebersichtTask);

    return new DWFilmDetailsTask(
        this,
        new ConcurrentLinkedQueue<>(
            sendungFolgenUrls.stream().map(CrawlerUrlDTO::new).collect(Collectors.toList())),
        DwCrawler.BASE_URL,
        jsoupConnection);
  }
}
