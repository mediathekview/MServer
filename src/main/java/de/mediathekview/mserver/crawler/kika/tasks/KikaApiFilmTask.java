package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import de.mediathekview.mserver.crawler.ard.ArdUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.core.Response;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.json.KikaApiFilmDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoPageDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class KikaApiFilmTask extends AbstractJsonRestTask<Film, KikaApiVideoInfoDto, KikaApiFilmDto> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiFilmTask.class);

  private transient ArdUrlOptimizer ardUrlOptimizer;
  private transient ZdfVideoUrlOptimizer zdfVideoUrlOptimizer;

  public KikaApiFilmTask(AbstractCrawler crawler, Queue<KikaApiFilmDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
    ardUrlOptimizer = new ArdUrlOptimizer(crawler);
    zdfVideoUrlOptimizer = new ZdfVideoUrlOptimizer(crawler);
  }

  @Override
  protected JsonDeserializer<KikaApiVideoInfoDto> getParser(KikaApiFilmDto aDTO) {
    return new KikaApiVideoInfoPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiVideoInfoDto>>() {}.getType();
  }

  @Override
  protected void handleHttpError(KikaApiFilmDto dto, URI url, Response response) {
	    crawler.printErrorMessage();
	    LOG.fatal(
	        "A HTTP error {} occurred when getting REST information from: \"{}\".",
	        response.getStatus(),
	        url);
  }

  @Override
  protected void postProcessing(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
    //
    if (aResponseObj.getErrorCode().isPresent()) {
      LOG.error("Error {} : {} for target {} ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().orElse(""), aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    if (aResponseObj.getVideoUrls().isEmpty()) {
      LOG.error("No VideoUrls for {}", aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    final Optional<LocalDateTime> airedDate = getAiredDateTime(aDTO);
    if (aDTO.getTitle().isEmpty() || aDTO.getTopic().isEmpty() || airedDate.isEmpty() || aDTO.getDuration().isEmpty()) {
      if (aDTO.getTitle().isEmpty()) {
        LOG.error("Missing title for {}", aDTO.getUrl());
      } else if (aDTO.getTopic().isEmpty()) {
    	  LOG.error("Missing topic for {}", aDTO.getUrl());
      } else if (airedDate.isEmpty()) {
    	  LOG.error("Missing date for {}", aDTO.getUrl());
      } else if (aDTO.getDuration().isEmpty()) {
    	  LOG.error("Missing duration for {}", aDTO.getUrl());
      }
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    Film aFilm = new Film(
        UUID.randomUUID(),
        crawler.getSender(),
        aDTO.getTitle().get(),
        aDTO.getTopic().get(),
        airedDate.get(),
        parseDuration(aDTO, aDTO.getDuration()).get()
        );
    if (aDTO.getDescription().isPresent()) {
      aFilm.setBeschreibung(aDTO.getDescription().get());
    }
    getGeo(aDTO).ifPresent(aFilm::setGeoLocations);
    getWebsite(aDTO).ifPresent(aFilm::setWebsite);
    aFilm.setUrls(getVideoUrls(aResponseObj, aDTO));
    aFilm.addAllSubtitleUrls(getSubtitle(aResponseObj, aDTO));
    //
    
    
    if (!taskResults.add(aFilm)) {
      LOG.debug("Rejected duplicate {}",aFilm);
      crawler.incrementAndGetErrorCount();
    } else {
      crawler.incrementAndGetActualCount();
    }
    crawler.updateProgress();
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, KikaApiFilmDto> createNewOwnInstance(
      Queue<KikaApiFilmDto> aElementsToProcess) {
    return new KikaApiFilmTask(crawler, aElementsToProcess);
  }

  protected Optional<LocalDateTime> getAiredDateTime(KikaApiFilmDto aDTO) {
    Optional<LocalDateTime> airedDate;
    if (aDTO.getDate().isPresent()) {
      airedDate = parseLocalDateTime(aDTO, aDTO.getDate());	
    } else {
      airedDate = parseLocalDateTime(aDTO, aDTO.getAppearDate());
    }
    return airedDate;
  }
  
  protected Set<URL> getSubtitle(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
	Set<URL> urls = new HashSet<>();
	if (aResponseObj.hasSubtitle()) {
      for (String subtitleUrlAsString : aResponseObj.getSubtitle()) {
        try {
          urls.add(new URL(UrlUtils.addProtocolIfMissing(subtitleUrlAsString, UrlUtils.PROTOCOL_HTTPS)));
        } catch (MalformedURLException e) {
          LOG.error("Invalid subtitle url {} for {} error {}", subtitleUrlAsString, aDTO.getUrl(), e);
        }
      }
      if (aResponseObj.getSubtitle().isEmpty()) {
        LOG.error("Missing subtitle for {}", aDTO.getUrl());
      }
    }
	return urls;
  } 
  
  protected Map<Resolution,FilmUrl> getVideoUrls(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
	  Map<Resolution, FilmUrl> urls = new EnumMap<>(Resolution.class);
	  for (Map.Entry<Resolution,String> element : aResponseObj.getVideoUrls().entrySet()) {
      try {
        String url = element.getValue();
        if (Resolution.HD.equals(element.getKey())) {
          url = ardUrlOptimizer.optimizeHdUrl(url);
          url = zdfVideoUrlOptimizer.getOptimizedUrlHd(url);
        }

        final FilmUrl filmUrl = new FilmUrl(url, crawler.determineFileSizeInKB(url));
        urls.put(element.getKey(), filmUrl);
      } catch (MalformedURLException e) {
        LOG.error("Invalid video url {} for {} error {}", element.getValue(), aDTO.getUrl(), e);
      }
    }
    return urls;
  }
  
  protected Optional<URL> getWebsite(KikaApiFilmDto aDTO) {
	Optional<URL> rs = Optional.empty();
	if (aDTO.getWebsite().isPresent()) {
      try {
        rs = Optional.of(new URL(aDTO.getWebsite().get()));
      } catch (MalformedURLException e) {
        LOG.error("Invalid website url {} for {} error {}", aDTO.getWebsite().get(), aDTO.getUrl(), e);
      }
    }
	return rs;
  }
  
  protected Optional<Collection<GeoLocations>> getGeo(KikaApiFilmDto aDTO) {
    Optional<Collection<GeoLocations>> rs = Optional.empty();
    if (aDTO.getGeoProtection().isPresent()) {
      Optional<GeoLocations> geo = parseGeo(aDTO, aDTO.getGeoProtection());
      if (geo.isPresent()) {
        Collection<GeoLocations> collectionOfGeolocations = new ArrayList<>();
        collectionOfGeolocations.add(geo.get());
        rs = Optional.of(collectionOfGeolocations);
      }
    }
    return rs;
  }
  
  protected Optional<LocalDateTime> parseLocalDateTime(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<LocalDateTime> result = Optional.empty();
    if (text.isPresent()) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        result = Optional.of(LocalDateTime.parse(text.get().substring(0, 19), formatter));
      } catch (Exception e) {
        LOG.error("DateTimeFormatter failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }
  //
  protected Optional<Duration> parseDuration(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<Duration> result = Optional.empty();
    if (text.isPresent()) {
      try {
        int min = Integer.parseInt(text.get());
        result = Optional.of(Duration.ofSeconds(min));
      } catch (Exception e) {
        LOG.error("Parse duration failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }
  //
  protected Optional<GeoLocations> parseGeo(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<GeoLocations> result = Optional.empty();
    if (text.isPresent()) {
      if (text.get().equalsIgnoreCase("germany")) {
        return Optional.of(GeoLocations.GEO_DE);
      } else if (text.get().equalsIgnoreCase("worldwide")) {
        return  Optional.empty();
      } else {
        LOG.error("Unknow GeoLocations {} url {}", text.get(), sourceUrl.getUrl());
      }
    }
    return result;
  }


}
