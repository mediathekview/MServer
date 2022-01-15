package de.mediathekview.mserver.crawler.funk.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;
import de.mediathekview.mserver.crawler.funk.FunkCrawler;

import java.util.Queue;

public class FunkChannelsRestTask extends FunkRestTask<FunkChannelDTO> {
  public FunkChannelsRestTask(
          FunkCrawler crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint) {
    super(crawler, funkChannelDTOFunkRestEndpoint);
  }

  public FunkChannelsRestTask(
          FunkCrawler crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint,
          final Queue<CrawlerUrlDTO> urlsToCrawl) {
    super(crawler, funkChannelDTOFunkRestEndpoint, urlsToCrawl);
  }

  @Override
  protected Integer getMaximumSubpages() {
    // load all channels to fill channel list completely
    return Integer.MAX_VALUE;
  }
}
