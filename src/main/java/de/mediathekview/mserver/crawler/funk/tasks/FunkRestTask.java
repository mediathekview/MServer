package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkRestTask<T>
    extends AbstractJsonRestTask<T, PagedElementListDTO<T>, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(FunkRestTask.class);
  private final FunkRestEndpoint<T> restEndpoint;

  public FunkRestTask(final AbstractCrawler crawler, final FunkRestEndpoint<T> aRestEndpoint) {
    this(
        crawler,
        aRestEndpoint,
        aRestEndpoint.getEndpointUrl().getAsQueue(crawler),
        Optional.empty());
  }

  public FunkRestTask(
      final AbstractCrawler crawler,
      final FunkRestEndpoint<T> aRestEndpoint,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    this(crawler, aRestEndpoint, aUrlToCrawlDTOs, Optional.empty());
  }

  public FunkRestTask(
      final AbstractCrawler crawler,
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
    return new TypeToken<Set<T>>() {}.getType();
  }

  @Override
  protected void handleHttpError(final URI url, final Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        String.format(
            "A HTTP error %d occurred when getting REST information from: \"%s\".",
            response.getStatus(), url.toString()));
  }

  @Override
  protected void postProcessing(
      final PagedElementListDTO<T> responseObj, final CrawlerUrlDTO crawlerUrl) {
    final Optional<String> nextPageLink = responseObj.getNextPage();

    final Optional<FunkRestTask<T>> subpageCrawler;
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > 0) {
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

  @Override
  protected FunkRestTask<T> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new FunkRestTask<>(crawler, restEndpoint, aElementsToProcess, authKey);
  }
}
