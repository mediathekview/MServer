package mServer.crawler.sender.arte.tasks;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.arte.ArteRestVideoTypeMapper;
import mServer.crawler.sender.arte.ArteVideoType;
import mServer.crawler.sender.arte.json.ArteVideoInfoDto;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteDtoVideo2FilmTask extends AbstractRecursivConverterTask<DatenFilm, ArteVideoInfoDto> {
  private static final long serialVersionUID = 1L;
  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
  private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  private final String sender;

  public ArteDtoVideo2FilmTask(MediathekReader aCrawler, ConcurrentLinkedQueue<ArteVideoInfoDto> aUrlToCrawlDTOs, String sender) {
    super(aCrawler, aUrlToCrawlDTOs);
    this.sender = sender;
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, ArteVideoInfoDto> createNewOwnInstance(
          ConcurrentLinkedQueue<ArteVideoInfoDto> aElementsToProcess) {
    return new ArteDtoVideo2FilmTask(crawler, aElementsToProcess, sender);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return 50;
  }

  @Override
  protected void processElement(ArteVideoInfoDto aElement) {
    if (Config.getStop()) {
      return;
    }

    parse(aElement);
  }

  protected void parse(ArteVideoInfoDto aElement) {
    Map<Qualities, String> videoUrls = buildVideoUrls(aElement, ArteVideoType.DEFAULT);
    Map<Qualities, String> videoSubs = buildVideoUrls(aElement, ArteVideoType.AUDIO_DESCRIPTION);
    if (!videoUrls.isEmpty()) {
      addFilm(aElement, "", videoUrls);
    }
    if (!videoSubs.isEmpty()) {
      addFilm(aElement, " (mit Untertitel)", videoSubs);
    }
    //
    Map<Qualities, String> originalVersion = buildVideoUrls(aElement, ArteVideoType.ORIGINAL);
    Map<Qualities, String> originalVersionSubs = buildVideoUrls(aElement, ArteVideoType.ORIGINAL_WITH_SUBTITLE);
    if (!originalVersion.isEmpty()) {
      addFilm(aElement, " (Originalversion)", originalVersion);
    }
    if (!originalVersionSubs.isEmpty()) { // es gibt nur FR und FR mit UT dann nehmen wir FR mit UT
      addFilm(aElement, " (Originalversion mit Untertitel)", originalVersionSubs);
    }
  }

  protected void addFilm(ArteVideoInfoDto videoInfo, String titleSuffix, Map<Qualities, String> video) {
    final LocalDateTime localDateTime = buildAired(videoInfo);
    String date = localDateTime.format(DATE_FORMAT);
    String time = localDateTime.format(TIME_FORMAT);

    DatenFilm film = new DatenFilm(sender, buildTopic(videoInfo), buildWebsite(videoInfo), buildTitle(videoInfo) + titleSuffix,
            video.get(Qualities.NORMAL), "" /*urlRtmp*/,
            date, time, buildDuration(videoInfo).getSeconds(), buildDescription(videoInfo));
    if (video.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, video.get(Qualities.HD));
    }
    if (video.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, video.get(Qualities.SMALL));
    }

    final GeoLocations geoLocations = buildGeoLocation(videoInfo);
    if (geoLocations != GeoLocations.GEO_NONE) {
      film.arr[DatenFilm.FILM_GEO] = geoLocations.getDescription();
    }

    if (!taskResults.add(film)) {
      log.info("Duplicate {}", film);
    }
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

  protected String buildWebsite(ArteVideoInfoDto aElement) {
    if (aElement.getWebsite().isEmpty()) {
      return "";
    }
    return aElement.getWebsite().get();
  }

  protected Map<Qualities, String> buildVideoUrls(ArteVideoInfoDto aElement, ArteVideoType type) {
    final Map<Qualities, String> urls = new EnumMap<>(Qualities.class);
    aElement.getVideoLinks().forEach(entry -> {
      Optional<ArteVideoType> audioTypeCode = ArteRestVideoTypeMapper.map(sender, entry.getAudioCode().get());
      if (audioTypeCode.isPresent() && audioTypeCode.get().equals(type)) {
        urls.put(ArteRestVideoTypeMapper.mapQuality(entry.getQuality().get()).get(), entry.getUrl().get());
      }
    });
    return urls;
  }

}
