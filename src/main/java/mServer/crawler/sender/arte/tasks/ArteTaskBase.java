package mServer.crawler.sender.arte.tasks;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public abstract class ArteTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final Logger LOG = LogManager.getLogger(ArteTaskBase.class);
  private transient GsonBuilder gsonBuilder;

  private static final RateLimiter LIMITER;

  static {
    LIMITER = RateLimiter.create(2);
  }

  protected ArteTaskBase(
          final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<D> aUrlToCrawlDtos,
          final Optional<String> authToken) {
    super(aCrawler, aUrlToCrawlDtos, authToken);
    gsonBuilder = new GsonBuilder();

    // limiter bei ersten aufruf bauen?
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return 1000;
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected void deregisterJsonDeserializer() {
    gsonBuilder = new GsonBuilder();
  }

  /**
   * Try to request a WebTarget resource Retry 5 time to get the data in case of http error 404
   * error is not a reason for retry and null is returned Mainly this is to take care of ARTE 429
   * (too many request) response
   *
   * @param aTarget
   * @return
   */
  protected String requestWebtarget(final WebTarget aTarget) {
    Response response = null;
    WebTarget webTargetClone = aTarget;
    int retry = 0;
    String responseAsString = null;
    int status = 0;
    while (retry < 5) {
      try {
        response = executeRequest(webTargetClone);
        status = response.getStatus();
        if (status == 200) {
          responseAsString = response.readEntity(String.class);
          FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.SUM_DATA_BYTE, responseAsString.length());
          FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.SUM_TRAFFIC_BYTE, responseAsString.length());

          return responseAsString;
        } else if (status != 429) {
          LOG.warn(
                  "ArteTaskBase: attempt {} failed {}} for url {}",
                  retry,
                  status,
                  webTargetClone.getUri());
          FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
          return null;
        } else {
          if (retry == 4) {
            LOG.error(
                    "ArteTaskBase: attempt {} failed {} for url {}",
                    retry,
                    status,
                    webTargetClone.getUri());
            FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
          } else {
            LOG.warn(
                    "ArteTaskBase: attempt {} failed {} for url {}",
                    retry,
                    status,
                    webTargetClone.getUri());
            Log.sysLog(status + ": " + webTargetClone.getUri().toString());
            FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLVERSUCHE);
          }
          // Wartezeit von 60s aus Header Retry-After
          try {
            TimeUnit.MILLISECONDS.sleep(60000);
          } catch (InterruptedException ignored) {
          }
          retry++;
        }
      } finally {
        try {
          response.close();
        } catch (final Exception e) {
        }
      }
      webTargetClone = createWebTarget(webTargetClone.getUri().toString());
    }
    return responseAsString;
  }

  protected <O> Optional<O> deserializeOptional(final WebTarget aTarget, final Type aType) {
    final String inputString = requestWebtarget(aTarget);
    if (inputString != null) {
      final Gson gson = gsonBuilder.create();
      return gson.fromJson(inputString, aType);
    }
    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget aTarget, final Type aType) {
    final String inputString = requestWebtarget(aTarget);
    if (inputString != null) {
      final Gson gson = gsonBuilder.create();
      return gson.fromJson(inputString, aType);
    }
    return null;
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    final Optional<String> authKey = getAuthKey();
    if (authKey.isPresent()) {
      request = request.header(HEADER_AUTHORIZATION, authKey.get());
    }

    if (aTarget.getUri().getHost().contains("api.arte.tv")) {
      LIMITER.acquire();
    }

    return request
            .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
            .header(HEADER_ACCEPT, APPLICATION_JSON)
            .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
            .header("User-Agent", "Mozilla")
            .get();
  }
}
