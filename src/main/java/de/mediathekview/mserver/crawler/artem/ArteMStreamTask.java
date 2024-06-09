package de.mediathekview.mserver.crawler.artem;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.reflect.TypeToken;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class ArteMStreamTask extends AbstractJsonRestTask<Film, PagedElementListDTO<ArteMStreamDto>, ArteMVideoDto> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ArteMStreamTask.class);
  private int subPageIndex = 0;

  protected ArteMStreamTask(AbstractCrawler crawler, Queue<ArteMVideoDto> urlToCrawlDTOs, String authKey, int subPageIndex) {
    super(crawler, urlToCrawlDTOs, authKey);
    this.subPageIndex = subPageIndex;
  }
  
  @Override
  protected Type getType() {
    return new TypeToken<List<ArteMVideoDto>>() {}.getType();
  }

  @Override
  protected void handleHttpError(ArteMVideoDto dto, URI url, Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
  }

  @Override
  protected void postProcessing(PagedElementListDTO<ArteMStreamDto> aResponseObj, ArteMVideoDto aDTO) {
    final Optional<AbstractRecursiveConverterTask<Film, ArteMVideoDto>> subpageCrawler;
    final Optional<String> nextPageLink = aResponseObj.getNextPage();
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > subPageIndex) {
      final Queue<ArteMVideoDto> nextPageLinks = new ConcurrentLinkedQueue<>();
      ArteMVideoDto np = new ArteMVideoDto(aDTO);
      np.setUrl(nextPageLink.get());
      nextPageLinks.add(np);
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }
    // Trailer
    if (!aDTO.getPlatform().orElse("").equalsIgnoreCase("EXTRAIT")) {
      Set<ArteMStreamDto> streams = aResponseObj.getElements();
      taskResults.add(createFilm(aDTO, streams));
    }
    //
    
    
  }

  @Override
  protected Object getParser(ArteMVideoDto aDTO) {
    return new ArteMSreamDeserializer();
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, ArteMVideoDto> createNewOwnInstance(
      Queue<ArteMVideoDto> aElementsToProcess) {
    return new ArteMStreamTask(crawler, aElementsToProcess, getAuthKey().orElse(""), subPageIndex+1);
  }

  private Film createFilm(ArteMVideoDto filmData, Set<ArteMStreamDto> streams) {
    Film film = new Film(
        UUID.randomUUID(),
        crawler.getSender(),
        filmData.getSubtitle().orElse(""),
        filmData.getTitle().get(),
        parseDate(filmData.getCreationDate().get()).get(),
        parseDuration(filmData.getDurationSeconds().get()).get()
        );
    film.setBeschreibung(filmData.getShortDescription().get());
    film.setWebsite(parseWebsite(filmData.getWebsite().get()).get());
    film.addGeolocation(parseGeo(filmData.getGeoblockingZone().get()));
    streams.stream().findAny().get().getSubtitles();
    return film;
  }
  
  private Set<URL> parseSubtitle(Optional<Map<String, String>> data) {
    return null;
  }
  
  private GeoLocations parseGeo(String data) {
    switch(data) {
      case "ALL":
        return GeoLocations.GEO_NONE;
    }
    return GeoLocations.GEO_NONE;
  }
  
  private Optional<LocalDateTime> parseDate(String date) {
    try {
      return Optional.of(LocalDateTime.parse(date));
    } catch (Exception e) {

    }
    return Optional.empty(); 
  }
  
  private Optional<Duration> parseDuration(String data) {
    try {
      return Optional.of(Duration.ofSeconds(Long.parseLong(data)));
    } catch (Exception e) {

    }
    return Optional.empty(); 
  }
  
  private Optional<URL> parseWebsite(String data) {
    try {
      return Optional.of(new URL(data));
    } catch (Exception e) {

    }
    return Optional.empty(); 
  }
    
}
