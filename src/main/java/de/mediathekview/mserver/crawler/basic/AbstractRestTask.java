package de.mediathekview.mserver.crawler.basic;

import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * This task is based on {@link AbstractUrlTask} which takes a {@link Queue} of {@link D} and loads
 * the URL with REST as {@link WebTarget}.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to create the result
 *     objects.
 */
public abstract class AbstractRestTask<T, D extends CrawlerUrlDTO> extends AbstractUrlTask<T, D> {
  protected static final String ENCODING_GZIP = "gzip";
  protected static final String HEADER_ACCEPT = "Accept";
  protected static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  protected static final String HEADER_AUTHORIZATION = "Authorization";
  protected static final String HEADER_CONTENT_TYPE = "Content-Type";
  protected static final String AUTHORIZATION_BEARER = "Bearer ";
  protected static final String APPLICATION_JSON = "application/json";
  private static final long serialVersionUID = 2590729915326002860L;
  private final transient String authKey;
  private final transient Client client;

  protected AbstractRestTask(
      final AbstractCrawler aCrawler,
      final Queue<D> aUrlToCrawlDTOs,
      @Nullable final String aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs);
    authKey = aAuthKey;

    client =
        ClientBuilder.newBuilder()
            .readTimeout(crawler.getCrawlerConfig().getSocketTimeoutInSeconds(), TimeUnit.SECONDS)
            .build();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }

  /**
   * In this method you have to use the {@link WebTarget} to create a object of the return type
   * {@link T}. Add the results to {@link AbstractUrlTask#taskResults}.
   *
   * @param aDTO A DTO containing at least the URL of the given document.
   * @param aTarget The {@link WebTarget}.
   */
  protected abstract void processRestTarget(D aDTO, WebTarget aTarget);

  @Override
  protected void processElement(final D aDTO) {
    final WebTarget target = createWebTarget(aDTO.getUrl());
    processRestTarget(aDTO, target);
  }

  /**
   * Creates a {@link WebTarget}.
   *
   * @param aUrl the url.
   * @return the {@link WebTarget} to access the url.
   */
  protected WebTarget createWebTarget(final String aUrl) {
    crawler.getRateLimiter().acquire();
    return client.target(aUrl);
  }

  protected Optional<String> getAuthKey() {
    return Optional.ofNullable(authKey);
  }
}
