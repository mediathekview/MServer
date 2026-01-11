package mServer.crawler.sender.arte.tasks;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.tool.MserverDaten;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public abstract class ArteRateLimitedJsonRestTask<T, R, D extends CrawlerUrlDTO> extends AbstractJsonRestTask<T, R, D> {
  private static final long serialVersionUID = 1L;
  private static final RateLimiter rateLimiter = RateLimiter.create(MserverDaten.getArteRateLimit());
  private static final RateLimiter opaApirateLimiter = RateLimiter.create(0.3);

  protected ArteRateLimitedJsonRestTask(MediathekReader aCrawler, ConcurrentLinkedQueue<D> urlToCrawlDTOs, Optional<String> authKey) {
    super(aCrawler, urlToCrawlDTOs, authKey);
  }

  @Override
  protected void processRestTarget(final D aDTO, final WebTarget aTarget) {
    int retryCount = 0;
    int maxRetries = 3;
    boolean stop = false;

    while (!stop && !Config.getStop()) {
      // Apply rate limiting before each request (including retries)
      if (aTarget.getUri().toString().contains("api.arte.tv/api/opa/")) {
        opaApirateLimiter.acquire();
      } else {
        rateLimiter.acquire();
      }

      Builder request = aTarget.request();
      final Optional<String> authKey = getAuthKey();
      if (authKey.isPresent()) {
        request = request.header(HEADER_AUTHORIZATION, authKey.get());
      }

      try (Response response = createResponse(request, aDTO)) {
        traceRequest(response.getLength());

        if (response.getStatus() == 200) {
          gsonBuilder.registerTypeAdapter(getType(), getParser(aDTO));
          final Gson gson = gsonBuilder.create();
          final String jsonOutput = response.readEntity(String.class);
          final R responseObj = gson.fromJson(jsonOutput, getType());
          postProcessing(responseObj, aDTO);
          stop = true;
          // Check if we got a 429 and have retries left
        } else if (response.getStatus() == 429 && retryCount < maxRetries) {
          String retryAfter = response.getHeaderString("Retry-After");
          Log.sysLog("429: " + aDTO.getUrl() + " - retry after: " + retryAfter);
          retryCount++;
          try {
            TimeUnit.MILLISECONDS.sleep(60000);
          } catch (InterruptedException ignored) {
          }
        } else {
          FilmeSuchen.listeSenderLaufen.inc(crawler.getRunIdentifier(), RunSender.Count.FEHLER);
          FilmeSuchen.listeSenderLaufen.inc(crawler.getRunIdentifier(), RunSender.Count.FEHLVERSUCHE);
          handleHttpError(aDTO, aTarget.getUri(), response);
          stop = true;
        }
      }
    }
  }
}