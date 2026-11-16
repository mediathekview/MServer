package mServer.crawler.sender.arte.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import jakarta.ws.rs.core.Response;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.arte.ArteRestVideoTypeMapper;
import mServer.crawler.sender.arte.ArteVideoType;
import mServer.crawler.sender.arte.json.ArteVideoInfoDto;
import mServer.crawler.sender.arte.json.ArteVideoLinkDto;
import mServer.crawler.sender.arte.json.ArteVideoPlayerDeserializer;
import mServer.crawler.sender.base.*;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteDtoVideo2FilmTask extends ArteRateLimitedJsonRestTask<DatenFilm, List<ArteVideoLinkDto>, ArteVideoInfoDto> {
  @Serial private static final long serialVersionUID = 1L;
  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final String EXTENSION_M3U8 = ".m3u8";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
  private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
  private static final Request.Builder REQUEST_BUILDER = new Request.Builder();

  protected final transient Logger log = LogManager.getLogger(this.getClass());
  private final String sender;

  public ArteDtoVideo2FilmTask(MediathekReader aCrawler, ConcurrentLinkedQueue<ArteVideoInfoDto> aUrlToCrawlDTOs, String sender) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.empty());
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

  protected void parse(ArteVideoInfoDto aElement) {
    if (aElement.getUrl().isEmpty()) {
      log.warn("No video url found for: {}", aElement);
      return;
    }
    Map<Qualities, String> videoUrls = buildVideoUrls(aElement, ArteVideoType.DEFAULT);
    Map<Qualities, String> videoAD = buildVideoUrls(aElement, ArteVideoType.AUDIO_DESCRIPTION);
    if (!videoUrls.isEmpty()) {
      addFilm(aElement, "", videoUrls);
    }
    if (!videoAD.isEmpty()) {
      addFilm(aElement, " (Audiodeskription)", videoAD);
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

    // ARTE provides subs as a new video
    Map<Qualities, String> videoSub = buildVideoUrls(aElement, ArteVideoType.SUBTITLE_INCLUDED);
    if (!videoSub.isEmpty()) {
      addFilm(aElement, " (mit Untertitel)", videoSub);
    }
  }

  protected void addFilm(ArteVideoInfoDto videoInfo, String titleSuffix, Map<Qualities, String> video) {
    final LocalDateTime localDateTime = buildAired(videoInfo);
    String date = localDateTime.format(DATE_FORMAT);
    String time = localDateTime.format(TIME_FORMAT);

    String url = video.containsKey(Qualities.NORMAL) ? video.get(Qualities.NORMAL) : video.get(Qualities.HD);
    try {
      DatenFilm film = new DatenFilm(sender, buildTopic(videoInfo), buildWebsite(videoInfo), buildTitle(videoInfo) + titleSuffix,
              url, "" /*urlRtmp*/,
              date, time, buildDuration(videoInfo).getSeconds(), buildDescription(videoInfo));
      addSubtitles(film, video);
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
    } catch(Exception e) {
      FilmeSuchen.listeSenderLaufen.inc(crawler.getRunIdentifierBase() + "-" + sender, RunSender.Count.FEHLER);
      Log.errorLog(637846873, e, "exception creating film " + videoInfo.getTitle() + " (" + titleSuffix + ")");
    }
  }

  private void addSubtitles(DatenFilm film, Map<Qualities, String> videoUrls) {
    Set<String> loadedPlaylists = new HashSet<>();
    M3U8Parser parser = new M3U8Parser();
    for (String videoUrl : videoUrls.values()) {
      try {
        URI playlistUri = URI.create(videoUrl);
        if (playlistUri.getPath() == null
                || !playlistUri.getPath().endsWith(EXTENSION_M3U8)
                || !loadedPlaylists.add(playlistUri.toString())) {
          continue;
        }

        String content = readContent(playlistUri.toURL()).orElse("");
        for (M3U8Dto entry :
                parser.parse(content).stream()
                        .filter(dto -> dto.getType() == M3U8Dto.M3U8Type.MEDIA_SUBTITLE)
                        .toList()) {
          URI subtitleUri = playlistUri.resolve(entry.getUrl());
          String subtitlePath = subtitleUri.getPath();
          if (subtitlePath != null && subtitlePath.endsWith(EXTENSION_M3U8)) {
            URI vttUri =
                    new URI(
                            subtitleUri.getScheme(),
                            subtitleUri.getUserInfo(),
                            subtitleUri.getHost(),
                            subtitleUri.getPort(),
                            subtitlePath.substring(0, subtitlePath.length() - EXTENSION_M3U8.length()) + ".vtt",
                            subtitleUri.getQuery(),
                            subtitleUri.getFragment());
            film.arr[DatenFilm.FILM_URL_SUBTITLE] = vttUri.toString();
          }
        }
      } catch (IOException | URISyntaxException | IllegalArgumentException e) {
        log.error("Error determining subtitles from M3U8 {}", videoUrl, e);
      }
    }
  }

  protected String buildTitle(ArteVideoInfoDto aElement) {
    String title = aElement.getTitle().orElse("");
    if (aElement.getSubtitle().isPresent()) {
      title += " - " + aElement.getSubtitle().get();
    }
    return title;
  }

  protected String buildTopic(ArteVideoInfoDto aElement) {
    String topic = aElement.getCategoryName().orElse("");
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
    return Duration.ofSeconds(Integer.parseInt(aElement.getDurationSeconds().orElse("0")));
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
      Optional<ArteVideoType> audioTypeCode = ArteRestVideoTypeMapper.map(sender, entry.getAudioCode().orElse(""));
      if (audioTypeCode.isPresent() && audioTypeCode.get().equals(type)) {
        Optional<Qualities> quality = ArteRestVideoTypeMapper.mapQuality(entry.getQuality().get());
        quality.ifPresent(qualities -> urls.put(qualities, entry.getUrl().get()));
      }
    });
    return urls;
  }
  @Override
  protected JsonDeserializer<List<ArteVideoLinkDto>> getParser(ArteVideoInfoDto aDTO) {
    return new ArteVideoPlayerDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<List<ArteVideoLinkDto>>() {}.getType();
  }

  @Override
  protected void handleHttpError(ArteVideoInfoDto dto, URI url, Response response) {
    Log.errorLog(637846873, "A HTTP error " + response.getStatus() + " occurred when getting REST Video information from: \"" + url +"\".");
    log.fatal("A HTTP error {} occurred when getting REST Video information from: \"{}\".", response.getStatus(), url);
  }

  @Override
  protected void postProcessing(List<ArteVideoLinkDto> aResponseObj, ArteVideoInfoDto aDTO) {
    if (Config.getStop()) {
      return;
    }

    aDTO.setVideoLinks(
            aResponseObj.stream()
                    .filter(code ->
                            ArteRestVideoTypeMapper.map(aDTO.getSender(), code.getAudioCode().orElse(""))
                                    .isPresent())
                    .toList());

    parse(aDTO);
  }

  private Optional<String> readContent(final URL aUrl) {
    Request request = REQUEST_BUILDER.url(aUrl).build();
    try (okhttp3.Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute(); ResponseBody body = response.body()) {
      if (response.isSuccessful() && body != null) {
        return Optional.of(body.string());
      } else {
        log.error("ArdVideoInfoJsonDeserializer: Request {} failed: {}", aUrl, response.code());
      }
    } catch (IOException ex) {
      log.error("ArdVideoInfoJsonDeserializer: ", ex);
    }

    return Optional.empty();
  }
}
