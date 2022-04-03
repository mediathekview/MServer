package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.*;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.UrlParseException;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.M3U8Constants;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SrfFilmJsonDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final org.apache.logging.log4j.Logger LOG =
      LogManager.getLogger(SrfFilmJsonDeserializer.class);

  private static final String ATTRIBUTE_DESCRIPTION = "description";
  private static final String ATTRIBUTE_DRM_LIST = "drmList";
  private static final String ATTRIBUTE_DURATION = "duration";
  private static final String ATTRIBUTE_FORMAT = "format";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_LEAD = "lead";
  private static final String ATTRIBUTE_MIMETYPE = "mimeType";
  private static final String ATTRIBUTE_PUBLISHED_DATE = "publishedDate";
  private static final String ATTRIBUTE_QUALITY = "quality";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_URL = "url";

  private static final String ELEMENT_CHAPTER_LIST = "chapterList";
  private static final String ELEMENT_EPISODE = "episode";
  private static final String ELEMENT_RESOURCE_LIST = "resourceList";
  private static final String ELEMENT_SHOW = "show";
  private static final String ELEMENT_SUBTITLE_LIST = "subtitleList";

  private static final String SUBTITLE_FORMAT = "TTML";

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

  private void addUrls(final Map<Resolution, String> aVideoUrls, final Film aFilm) {
    aVideoUrls.forEach(
        (key, value) -> {
          try {
            aFilm.addUrl(key, new FilmUrl(value, crawler.determineFileSizeInKB(value)));
          } catch (final MalformedURLException | IllegalArgumentException ex) {
            LOG.error(String.format("A found download URL \"%s\" isn't valid.", value), ex);
          }
        });
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
        String subtitleUrl =
            String.format(
                "%s/%s", subtitleBaseUrl.get(), convertVideoCaptionToSubtitleFile(caption.get()));

        return UrlUtils.addProtocolIfMissing(subtitleUrl, UrlUtils.PROTOCOL_HTTPS);
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

        if (!arrayItemObject.has(ATTRIBUTE_DRM_LIST)
            && arrayItemObject.has(ATTRIBUTE_MIMETYPE)
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

    if (resolution.isEmpty()) {
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

    final JsonObject object = aJsonElement.getAsJsonObject();
    final String theme = parseShow(object);
    final EpisodeData episodeData = parseEpisode(object);
    final ChapterListData chapterList = parseChapterList(object);

    if (chapterList.videoUrl.equals("")) {
      return Optional.empty();
    }

    final Map<Resolution, String> videoUrls = readUrls(chapterList.videoUrl);
    if (videoUrls.isEmpty()) {
      return Optional.empty();
    }

    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.SRF,
            episodeData.title,
            theme,
            episodeData.publishDate,
            chapterList.duration);
    film.setBeschreibung(chapterList.description);
    film.setWebsite(buildWebsiteUrl(chapterList.id, episodeData.title, theme).orElse(null));
    addUrls(videoUrls, film);
    addSubtitle(chapterList.subtitleUrl, film);

    return Optional.of(film);
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

        if (episodeObject.has(ATTRIBUTE_PUBLISHED_DATE)) {
          final String date = episodeObject.get(ATTRIBUTE_PUBLISHED_DATE).getAsString();
          result.publishDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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

    if (chapterListEntry.has(ATTRIBUTE_DURATION)) {
      final long duration = chapterListEntry.get(ATTRIBUTE_DURATION).getAsLong();
      result.duration = Duration.of(duration, ChronoUnit.MILLIS);
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

  private Map<Resolution, String> readUrls(final String aM3U8Url) {
    Map<Resolution, String> urls = new EnumMap<>(Resolution.class);
    final String optimizedUrl = getOptimizedUrl(aM3U8Url);
    Optional<String> content;

    try {
      content = Optional.of(crawler.requestBodyAsString(optimizedUrl));
      if (content.isEmpty() || content.get().length() == 0) {
        content = Optional.of(crawler.requestBodyAsString(aM3U8Url));
      }

      if (content.isPresent() && content.get().length() > 0) {
        final M3U8Parser parser = new M3U8Parser();
        final List<M3U8Dto> m3u8Data = parser.parse(content.get());
        m3u8Data.forEach(
            entry -> {
              final Optional<Resolution> resolution = getResolution(entry);
              if (resolution.isPresent()) {
                urls.put(resolution.get(), enrichUrl(optimizedUrl, entry.getUrl()));
              }
            });

      } else {
        LOG.error("SrfFilmJsonDeserializer: Loading m3u8-url failed: {}", aM3U8Url);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (Exception e) {
      LOG.error("SrfFilmJsonDeserializer: Loading m3u8-url failed: {}", aM3U8Url);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }

    return urls;
  }

  private String enrichUrl(String m3u8Url, String videoUrl) {
    // some video urls contain only filename
    if (UrlUtils.getProtocol(videoUrl).isEmpty()) {
      final String m3u8WithoutParameters = UrlUtils.removeParameters(m3u8Url);
      final Optional<String> m3u8File = UrlUtils.getFileName(m3u8WithoutParameters);
      if (m3u8File.isPresent()) {
        return m3u8WithoutParameters.replace(m3u8File.get(), videoUrl);
      }

      final Optional<String> lastSegment = UrlUtils.getLastSegment(m3u8WithoutParameters);
      if (lastSegment.isPresent()) {
        return m3u8WithoutParameters.replace(lastSegment.get(), videoUrl);
      }
    }
    return videoUrl;
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
