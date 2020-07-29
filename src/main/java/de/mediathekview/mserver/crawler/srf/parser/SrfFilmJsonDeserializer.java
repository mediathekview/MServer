package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.FileSizeDeterminer;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlParseException;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.M3U8Constants;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;

public class SrfFilmJsonDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final org.apache.logging.log4j.Logger LOG =
      LogManager.getLogger(SrfFilmJsonDeserializer.class);

  private static final String ATTRIBUTE_DESCRIPTION = "description";
  private static final String ATTRIBUTE_DURATION = "duration";
  private static final String ATTRIBUTE_FORMAT = "format";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_LEAD = "lead";
  private static final String ATTRIBUTE_MIMETYPE = "mimeType";
  private static final String ATTRIBUTE_PUBLISH_DATE = "publishDate";
  private static final String ATTRIBUTE_QUALITY = "quality";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_URL = "url";

  private static final String ELEMENT_CHAPTER_LIST = "chapterList";
  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_EPISODE = "episode";
  private static final String ELEMENT_RESOURCE_LIST = "resourceList";
  private static final String ELEMENT_SHOW = "show";
  private static final String ELEMENT_SUBTITLE_LIST = "subtitleList";

  private static final String SUBTITLE_FORMAT = "TTML";
  private static final String ATTRIBUTE_URL_SD = "downloadSdUrl";
  private static final String ATTRIBUTE_URL_HD = "downloadHdUrl";

  private final AbstractCrawler crawler;

  public SrfFilmJsonDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  private static void addSubtitle(final String aSubtitleUrl, final Film aFilm) {
    if (!aSubtitleUrl.isEmpty()) {
      try {
        aFilm.addSubtitle(new URL(aSubtitleUrl));
      } catch (final MalformedURLException ex) {
        LOG.error(String.format("A subtitle URL \"%s\" isn't valid.", aSubtitleUrl), ex);
      }
    }
  }

  private static boolean addUrls(final JsonObject data, final Film aFilm) {
    Optional<FilmUrl> sdUrl = buildFilmUrl(JsonUtils.getAttributeAsString(data, ATTRIBUTE_URL_SD));
    Optional<FilmUrl> hdUrl = buildFilmUrl(JsonUtils.getAttributeAsString(data, ATTRIBUTE_URL_HD));

    if (sdUrl.isPresent() || hdUrl.isPresent()) {
      if (sdUrl.isPresent()) {
        aFilm.addUrl(Resolution.NORMAL, sdUrl.get());
      }
      if (hdUrl.isPresent()) {
        aFilm.addUrl(Resolution.HD, hdUrl.get());
      }
      return true;
    } else {
      return false;
    }
  }

  private static Optional<FilmUrl> buildFilmUrl(final Optional<String> url) {

    if (!url.isPresent()) {
      return Optional.empty();
    }

    try {
      return Optional.of(new FilmUrl(url.get(), new FileSizeDeterminer(url.get()).getFileSizeInMiB()));
    } catch (final MalformedURLException ex) {
      LOG.error(String.format("A found download URL \"%s\" isn't valid.", url), ex);
    }

    return Optional.empty();
  }

  private static Optional<URL> buildWebsiteUrl(
      final String aId, final String aTitle, final String aTheme) {

    final String url =
        String.format(
            SrfConstants.WEBSITE_URL, replaceCharForUrl(aTheme), replaceCharForUrl(aTitle), aId);

    try {
      return Optional.of(new URL(url));
    } catch (final MalformedURLException ex) {
      LOG.error(String.format("The website url \"%s\" isn't valid.", url), ex);
    }

    return Optional.empty();
  }

  private static String replaceCharForUrl(final String aValue) {
    return aValue
        .toLowerCase()
        .replace(' ', '-')
        .replace('.', '-')
        .replace(',', '-')
        .replace(":", "")
        .replace("\"", "")
        .replace("--", "-");
  }

  private static String parseShow(final JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_SHOW)) {
      final JsonElement showElement = aJsonObject.get(ELEMENT_SHOW);
      if (!showElement.isJsonNull()) {
        final JsonObject showObject = showElement.getAsJsonObject();
        if (showObject.has(ATTRIBUTE_TITLE)) {
          return showObject.get(ATTRIBUTE_TITLE).getAsString();
        }
      }
    }

    return "";
  }

  private static String extractSubtitleFromVideoUrl(String videoUrl) {
    try {
      Optional<String> subtitleBaseUrl = UrlUtils.getUrlParameterValue(videoUrl, "webvttbaseurl");
      Optional<String> caption = UrlUtils.getUrlParameterValue(videoUrl, "caption");

      if (subtitleBaseUrl.isPresent() && caption.isPresent()) {
        return String.format(
            "%s//%s/%s",
            UrlUtils.PROTOCOL_HTTPS,
            subtitleBaseUrl.get(),
            convertVideoCaptionToSubtitleFile(caption.get()));
      }

    } catch (UrlParseException e) {
      LOG.error("SRF: error parsing subtitleUrl", e);
    }

    return "";
  }

  private static String convertVideoCaptionToSubtitleFile(String caption) {
    String subtitle = caption.replace("vod.m3u8", "vod.vtt");
    return subtitle.split(":")[0];
  }

  private static String parseSubtitleList(final JsonElement aSubtitleListElement) {
    if (!aSubtitleListElement.isJsonArray()) {
      return "";
    }

    final JsonArray subtitleArray = aSubtitleListElement.getAsJsonArray();
    for (final JsonElement arrayItemElement : subtitleArray) {
      if (!arrayItemElement.isJsonNull()) {
        final JsonObject arrayItemObject = arrayItemElement.getAsJsonObject();

        if (arrayItemObject.has(ATTRIBUTE_FORMAT)
            && arrayItemObject.has(ATTRIBUTE_URL)
            && arrayItemObject.get(ATTRIBUTE_FORMAT).getAsString().equals(SUBTITLE_FORMAT)) {
          return arrayItemObject.get(ATTRIBUTE_URL).getAsString();
        }
      }
    }

    return "";
  }

  private static String parseResourceList(final JsonElement aResourceListElement) {
    if (!aResourceListElement.isJsonArray()) {
      return "";
    }

    String url = "";

    final JsonArray resourceArray = aResourceListElement.getAsJsonArray();
    for (final JsonElement arrayItemElement : resourceArray) {
      if (!arrayItemElement.isJsonNull()) {
        final JsonObject arrayItemObject = arrayItemElement.getAsJsonObject();

        if (arrayItemObject.has(ATTRIBUTE_MIMETYPE)
            && arrayItemObject.has(ATTRIBUTE_URL)
            && arrayItemObject.get(ATTRIBUTE_MIMETYPE).getAsString().contains("x-mpegURL")) {
          if (url.isEmpty()
              || (arrayItemObject.has(ATTRIBUTE_QUALITY)
                  && arrayItemObject.get(ATTRIBUTE_QUALITY).getAsString().compareToIgnoreCase("HD")
                      == 0)) {
            url = arrayItemObject.get(ATTRIBUTE_URL).getAsString();
          }
        }
      }
    }

    return url;
  }

  private static Optional<Resolution> getResolution(final M3U8Dto aDto) {
    final Optional<Resolution> resolution = aDto.getResolution();

    if (!resolution.isPresent()) {
      final Optional<String> codecMeta = aDto.getMeta(M3U8Constants.M3U8_CODECS);

      // Codec muss "avcl" beinhalten, sonst ist es kein Video
      if (codecMeta.isPresent() && !codecMeta.get().contains("avc1")) {
        return Optional.empty();
      }

      final Optional<String> widthMeta = aDto.getMeta(M3U8Constants.M3U8_BANDWIDTH);

      // Bandbreite verwenden
      if (widthMeta.isPresent()) {
        final int width = Integer.parseInt(widthMeta.get());

        if (width <= 700000) {
          return Optional.of(Resolution.SMALL);
        } else if (width > 3000000) {
          return Optional.of(Resolution.HD);
        } else {
          return Optional.of(Resolution.NORMAL);
        }
      }
    }

    return resolution;
  }

  @Override
  public Optional<Film> deserialize(
      final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aContext) {

    final JsonObject object = aJsonElement.getAsJsonObject().getAsJsonObject(ELEMENT_DATA);
    final String theme = parseShow(object);

    final Optional<String> title = JsonUtils.getAttributeAsString(object, ATTRIBUTE_TITLE);
    final Optional<String> description = JsonUtils.getAttributeAsString(object, ATTRIBUTE_LEAD);
    Duration duration = parseDuration(object);
    LocalDateTime date = parseDate(object);

    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.SRF,
            title.get(),
            theme,
            date,
            duration);

    description.ifPresent(film::setBeschreibung);

    //film.setWebsite(buildWebsiteUrl(chapterList.id, episodeData.title, theme).orElse(null));
    if (!addUrls(object, film)) {
      return Optional.empty();
    };
    //addSubtitle(chapterList.subtitleUrl, film);

    return Optional.of(film);
  }

  private Duration parseDuration(final JsonObject data) {
    if (data.has(ATTRIBUTE_DURATION)) {
      final long duration = data.get(ATTRIBUTE_DURATION).getAsLong();
      return Duration.of(duration, ChronoUnit.MILLIS);
    }

    return Duration.ZERO;
  }

  private LocalDateTime parseDate(final JsonObject data) {
    if (data.has(ATTRIBUTE_PUBLISH_DATE)) {
      final String date = data.get(ATTRIBUTE_PUBLISH_DATE).getAsString();
      return LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    return LocalDateTime.MIN;
  }

  private EpisodeData parseEpisode(final JsonObject aJsonObject) {
    final EpisodeData result = new EpisodeData();

    if (aJsonObject.has(ELEMENT_EPISODE)) {
      final JsonElement episodeElement = aJsonObject.get(ELEMENT_EPISODE);

      if (!episodeElement.isJsonNull()) {
        final JsonObject episodeObject = episodeElement.getAsJsonObject();

        if (episodeObject.has(ATTRIBUTE_TITLE)) {
          result.title = episodeObject.get(ATTRIBUTE_TITLE).getAsString();
        }


      }
    }

    return result;
  }

  private ChapterListData parseChapterList(final JsonObject aJsonObject) {
    final ChapterListData result = new ChapterListData();

    if (!aJsonObject.has(ELEMENT_CHAPTER_LIST)) {
      return result;
    }

    final JsonElement chapterListElement = aJsonObject.get(ELEMENT_CHAPTER_LIST);
    if (chapterListElement.isJsonNull()) {
      return result;
    }

    final JsonArray chapterListArray = chapterListElement.getAsJsonArray();
    if (chapterListArray.size() != 1) {
      return result;
    }

    final JsonObject chapterListEntry = chapterListArray.get(0).getAsJsonObject();
    if (chapterListEntry.has(ATTRIBUTE_ID)) {
      result.id = chapterListEntry.get(ATTRIBUTE_ID).getAsString();
    }



    if (chapterListEntry.has(ATTRIBUTE_DESCRIPTION)) {
      result.description = chapterListEntry.get(ATTRIBUTE_DESCRIPTION).getAsString();
    } else if (chapterListEntry.has(ATTRIBUTE_LEAD)) {
      result.description = chapterListEntry.get(ATTRIBUTE_LEAD).getAsString();
    }

    if (chapterListEntry.has(ELEMENT_RESOURCE_LIST)) {
      result.videoUrl = parseResourceList(chapterListEntry.get(ELEMENT_RESOURCE_LIST));
    }

    if (chapterListEntry.has(ELEMENT_SUBTITLE_LIST)) {
      result.subtitleUrl = parseSubtitleList(chapterListEntry.get(ELEMENT_SUBTITLE_LIST));
    } else {
      result.subtitleUrl = extractSubtitleFromVideoUrl(result.videoUrl);
    }

    return result;
  }

  private EnumMap readUrls(final String aM3U8Url) {
    final EnumMap urls = new EnumMap(Resolution.class);
    final String optimizedUrl = getOptimizedUrl(aM3U8Url);

    Optional<String> content = loadM3u8(optimizedUrl);
    if (!content.isPresent()) {
      content = loadM3u8(aM3U8Url);
    }

    if (content.isPresent()) {
      final M3U8Parser parser = new M3U8Parser();
      final List<M3U8Dto> m3u8Data = parser.parse(content.get());
      m3u8Data.forEach(
          entry -> {
            final Optional<Resolution> resolution = getResolution(entry);
            if (resolution.isPresent()) {
              urls.put(resolution.get(), entry.getUrl());
            }
          });

    } else {
      LOG.error(String.format("SrfFilmJsonDeserializer: Loading m3u8-url failed: %s", aM3U8Url));
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }

    return urls;
  }

  private Optional<String> loadM3u8(final String aM3U8Url) {

    final MVHttpClient mvhttpClient = MVHttpClient.getInstance();
    final OkHttpClient httpClient = mvhttpClient.getHttpClient();
    final Request request = new Request.Builder().url(aM3U8Url).build();

    try (final Response response = httpClient.newCall(request).execute();
        final ResponseBody body = response.body()) {
      if (response.isSuccessful() && body != null) {
        return Optional.of(body.string());
      }
    } catch (final Exception e) {
      LOG.error(e);
    }

    return Optional.empty();
  }

  private String getOptimizedUrl(final String aM3U8Url) {

    if (!aM3U8Url.contains("q10,q20,q30,.mp4.csmil")) {
      return aM3U8Url;
    }

    return aM3U8Url.replace("q10,q20,q30,.mp4.csmil", "q10,q20,q30,q50,q60,.mp4.csmil");
  }

  private class EpisodeData {

    String title;
    LocalDateTime publishDate;
  }

  private class ChapterListData {

    Duration duration;
    String id;
    String description = "";
    String videoUrl = "";
    String subtitleUrl = "";
  }
}
