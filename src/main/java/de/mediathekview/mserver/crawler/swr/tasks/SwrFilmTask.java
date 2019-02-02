package de.mediathekview.mserver.crawler.swr.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.swr.parser.SwrFilmDeserializer;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SwrFilmTask extends AbstractRestTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SwrFilmTask.class);
  private static final int REQUEST_DELAY = 4000;

  private final String baseUrl;

  public SwrFilmTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDtos, Optional.empty());

    baseUrl = aBaseUrl;
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    delayRequest();

    Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    switch (response.getStatus()) {
      case 200:
      case 203:
        parseFilm(response, aTarget.getUri());
        break;
      case 403:
        // Geo-Blocking für Crawler-Standort
        LOG.error("SwrFilmTask: Not authorized to access url " + aTarget.getUri().toString());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
        break;
      default:
        LOG.error("SwrFilmTask: Error reading url " + aTarget.getUri().toString() + ": " + response.getStatus());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
    }
  }

  private void delayRequest() {
    try {
      TimeUnit.MILLISECONDS.sleep(REQUEST_DELAY);
    } catch (InterruptedException e) {
      LOG.error(e);
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new SwrFilmTask(crawler, aElementsToProcess, baseUrl);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    // only one instance of SwrFilmTask is allowed because SWR blocks requests
    return Integer.MAX_VALUE;
  }

  private void parseFilm(Response response, URI uri) {
    final String jsonOutput = response.readEntity(String.class);

    try {
      Type type = new TypeToken<Optional<Film>>() {
      }.getType();
      final Gson gson = new GsonBuilder().registerTypeAdapter(type, new SwrFilmDeserializer()).create();

      Optional<Film> film = gson.fromJson(jsonOutput, type);
      if (film.isPresent()) {
        taskResults.add(film.get());

        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      } else {
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (JsonSyntaxException e) {
      LOG.error("SwrFilmDetailTask: Error reading url " + uri.toString(), e);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }
}
