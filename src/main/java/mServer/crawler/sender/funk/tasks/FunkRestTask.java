package mServer.crawler.sender.funk.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.core.Response;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.PagedElementListDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkRestTask<T>
        extends AbstractJsonRestTask<T, PagedElementListDTO<T>, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(FunkRestTask.class);
  private final transient FunkRestEndpoint<T> restEndpoint;

  public FunkRestTask(final MediathekReader crawler, final FunkRestEndpoint<T> aRestEndpoint) {
    this(crawler, aRestEndpoint, aRestEndpoint.getEndpointUrl().getAsQueue(), Optional.empty());
  }

  public FunkRestTask(
          final MediathekReader crawler,
          final FunkRestEndpoint<T> aRestEndpoint,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    this(crawler, aRestEndpoint, aUrlToCrawlDTOs, Optional.empty());
  }

  public FunkRestTask(
          final MediathekReader crawler,
          final FunkRestEndpoint<T> aRestEndpoint,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
          final Optional<String> authKey) {
    super(crawler, aUrlToCrawlDTOs, authKey);
    restEndpoint = aRestEndpoint;
  }

  @Override
  protected JsonDeserializer<PagedElementListDTO<T>> getParser(final CrawlerUrlDTO aDTO) {
    return restEndpoint.getDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<T>>() {
    }.getType();
  }

  @Override
  protected void handleHttpError(final CrawlerUrlDTO dto, final URI url, final Response response) {
    LOG.fatal(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);
  }

  @Override
  protected void postProcessing(
          final PagedElementListDTO<T> responseObj, final CrawlerUrlDTO crawlerUrl) {
    final Optional<String> nextPageLink = responseObj.getNextPage();

    final Optional<FunkRestTask<T>> subpageCrawler;
    if (nextPageLink.isPresent() && getMaximumSubpages() > 0) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new CrawlerUrlDTO(nextPageLink.get()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }

    taskResults.addAll(responseObj.getElements());
    subpageCrawler.ifPresent(funkChannelTask -> taskResults.addAll(funkChannelTask.join()));
  }

  protected Integer getMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return 3;
    }
    return 1;
  }

  @Override
  protected FunkRestTask<T> createNewOwnInstance(final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new FunkRestTask<>(crawler, restEndpoint, aElementsToProcess, getAuthKey());
  }
}
