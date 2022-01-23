package de.mediathekview.mserver.crawler.funk.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;

import java.util.Queue;

public class FunkChannelsRestTask extends FunkRestTask<FunkChannelDTO> {
  public FunkChannelsRestTask(
          AbstractCrawler crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint) {
    super(crawler, funkChannelDTOFunkRestEndpoint);
  }

  public FunkChannelsRestTask(
          AbstractCrawler crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint,
          final Queue<CrawlerUrlDTO> urlsToCrawl) {
    super(crawler, funkChannelDTOFunkRestEndpoint, urlsToCrawl);
  }

  private FunkChannelsRestTask(
          AbstractCrawler crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint,
          final Queue<CrawlerUrlDTO> urlsToCrawl,
          final int pageNumber) {
    super(crawler, funkChannelDTOFunkRestEndpoint, urlsToCrawl, null, pageNumber);
  }

  @Override
  protected Integer getMaximumSubpages() {
    // load all channels to fill channel list completely
    return Integer.MAX_VALUE;
  }

  @Override
  protected FunkChannelsRestTask createNewOwnInstance(final Queue<CrawlerUrlDTO> aElementsToProcess, int pageNumber) {
    return new FunkChannelsRestTask(crawler, restEndpoint, aElementsToProcess, pageNumber);
  }
}
