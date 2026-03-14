package de.mediathekview.mserver.crawler.basic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;

/** A abstract REST api task which requests the given url with the Funk Api settings. */
public abstract class AbstractJsonRestTask<T, R, D extends CrawlerUrlDTO>
    extends AbstractRestTask<T, D> {
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected static final String ENCODING_GZIP = "gzip";
  private static final long serialVersionUID = -1090560363478964885L;
  protected final transient GsonBuilder gsonBuilder;

  protected AbstractJsonRestTask(
      final AbstractCrawler crawler,
      final Queue<D> urlToCrawlDTOs,
      @Nullable final String authKey) {
    super(crawler, urlToCrawlDTOs, authKey);
    gsonBuilder = new GsonBuilder();
  }

  protected abstract Object getParser(D aDTO);

  protected abstract Type getType();

  protected abstract void handleHttpError(D dto, URI url, Response response);

  protected abstract void postProcessing(R aResponseObj, D aDTO);

  @Override
  protected void processRestTarget(final D aDTO, final WebTarget aTarget) {
      gsonBuilder.registerTypeAdapter(getType(), getParser(aDTO));
      final Gson gson = gsonBuilder.create();

      Builder request = aTarget.request();
      final Optional<String> authKey = getAuthKey();
      if (authKey.isPresent()) {
          request = request.header(HEADER_AUTHORIZATION, authKey.get());
      }
      final int maxRetries = 3;
      int attempt = 0;
      while (attempt < maxRetries) {
          attempt++;
          Response response = null;
          try {
              response = createResponse(request, aDTO);
              int status = response.getStatus();
              if (status == 200) {
                  final String jsonOutput = response.readEntity(String.class);
                  final R responseObj = gson.fromJson(jsonOutput, getType());
                  postProcessing(responseObj, aDTO);
                  return;
              }
              if (status == 429 && attempt < maxRetries) {
                  final long proposalWaitMillis = getRetryAfterMillis(response).orElse(1000L) * attempt;
                  long waitMillis = proposalWaitMillis;
                  if (waitMillis < 100) {
                    waitMillis = 100;
                  } else if (waitMillis > 180000 ) {
                    waitMillis = 180000;
                  }
                  //log.debug("Too Many Requests - propsoal: {} waiting: {} ", proposalWaitMillis, waitMillis);
                  Thread.sleep(waitMillis);
                  crawler.getRateLimiter().acquire();
                  continue;
              }
              handleHttpError(aDTO, aTarget.getUri(), response);
              return;
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new RuntimeException("Retry interrupted", e);
          } finally {
              if (response != null) {
                  response.close();
              }
          }
      }
  }
  
  private Optional<Long> getRetryAfterMillis(Response response) {
    String retryAfter = response.getHeaderString("Retry-After");
    if (retryAfter == null) {
        return Optional.empty();
    }
    try {
        long seconds = Long.parseLong(retryAfter);
        return Optional.of(seconds * 1000);
    } catch (NumberFormatException e) {
        return Optional.empty();
    }
}


  protected Response createResponse(final Builder request, final D aDTO) {
	  request.header(ACCEPT_CHARSET, StandardCharsets.UTF_8);
    return request.header(ACCEPT_ENCODING, ENCODING_GZIP).header("User-Agent", "Mozilla").get();
  }
}
