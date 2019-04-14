package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteCrawlerUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteJsonElementDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.FunkApiUrls;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;
import de.mediathekview.mserver.crawler.funk.json.FunkChannelDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkChannelTask
    extends AbstractFunkRestTask<FunkChannelDTO, FunkChannelDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(FunkChannelTask.class);

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
  protected FunkChannelDeserializer getParser(final CrawlerUrlDTO aDTO) {
    return new FunkChannelDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<FunkChannelDTO>() {}.getType();
  }

  @Override
  protected void handleHttpError(final URI url, final Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        String.format(
            "A HTTP error %d occured when getting REST informations from: \"%s\".",
            response.getStatus(), url.toString()));
  }

  @Override
  protected void postProcessing(final FunkChannelDTO responseObj, final CrawlerUrlDTO crawlerUrl) {
    final Optional<URI> nextPageLink = responseObj.getNextPage();
    final Optional<AbstractUrlTask<ArteJsonElementDto, ArteCrawlerUrlDto>> subpageCrawler;
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > 0) {
      final ConcurrentLinkedQueue<ArteCrawlerUrlDto> nextPageLinks = new ConcurrentLinkedQueue<>();
      final ArteCrawlerUrlDto arteCrawlerUrlDto =
          new ArteCrawlerUrlDto(nextPageLink.get().toString());
      arteCrawlerUrlDto.setCategory(aDTO.getCategory());
      nextPageLinks.add(arteCrawlerUrlDto);
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }

    taskResults.addAll(responseObj.getFoundFilms());
    crawler.incrementMaxCountBySizeAndGetNewSize(responseObj.getFoundFilms().size());
    crawler.updateProgress();
    if (subpageCrawler.isPresent()) {
      taskResults.addAll(subpageCrawler.get().join());
    }
  }

  @Override
  protected AbstractRecrusivConverterTask createNewOwnInstance(
      final ConcurrentLinkedQueue aElementsToProcess) {
    return new FunkChannelTask(crawler, aElementsToProcess, authKey);
  }
}
