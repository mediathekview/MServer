package mServer.crawler.sender.srf.parser;

import com.google.gson.*;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.base.*;
import mServer.crawler.sender.srf.SrfConstants;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SrfFilmJsonDeserializer implements JsonDeserializer<Optional<DatenFilm>> {

  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SrfFilmJsonDeserializer.class);

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
  private static final String TEXT_AUDIO_DESCRIPTION = " mit Audiodeskription";

  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static String buildWebsiteUrl(String aId, String aTitle, String aTheme) {

    return (SrfConstants.WEBSITE_URL).formatted(
            replaceCharForUrl(aTheme), replaceCharForUrl(aTitle), aId);
  }

  private static String replaceCharForUrl(String aValue) {
    return aValue.toLowerCase().replace(' ', '-').replace('.', '-').replace(',', '-').replace(":", "").replace("\"", "")
            .replace("--", "-");
  }

  private static String parseShow(JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_SHOW)) {
      JsonElement showElement = aJsonObject.get(ELEMENT_SHOW);
      if (!showElement.isJsonNull()) {
        JsonObject showObject = showElement.getAsJsonObject();
        if (showObject.has(ATTRIBUTE_TITLE)) {
          return showObject.get(ATTRIBUTE_TITLE).getAsString();
        }
      }
    }

    return "";
  }

  private static String parseSubtitleList(JsonElement aSubtitleListElement) {
    if (!aSubtitleListElement.isJsonArray()) {
      return "";
    }

    JsonArray subtitleArray = aSubtitleListElement.getAsJsonArray();
    for (JsonElement arrayItemElement : subtitleArray) {
      if (!arrayItemElement.isJsonNull()) {
        JsonObject arrayItemObject = arrayItemElement.getAsJsonObject();

        if (arrayItemObject.has(ATTRIBUTE_FORMAT)
                && arrayItemObject.has(ATTRIBUTE_URL)
                && arrayItemObject.get(ATTRIBUTE_FORMAT).getAsString().equals(SUBTITLE_FORMAT)) {
          return arrayItemObject.get(ATTRIBUTE_URL).getAsString();
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
                "%s/%s".formatted(
                        subtitleBaseUrl.get(),
                        convertVideoCaptionToSubtitleFile(caption.get()));

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

  private static String parseResourceList(JsonElement aResourceListElement) {
    if (!aResourceListElement.isJsonArray()) {
      return "";
    }

    String url = "";

    JsonArray resourceArray = aResourceListElement.getAsJsonArray();
    for (JsonElement arrayItemElement : resourceArray) {
      if (!arrayItemElement.isJsonNull()) {
        JsonObject arrayItemObject = arrayItemElement.getAsJsonObject();

        if (!arrayItemObject.has(ATTRIBUTE_DRM_LIST)
                && arrayItemObject.has(ATTRIBUTE_MIMETYPE)
                && arrayItemObject.has(ATTRIBUTE_URL)
                && arrayItemObject.get(ATTRIBUTE_MIMETYPE).getAsString().contains("x-mpegURL")) {
          if (url.isEmpty()
                  || (arrayItemObject.has(ATTRIBUTE_QUALITY)
                  && arrayItemObject.get(ATTRIBUTE_QUALITY).getAsString().compareToIgnoreCase("HD") == 0)) {
            url = arrayItemObject.get(ATTRIBUTE_URL).getAsString();

          }
        }
      }
    }

    return url;
  }

  private static Optional<Qualities> getResolution(M3U8Dto aDto) {
    Optional<Qualities> resolution = aDto.getResolution();

    if (resolution.isEmpty()) {
      Optional<String> codecMeta = aDto.getMeta(M3U8Constants.M3U8_CODECS);

      // Codec muss "avcl" beinhalten, sonst ist es kein Video
      if (codecMeta.isPresent() && !codecMeta.get().contains("avc1")) {
        return Optional.empty();
      }

      Optional<String> widthMeta = aDto.getMeta(M3U8Constants.M3U8_BANDWIDTH);

      // Bandbreite verwenden
      if (widthMeta.isPresent()) {
        int width = Integer.parseInt(widthMeta.get());

        if (width <= 700000) {
          return Optional.of(Qualities.SMALL);
        } else if (width > 3000000) {
          return Optional.of(Qualities.HD);
        } else {
          return Optional.of(Qualities.NORMAL);
        }
      }
    }

    return resolution;
  }

  @Override
  public Optional<DatenFilm> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    JsonObject object = aJsonElement.getAsJsonObject();
    String theme = parseShow(object);
    final boolean isAudioDescription = theme.contains(TEXT_AUDIO_DESCRIPTION);
    EpisodeData episodeData = parseEpisode(object);
    ChapterListData chapterList = parseChapterList(object);

    if (chapterList.videoUrl.equals("")) {
      return Optional.empty();
    }

    Map<Qualities, String> videoUrls = readUrls(chapterList.videoUrl);
    if (videoUrls.isEmpty() || !videoUrls.containsKey(Qualities.NORMAL)) {
      return Optional.empty();
    }

    String documentUrl = buildWebsiteUrl(chapterList.id, episodeData.title, theme);
    String date = "";
    String time = "";

    try {
      date = episodeData.publishDate.format(dateFormatDatenFilm);
      time = episodeData.publishDate.format(timeFormatDatenFilm);
    } catch (DateTimeParseException ex) {
      LOG.error(documentUrl, ex);
    }

    DatenFilm film = new DatenFilm(Const.SRF,
            isAudioDescription ? theme.replace(TEXT_AUDIO_DESCRIPTION, "").trim() : theme,
            documentUrl,
            isAudioDescription ? episodeData.title.replace(TEXT_AUDIO_DESCRIPTION, "").trim() + " (Audiodeskription)" : episodeData.title,
            videoUrls.get(Qualities.NORMAL), "",
            date, time,
            chapterList.duration.getSeconds(),
            chapterList.description);

    if (videoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
    }
    if (videoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
    }
    if (!chapterList.subtitleUrl.isEmpty()) {
      CrawlerTool.addUrlSubtitle(film, chapterList.subtitleUrl);
    }

    return Optional.of(film);
  }

  private EpisodeData parseEpisode(JsonObject aJsonObject) {
    EpisodeData result = new EpisodeData();

    if (aJsonObject.has(ELEMENT_EPISODE)) {
      JsonElement episodeElement = aJsonObject.get(ELEMENT_EPISODE);

      if (!episodeElement.isJsonNull()) {
        JsonObject episodeObject = episodeElement.getAsJsonObject();

        if (episodeObject.has(ATTRIBUTE_TITLE)) {
          result.title = episodeObject.get(ATTRIBUTE_TITLE).getAsString();
        }

        if (episodeObject.has(ATTRIBUTE_PUBLISHED_DATE)) {
          String date = episodeObject.get(ATTRIBUTE_PUBLISHED_DATE).getAsString();
          result.publishDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
      }
    }

    return result;
  }

  private ChapterListData parseChapterList(JsonObject aJsonObject) {
    ChapterListData result = new ChapterListData();

    if (!aJsonObject.has(ELEMENT_CHAPTER_LIST)) {
      return result;
    }

    JsonElement chapterListElement = aJsonObject.get(ELEMENT_CHAPTER_LIST);
    if (chapterListElement.isJsonNull()) {
      return result;
    }

    JsonArray chapterListArray = chapterListElement.getAsJsonArray();
    if (chapterListArray.size() != 1) {
      return result;
    }

    JsonObject chapterListEntry = chapterListArray.get(0).getAsJsonObject();
    if (chapterListEntry.has(ATTRIBUTE_ID)) {
      result.id = chapterListEntry.get(ATTRIBUTE_ID).getAsString();
    }

    if (chapterListEntry.has(ATTRIBUTE_DURATION)) {
      long duration = chapterListEntry.get(ATTRIBUTE_DURATION).getAsLong();
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

  private Map<Qualities, String> readUrls(String aM3U8Url) {
    Map<Qualities, String> urls = new EnumMap<>(Qualities.class);
    final String optimizedUrl = UrlUtils.removeParameters(getOptimizedUrl(aM3U8Url));

    Optional<String> content = loadM3u8(optimizedUrl);
    if (content.isEmpty()) {
      content = loadM3u8(aM3U8Url);
    }

    if (content.isPresent()) {
      M3U8Parser parser = new M3U8Parser();
      List<M3U8Dto> m3u8Data = parser.parse(content.get());
      m3u8Data.forEach(entry -> {
        Optional<Qualities> resolution = getResolution(entry);
        if (resolution.isPresent()) {
          urls.put(resolution.get(), enrichUrl(optimizedUrl, entry.getUrl()));
        }
      });

    } else {
      LOG.error("SrfFilmJsonDeserializer: Loading m3u8-url failed: {}", aM3U8Url);
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

  private Optional<String> loadM3u8(String aM3U8Url) {

    Request request = new Request.Builder()
            .url(aM3U8Url).build();

    try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
         ResponseBody body = response.body()) {
      if (response.isSuccessful() && body != null) {
        return Optional.of(body.string());
      }
    } catch (Exception e) {
      LOG.error(e);
    }

    return Optional.empty();
  }

  private String getOptimizedUrl(String aM3U8Url) {

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
