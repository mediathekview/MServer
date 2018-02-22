package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class WdrCosmoCrawler extends WdrRadioCrawlerBase {

  public WdrCosmoCrawler(
      ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.WDR_COSMO;
  }

  @Override
  protected Set<WdrTopicUrlDto> getTopicOverviewPages() {
    Set<WdrTopicUrlDto> topicOverviews = new HashSet<>();
    topicOverviews.add(
        new WdrTopicUrlDto(getSender().getName(), WdrConstants.URL_RADIO_COSMO, false));
    return topicOverviews;
  }
}
