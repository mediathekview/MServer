package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrRadioPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class Wdr4Crawler extends WdrRadioCrawlerBase {

  public Wdr4Crawler(
      ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.WDR4;
  }

  @Override
  protected Set<WdrTopicUrlDto> getTopicOverviewPages() throws InterruptedException, ExecutionException {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawl = new ConcurrentLinkedQueue<>();
    urlToCrawl.add(new CrawlerUrlDTO(WdrConstants.URL_RADIO_WDR4));

    WdrRadioPageTask radioPageTask = new WdrRadioPageTask(this, urlToCrawl);
    return forkJoinPool.submit(radioPageTask).get();
  }
}
