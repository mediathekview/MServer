package mServer.crawler.sender.dw.parser;

import com.google.gson.*;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.dw.DwVideoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DwFilmDetailDeserializer implements JsonDeserializer<Optional<DatenFilm>> {
  private static final Logger LOG = LogManager.getLogger(DwFilmDetailDeserializer.class);

  private static final String ELEMENT_ID = "id";
  private static final String ELEMENT_TYPE = "type";
  private static final String ELEMENT_NAME = "name";
  private static final String ELEMENT_TEASER = "teaser";
  private static final String ELEMENT_CATEGORY = "categoryName";
  private static final String ELEMENT_LINK = "permaLink";
  private static final String ELEMENT_DATETIME = "displayDate";

  private static final String ELEMENT_MAINCONTENT = "mainContent";
  private static final String ELEMENT_MAINCONTENT_LINK = "url";
  private static final String ELEMENT_MAINCONTENT_DURATION = "duration";
  private static final String ELEMENT_MAINCONTENT_SOURCES = "sources";
  private static final String ELEMENT_MAINCONTENT_SOURCES_URL = "url";
  private static final String ELEMENT_MAINCONTENT_SOURCES_FORMAT = "format";
  private static final String ELEMENT_MAINCONTENT_SOURCES_BITRATE = "bitrate";
  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final Pattern PATTERN_VIDEO_WITH_RESOLUTION =
      Pattern.compile(".+_(\\d+)x(\\d+)\\.mp4");
  private static final Comparator<? super DwVideoDto> DW_VIDEO_COMPARATOR =
      Comparator.comparing(DwVideoDto::getWidth).thenComparing(DwVideoDto::getBitRate);

  private final String sender;
  private final MediathekReader crawler;

  public DwFilmDetailDeserializer(final MediathekReader aCrawler) {
    this.sender = aCrawler.getSendername();
    crawler = aCrawler;
  }

  protected boolean isValidVideo(
      final JsonObject jsonObject,
      Optional<String> videoId,
      Optional<String> type,
      Optional<String> title,
      Optional<String> topic) {
    if (!videoId.isPresent()) {
      LOG.error("Could not find mandatory element videoId");
      return false;
    } else if (!title.isPresent()) {
      LOG.error("Could not find mandatory element title for videoId {} ", videoId.get());
      return false;
    } else if (!topic.isPresent()) {
      LOG.error("Could not find mandatory element topic for videoId {} ", videoId.get());
      return false;
    } else if (!type.isPresent()) {
      LOG.error("Could not find mandatory element type for videoId {} ", videoId.get());
      return false;
    } else if (!type.get().equalsIgnoreCase("video")) {
      return false;
    } else if (!jsonObject.has(ELEMENT_MAINCONTENT)) {
      LOG.error("Could not find maincontent for videoId {}", videoId.get());
      return false;
    } else if (!jsonObject
        .get(ELEMENT_MAINCONTENT)
        .getAsJsonObject()
        .has(ELEMENT_MAINCONTENT_SOURCES)) {
      LOG.error("Could not find sources for videoId {}", videoId.get());
      return false;
    } else if (!JsonUtils.getAttributeAsString(
            jsonObject.get(ELEMENT_MAINCONTENT).getAsJsonObject(), ELEMENT_MAINCONTENT_LINK)
        .isPresent()) {
      LOG.error("Could not find thisPageUrl for videoId {}", videoId.get());
      return false;
    }
    return true;
  }

  @Override
  public Optional<DatenFilm> deserialize(
      JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    final JsonObject jsonObject = aJsonElement.getAsJsonObject();

    final Optional<String> videoId = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_ID);
    final Optional<String> type = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_TYPE);
    final Optional<String> title = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_NAME);
    final Optional<String> topic = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_CATEGORY);

    if (!isValidVideo(jsonObject, videoId, type, title, topic)) {
      return Optional.empty();
    }

    final JsonObject jsonObjectMainContent = jsonObject.get(ELEMENT_MAINCONTENT).getAsJsonObject();
    final Optional<String> thisPageUrl =
        JsonUtils.getAttributeAsString(jsonObjectMainContent, ELEMENT_MAINCONTENT_LINK);

    final Optional<String> description = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_TEASER);
    final Optional<String> website = getWebsite(thisPageUrl.get(), jsonObject);

    final LocalDateTime airedDate = getAiredDate(thisPageUrl.get(), jsonObject);
    String dateValue = airedDate.format(DATE_FORMAT);
    String timeValue = airedDate.format(TIME_FORMAT);

    final Duration duration = getDuration(thisPageUrl.get(), jsonObjectMainContent);

    final JsonArray jsonObjectMainContentSources =
            jsonObjectMainContent.get(ELEMENT_MAINCONTENT_SOURCES).getAsJsonArray();
    final Map<Qualities, String> videos = getVideos(title.get(), jsonObjectMainContentSources);

    final DatenFilm film = new DatenFilm(Const.DW, topic.get(), website.orElse(""), title.get(), videos.get(Qualities.NORMAL), "",
            dateValue, timeValue, duration.getSeconds(), description.orElse(""));
    if (videos.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videos.get(Qualities.SMALL));
    }
    if (videos.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videos.get(Qualities.HD));
    }

    return Optional.of(film);
  }

  private Optional<String> getWebsite(final String videoid, final JsonObject jsonObject) {
    Optional<String> websiteUrl = Optional.empty();
    final Optional<String> websiteString = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_LINK);
    if (websiteString.isPresent()) {
      try {
        websiteUrl = Optional.of(websiteString.get());
      } catch (Exception e) {
        LOG.error("Error getWebsite for video {} on value '{}'", videoid, websiteString.get());
      }
    } else {
      LOG.error("no error getWebsite found for video {}", videoid);
    }
    return websiteUrl;
  }

  private LocalDateTime getAiredDate(final String videoid, final JsonObject jsonObject) {
    final DateTimeFormatter dateFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    LocalDateTime airedDatetime = LocalDateTime.now();
    final Optional<String> displayDate =
        JsonUtils.getAttributeAsString(jsonObject, ELEMENT_DATETIME);
    if (displayDate.isPresent()) {
      try {
        airedDatetime = LocalDateTime.parse(displayDate.get(), dateFormatter);
      } catch (Exception e) {
        LOG.error("error parsing getAiredDate value '{}' for video {}", displayDate.get(), videoid);
      }
    } else {
      LOG.error("no airedDate found for video {}", videoid);
    }
    return airedDatetime;
  }

  private Duration getDuration(final String videoid, final JsonObject jsonObject) {
    Duration duration = Duration.ofSeconds(0);
    final Optional<String> durationString =
        JsonUtils.getAttributeAsString(jsonObject, ELEMENT_MAINCONTENT_DURATION);
    if (durationString.isPresent()) {
      try {
        Optional<String> durationInSeconds =
            JsonUtils.getAttributeAsString(jsonObject, ELEMENT_MAINCONTENT_DURATION);
        if (durationInSeconds.isPresent()) {
          duration = Duration.ofSeconds(Integer.parseInt(durationInSeconds.get()));
        }
      } catch (Exception e) {
        LOG.error("error getDuration for video {} on value '{}'", videoid, durationString.get());
      }
    } else {
      LOG.error("no error duration found for video {}", videoid);
    }
    return duration;
  }

  private Map<Qualities, String> getVideos(
      final String videoid, final JsonArray videos) {
    final Map<Qualities, String> videoListe = new ConcurrentHashMap<>();

    if (videos == null) {
      return videoListe;
    }

    final ArrayList<DwVideoDto> videoListeRaw = new ArrayList<>();

    videos.forEach(
        (JsonElement currentElement) -> {
          if (currentElement.isJsonObject()) {
            final JsonObject currentElementObject = currentElement.getAsJsonObject();
            final Optional<String> bitrateValue =
                JsonUtils.getAttributeAsString(
                    currentElementObject, ELEMENT_MAINCONTENT_SOURCES_BITRATE);
            final Optional<String> format =
                JsonUtils.getAttributeAsString(
                    currentElementObject, ELEMENT_MAINCONTENT_SOURCES_FORMAT);
            final Optional<String> url =
                JsonUtils.getAttributeAsString(
                    currentElementObject, ELEMENT_MAINCONTENT_SOURCES_URL);

            if (url.isPresent() && format.isPresent()) {
              if (!format.get().equalsIgnoreCase("hls")) {
                final int bitrate = tryParseBitRate(bitrateValue);
                final Optional<Integer> width = tryToDetermineWidthFromUrl(url.get());
                videoListeRaw.add(new DwVideoDto(url.get(), width.orElse(0), bitrate));
              }
            } else {
              LOG.error("Mising video url element for video: {}", videoid);
            }
          }
        });
    //
    videoListeRaw.sort(DW_VIDEO_COMPARATOR);
    videoListeRaw.forEach(
        video -> {
          final Qualities qualities = determineQuality(video.getUrl(), video.getWidth());
          videoListe.put(qualities, video.getUrl());
        });

    return videoListe;
  }

  private int tryParseBitRate(Optional<String> bitRateValue) {
    if (bitRateValue.isPresent()) {
      try {
        return Integer.parseInt(bitRateValue.get());
      } catch (NumberFormatException e) {
        LOG.debug(e);
      }
    }
    return 0;
  }

  private Qualities determineQuality(String url, int width) {
    if (width > 0) {
      return getQualityFromWidth(width);
    }
    if (url.endsWith("avc.mp4") || url.endsWith("hd.mp4")) {
      return Qualities.HD;
    }
    if (url.endsWith("sor.mp4")) {
      return Qualities.SMALL;
    }
    if (url.endsWith("sd.mp4")) {
      return Qualities.NORMAL;
    }
    LOG.debug("unknown url format: {}", url);
    return Qualities.SMALL;
  }

  private static Qualities getQualityFromWidth(final int width) {
    if (width >= 1280) {
      return Qualities.HD;
    }
    if (width >= 640) {
      return Qualities.NORMAL;
    }
    return Qualities.SMALL;
  }

  private Optional<Integer> tryToDetermineWidthFromUrl(String url) {

    final Matcher matcher = PATTERN_VIDEO_WITH_RESOLUTION.matcher(url);
    if (matcher.matches()) {
      try {
        final int width = Integer.parseInt(matcher.group(1));
        return Optional.of(width);
      } catch (NumberFormatException e) {
        LOG.error(e);
      }
    }
    return Optional.empty();
  }
}
