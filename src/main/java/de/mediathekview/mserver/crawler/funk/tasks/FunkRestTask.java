package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkRestTask<T>
    extends AbstractJsonRestTask<T, PagedElementListDTO<T>, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(FunkRestTask.class);
  protected final transient FunkRestEndpoint<T> restEndpoint;
  protected final int pageNumber;

  public FunkRestTask(final AbstractCrawler crawler, final FunkRestEndpoint<T> aRestEndpoint) {
    this(crawler, aRestEndpoint, aRestEndpoint.getEndpointUrl().getAsQueue(crawler), null, 1);
  }

  public FunkRestTask(
      final AbstractCrawler crawler,
      final FunkRestEndpoint<T> aRestEndpoint,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    this(crawler, aRestEndpoint, aUrlToCrawlDTOs, null, 1);
  }

  public FunkRestTask(
      final AbstractCrawler crawler,
      final FunkRestEndpoint<T> aRestEndpoint,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      @Nullable final String authKey,
      final int pageNumber) {
    super(crawler, aUrlToCrawlDTOs, authKey);
    restEndpoint = aRestEndpoint;
    this.pageNumber = pageNumber;
  }

  @Override
  protected JsonDeserializer<T> getParser(final CrawlerUrlDTO aDTO) {
    return restEndpoint.getDeserializer();
  }

  @Override
  protected Type getType() {
    return restEndpoint.getElementType();
  }

  @Override
  protected void handleHttpError(final CrawlerUrlDTO dto, final URI url, final Response response) {
    crawler.printErrorMessage();
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
    if (nextPageLink.isPresent() && pageNumber < getMaximumSubpages()) {
      final Queue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new CrawlerUrlDTO(nextPageLink.get()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks, pageNumber + 1));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }

    taskResults.addAll(responseObj.getElements());
    subpageCrawler.ifPresent(funkChannelTask -> taskResults.addAll(funkChannelTask.join()));
  }

  protected Integer getMaximumSubpages() {
    return config.getMaximumSubpages();
  }

  @Override
  protected FunkRestTask<T> createNewOwnInstance(final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return createNewOwnInstance(aElementsToProcess, 1);
  }

  protected FunkRestTask<T> createNewOwnInstance(Queue<CrawlerUrlDTO> aElementsToProcess, int pageNumber) {
    return new FunkRestTask<>(crawler, restEndpoint, aElementsToProcess, getAuthKey().orElse(null), pageNumber);
  }
}
