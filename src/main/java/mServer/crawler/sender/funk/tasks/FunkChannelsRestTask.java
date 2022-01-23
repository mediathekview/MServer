package mServer.crawler.sender.funk.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.funk.FunkChannelDTO;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkChannelsRestTask extends FunkRestTask<FunkChannelDTO> {
  public FunkChannelsRestTask(
          MediathekReader crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint) {
    super(crawler, funkChannelDTOFunkRestEndpoint);
  }

  public FunkChannelsRestTask(
          MediathekReader crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlsToCrawl) {
    super(crawler, funkChannelDTOFunkRestEndpoint, urlsToCrawl);
  }

  private FunkChannelsRestTask(
          MediathekReader crawler,
          FunkRestEndpoint<FunkChannelDTO> funkChannelDTOFunkRestEndpoint,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlsToCrawl,
          final int pageNumber) {
    super(crawler, funkChannelDTOFunkRestEndpoint, urlsToCrawl);
  }

  @Override
  protected Integer getMaximumSubpages() {
    // load all channels to fill channel list completely
    return Integer.MAX_VALUE;
  }

  @Override
  protected FunkChannelsRestTask createNewOwnInstance(final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess, int pageNumber) {
    return new FunkChannelsRestTask(crawler, restEndpoint, aElementsToProcess, pageNumber);
  }
}
