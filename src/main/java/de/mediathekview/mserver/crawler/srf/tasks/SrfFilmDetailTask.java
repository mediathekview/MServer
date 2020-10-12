package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfFilmJsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;

public class SrfFilmDetailTask extends AbstractRestTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SrfFilmDetailTask.class);

  public SrfFilmDetailTask(
      final AbstractCrawler aCrawler, final Queue<CrawlerUrlDTO> aURLsToCrawl) {
    super(aCrawler, aURLsToCrawl, null);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    switch (response.getStatus()) {
      case 200:
      case 203:
        parseFilm(response, aTarget.getUri());
        break;
      case 403:
        // Geo-Blocking für Crawler-Standort
        LOG.error("SrfFilmDetailTask: Not authorized to access url {}", aTarget.getUri());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
        break;
      default:
        LOG.error(
            "SrfFilmDetailTask: Error reading url {}: {}", aTarget.getUri(), response.getStatus());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
    }
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aURLsToCrawl) {
    return new SrfFilmDetailTask(crawler, aURLsToCrawl);
  }

  private void parseFilm(final Response response, final URI uri) {
    final String jsonOutput = response.readEntity(String.class);

    try {
      final Type type = new TypeToken<Optional<Film>>() {}.getType();
      final Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(type, new SrfFilmJsonDeserializer(crawler))
              .create();

      final Optional<Film> film = gson.fromJson(jsonOutput, type);
      if (film.isPresent()) {
        taskResults.add(film.get());

        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      }
    } catch (final JsonSyntaxException e) {
      LOG.error("SrfFilmDetailTask: Error reading url {}", uri, e);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }
}
