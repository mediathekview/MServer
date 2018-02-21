package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class Wdr5Crawler extends WdrRadioCrawlerBase {
  
  public Wdr5Crawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.WDR5;
  }

  @Override
  protected Set<WdrTopicUrlDTO> getTopicOverviewPages() {
    Set<WdrTopicUrlDTO> topicOverviews = new HashSet<>();
    topicOverviews.add(new WdrTopicUrlDTO(getSender().getName(), WdrConstants.URL_RADIO_WDR5, false));
    return topicOverviews;
  }
}
