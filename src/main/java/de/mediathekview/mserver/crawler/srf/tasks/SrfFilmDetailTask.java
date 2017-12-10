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
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfFilmDetailTask extends AbstractRestTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SrfFilmDetailTask.class);
  
  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  
  public SrfFilmDetailTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl)
  {
    super(aCrawler, aURLsToCrawl, Optional.empty());
  }
  
  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Type type = new TypeToken<Optional<Film>>() {}.getType();
    final Gson gson = new GsonBuilder().registerTypeAdapter(type, new SrfFilmJsonDeserializer()).create();
    Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();
    
    if (response.getStatus() == 200) {

      final String jsonOutput = response.readEntity(String.class);

      try {
        Optional<Film> film = gson.fromJson(jsonOutput, type);
        if (film.isPresent()) {
          taskResults.add(film.get());
        }
      } catch (JsonSyntaxException e) {
        LOG.error("Error reading url " + aTarget.getUri().toString(), e);
      }
      
      // TODO next page...
    } else {
      LOG.error("Error reading url " + aTarget.getUri().toString() + ": " + response.getStatus());
    }
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new SrfFilmDetailTask(crawler, aURLsToCrawl);
  }
  
}
