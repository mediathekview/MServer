package de.mediathekview.mserver.crawler.arte.tasks;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import de.mediathekview.mserver.crawler.arte.ArteVideoType;
import de.mediathekview.mserver.crawler.arte.ArteRestVideoTypeMapper;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoLinkDto;
import de.mediathekview.mserver.base.utils.LanguageCodeUtils;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Resolution;


public class ArteDtoVideo2FilmTask extends AbstractRecursiveConverterTask<Film, ArteVideoInfoDto> {
  @Serial private static final long serialVersionUID = 1L;
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
      Film originalVersionFilm = buildFilmBody(aElement);
      originalVersionFilm.setAudioLanguage(
          resolveAudioLanguage(aElement, ArteVideoType.ORIGINAL).orElse(null));
      addFilm(originalVersionFilm, film.getTitel()+ " (Originalversion)", originalVersion, originalVersionSubs);
    } else if (originalVersionSubs.size() > 0) { // es gibt nur FR und FR mit UT dann nehmen wir FR mit UT
      Film film = buildFilmBody(aElement);
      Film originalVersionFilm = buildFilmBody(aElement);
      originalVersionFilm.setAudioLanguage(
          resolveAudioLanguage(aElement, ArteVideoType.ORIGINAL_WITH_SUBTITLE).orElse(null));
      addFilm(originalVersionFilm, film.getTitel()+ " (Originalversion mit Untertitel)", originalVersionSubs, null);
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
    film.setId(aElement.getId());
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
    } catch (MalformedURLException _) {
      log.error("Invalid url: {}", aElement.getWebsite().get());
    }
    return null;
  }
  
  protected Map<Resolution, FilmUrl> buildVideoUrls(ArteVideoInfoDto aElement, ArteVideoType type) {
    Map<Resolution, FilmUrl> urls  = new EnumMap<>(Resolution.class);
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
  

  /**
   * Liest die Sprache der Tonspur fuer den ersten Videolink, der zum uebergebenen Typ gehoert.
   *
   * <p>arte liefert sie im Listing unter {@code videos[].versions[].audioLanguage}, je
   * Versionscode. Ausgewertet wurde bisher nur der Versionscode selbst, um den Videotyp zu
   * bestimmen; die Sprache blieb ungenutzt und war im Filmliste-Eintrag nur noch als Titelzusatz
   * "(Originalversion)" sichtbar, der sie nicht benennt.
   *
   * <p>Bei einer reinen Originalfassung (Code "VO") hilft dieser Block allein nicht weiter: arte
   * zeichnet sie dort mit {@code und} aus, eine mehrsprachige Fassung ("VOEU") mit {@code mul}.
   * Fuer diesen Fall wird auf {@code videos[].originalLanguage} zurueckgegriffen - die Sprache des
   * Originals ist bei einer Originalfassung genau die der Tonspur. Fuer die synchronisierte Fassung
   * waere derselbe Wert falsch, weshalb diese Methode nur fuer die Originalversionen aufgerufen
   * wird.
   *
   * <p>Bleibt auch das ohne Ergebnis, bleibt das Feld leer. Zu raten waere schlechter, als nichts
   * zu sagen.
   *
   * @param aElement die Videoinformationen des Beitrags.
   * @param type der gesuchte Videotyp.
   * @return die Sprache als ISO-639-2/T-Code, oder leer.
   */
  protected Optional<String> resolveAudioLanguage(ArteVideoInfoDto aElement, ArteVideoType type) {
    for (ArteVideoLinkDto entry : aElement.getVideoLinks()) {
      if (entry.getAudioCode().isEmpty()) {
        continue;
      }
      final String audioCode = entry.getAudioCode().get();
      final Optional<ArteVideoType> audioTypeCode =
          ArteRestVideoTypeMapper.map(crawler.getSender(), audioCode);
      if (audioTypeCode.isEmpty() || !audioTypeCode.get().equals(type)) {
        continue;
      }
      final Optional<String> language =
          LanguageCodeUtils.normalize(aElement.getAudioLanguagesByCode().get(audioCode));
      if (language.isPresent()) {
        return language;
      }
    }
    return LanguageCodeUtils.normalize(aElement.getOriginalLanguage().orElse(null));
  }

  protected Map<Resolution, String> builRawVideoUrls(ArteVideoInfoDto aElement, ArteVideoType type) {
    final Map<Resolution, String> urls = new EnumMap<>(Resolution.class);
    aElement.getVideoLinks().forEach( entry -> {
      Optional<ArteVideoType> audioTypeCode = ArteRestVideoTypeMapper.map(crawler.getSender(), entry.getAudioCode().get());
      if (audioTypeCode.isPresent() && audioTypeCode.get().equals(type)) {
        urls.put(ArteRestVideoTypeMapper.mapQuality(entry.getQuality().get()).get(), entry.getUrl().get());
      }
    });
    return urls;
  }

}
