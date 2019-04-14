package de.mediathekview.mserver.crawler.funk.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.FunkApiUrls;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;

import javax.ws.rs.client.WebTarget;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkChannelTask extends AbstractRestTask<FunkChannelDTO, CrawlerUrlDTO> {
    public FunkChannelTask(final AbstractCrawler crawler, final String authKey) {
        this(crawler, FunkApiUrls.CHANNELS.getAsQueue(crawler), Optional.ofNullable(authKey));
    }

    public FunkChannelTask(
            final AbstractCrawler crawler,
            final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
            final Optional<String> authKey) {
        super(crawler, aUrlToCrawlDTOs, authKey);
    }

    @Override
    protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
        aTarget.
    }

    @Override
    protected AbstractRecrusivConverterTask<FunkChannelDTO, CrawlerUrlDTO> createNewOwnInstance(
            final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
        return new FunkChannelTask(crawler, aElementsToProcess, authKey);
    }
}
