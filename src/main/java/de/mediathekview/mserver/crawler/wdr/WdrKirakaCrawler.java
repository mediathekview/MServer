package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrRadioPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class WdrKirakaCrawler extends WdrRadioCrawlerBase {

  public WdrKirakaCrawler(
      final ForkJoinPool forkJoinPool,
      final Collection<MessageListener> messageListeners,
      final Collection<SenderProgressListener> progressListeners,
      final MServerConfigManager rootConfig) {
    super(forkJoinPool, messageListeners, progressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.WDR_KIRAKA;
  }

  @Override
  protected Set<WdrTopicUrlDto> getTopicOverviewPages()
      throws InterruptedException, ExecutionException {
    final Queue<CrawlerUrlDTO> urlToCrawl = new ConcurrentLinkedQueue<>();
    urlToCrawl.add(new CrawlerUrlDTO(WdrConstants.URL_RADIO_KIRAKA));

    final WdrRadioPageTask radioPageTask = new WdrRadioPageTask(this, urlToCrawl, jsoupConnection);
    return forkJoinPool.submit(radioPageTask).get();
  }
}
