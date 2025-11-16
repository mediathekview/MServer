package de.mediathekview.mserver.crawler.arte.tasks;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.arte.ArteVideoType;
import de.mediathekview.mserver.crawler.arte.ArteRestVideoTypeMapper;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;


public class ArteDtoVideo2FilmTask extends AbstractRecursiveConverterTask<Film, ArteVideoInfoDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
  private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
  
  public ArteDtoVideo2FilmTask(AbstractCrawler aCrawler, Queue<ArteVideoInfoDto> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, ArteVideoInfoDto> createNewOwnInstance(
      Queue<ArteVideoInfoDto> aElementsToProcess) {
    return new ArteDtoVideo2FilmTask(crawler, aElementsToProcess);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return config.getMaximumUrlsPerTask();
  }

  @Override
  protected void processElement(ArteVideoInfoDto aElement) {
    parse(aElement);
  }
  
  ///////////////////////////////////////////////////////////////////////////
  
  protected void parse(ArteVideoInfoDto aElement) {
    Map<Resolution, FilmUrl> videoUrls = buildVideoUrls(aElement, ArteVideoType.DEFAULT);
    Map<Resolution, FilmUrl> videoAD = buildVideoUrls(aElement, ArteVideoType.AUDIO_DESCRIPTION);
    if (videoUrls.size() > 0) {
      Film film = buildFilmBody(aElement);
      addFilm(buildFilmBody(aElement), film.getTitel(), videoUrls, videoAD);
    }
    //
    Map<Resolution, FilmUrl> originalVersion = buildVideoUrls(aElement, ArteVideoType.ORIGINAL);
    Map<Resolution, FilmUrl> originalVersionSubs = buildVideoUrls(aElement, ArteVideoType.ORIGINAL_WITH_SUBTITLE);
    if (originalVersion.size() > 0) {
      Film film = buildFilmBody(aElement);
      addFilm(buildFilmBody(aElement), film.getTitel()+ " (Originalversion)", originalVersion, originalVersionSubs);
    } else if (originalVersionSubs.size() > 0) { // es gibt nur FR und FR mit UT dann nehmen wir FR mit UT
      Film film = buildFilmBody(aElement);
      addFilm(buildFilmBody(aElement), film.getTitel()+ " (Originalversion mit Untertitel)", originalVersionSubs, null);
    }
    // ARTE provides subs as a new video
    Map<Resolution, FilmUrl> videoSub = buildVideoUrls(aElement, ArteVideoType.SUBTITLE_INCLUDED);
    if (videoSub.size() > 0) {
      Film film = buildFilmBody(aElement);
      addFilm(buildFilmBody(aElement), film.getTitel()+ " (mit Untertitel)", videoSub, null);
    }
  }
  
  protected void addFilm(Film film, String title, Map<Resolution, FilmUrl> video, Map<Resolution, FilmUrl> audioDesc) {
    film.setTitel(title);
    film.setUrls(video);
    if (audioDesc != null) {
      film.setAudioDescriptions(audioDesc);
    }
    crawler.incrementAndGetActualCount();
    if (!taskResults.add(film)) {
      log.info("Duplicate {}", film);
    }
    
  }
  
  protected Film buildFilmBody(ArteVideoInfoDto aElement) {
    Film film = new Film(
        UUID.randomUUID(),
        aElement.getSender(),
        buildTitle(aElement),
        buildTopic(aElement),
        buildAired(aElement),
        buildDuration(aElement)
        );
    film.addGeolocation(buildGeoLocation(aElement));
    film.setBeschreibung(buildDescription(aElement));
    film.setWebsite(buildWebsite(aElement));
    return film;
  }
  
  protected String buildTitle(ArteVideoInfoDto aElement) {
    String title = aElement.getTitle().get();
    if (aElement.getSubtitle().isPresent()) {
      title += " - " + aElement.getSubtitle().get();
    }
    return title;
  }
  
  protected String buildTopic(ArteVideoInfoDto aElement) {
    String topic = aElement.getCategoryName().get();
    if (aElement.getSubcategoryName().isPresent()) {
      topic += " - " + aElement.getSubcategoryName().get();
    }
    return topic;
  }
  
  protected LocalDateTime buildAired(ArteVideoInfoDto aElement) {
    String value = aElement.
        getBroadcastBeginRounded().orElse(
            aElement.getBroadcastBegin().orElse(
                aElement.getFirstBroadcastDate().orElse(
                    aElement.getCreationDate().orElse(""))));
    LocalDateTime local = LocalDateTime.parse(value, DATE_FORMATTER);
    ZonedDateTime zoned = local.atZone(ZONE_ID);
    int hoursToAdd = zoned.getOffset().getTotalSeconds() / 3600;
    return local.plusHours(hoursToAdd);
  }
  
  protected Duration buildDuration(ArteVideoInfoDto aElement) {
    return Duration.ofSeconds(Integer.parseInt(aElement.getDurationSeconds().get()));
  }
  
  protected GeoLocations buildGeoLocation(ArteVideoInfoDto aElement) {
    GeoLocations geo = GeoLocations.GEO_NONE;
    if (aElement.getGeoblockingZone().isPresent()) {
      String code = aElement.getGeoblockingZone().get();
      switch (code) {
        case "DE_FR":
          geo = GeoLocations.GEO_DE_FR;
          break;
        case "EUR_DE_FR":
          geo = GeoLocations.GEO_DE_AT_CH_FR;
          break;
        case "SAT":
          geo = GeoLocations.GEO_DE_AT_CH_EU;
          break;
        case "ALL":
          geo = GeoLocations.GEO_NONE;
          break;
        default:
          log.debug("New ARTE GeoLocation: {}", code);
      }
    }
    return geo;
  }
  
  protected String buildDescription(ArteVideoInfoDto aElement) {
    return aElement.getShortDescription().orElse("");
  }
  
  protected URL buildWebsite(ArteVideoInfoDto aElement) {
    if (aElement.getWebsite().isEmpty()) {
      return null;
    }
    try {
      return URI.create(aElement.getWebsite().get()).toURL();
    } catch (MalformedURLException e) {
      log.error("Invalid url: {}", aElement.getWebsite().get());
    }
    return null;
  }
  
  protected Map<Resolution, FilmUrl> buildVideoUrls(ArteVideoInfoDto aElement, ArteVideoType type) {
    Map<Resolution, FilmUrl> urls  = new HashMap<>();
    Map<Resolution, String> rawUrls = builRawVideoUrls(aElement, type);
    rawUrls.forEach( (resolution, rawUrl) -> {
      try {
        urls.put(resolution, new FilmUrl(rawUrl, crawler.determineFileSizeInKB(rawUrl)));
      } catch (Exception e) {
        log.error("Error building FilmUrl {}", rawUrl, e);
      }
    });
    return urls;
  }
  
  protected Map<Resolution, String> builRawVideoUrls(ArteVideoInfoDto aElement, ArteVideoType type) {
    final Map<Resolution, String> urls = new HashMap<>();
    aElement.getVideoLinks().forEach( entry -> {
      Optional<ArteVideoType> audioTypeCode = ArteRestVideoTypeMapper.map(crawler.getSender(), entry.getAudioCode().get());
      if (audioTypeCode.isPresent() && audioTypeCode.get().equals(type)) {
        urls.put(ArteRestVideoTypeMapper.mapQuality(entry.getQuality().get()).get(), entry.getUrl().get());
      }
    });
    return urls;
  }

}
