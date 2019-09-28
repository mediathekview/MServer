package de.mediathekview.mserver.crawler.wdr.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mserver.base.utils.GeoLocationGuesser;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.WdrMediaDto;
import de.mediathekview.mserver.crawler.wdr.WdrVideoInfoDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_CONTENT;

public class WdrFilmDeserializer {

  private static final Logger LOG = LogManager.getLogger(WdrFilmDeserializer.class);

  private static final String DESCRIPTION_SELECTOR = "meta[property=og:description]";
  private static final String DURATION_SELECTOR = "meta[property=video:duration]";
  private static final String TIME_SELECTOR = "meta[name=dcterms.date]";
  private static final String TITLE_SELECTOR = "meta[property=og:title]";
  private static final String VIDEO_LINK_SELECTOR = "div.videoLink > a";

  private static final String ATTRIBUTE_DATA_EXTENSION = "data-extension";

  private static final Type OPTIONAL_CRAWLERURLDTO_TYPE_TOKEN =
      new TypeToken<Optional<CrawlerUrlDTO>>() {}.getType();
  private static final Type OPTIONAL_WDRMEDIADTO_TYPE_TOKEN =
      new TypeToken<Optional<WdrMediaDto>>() {}.getType();

  private final Sender sender;
  private final Gson gson;

  public WdrFilmDeserializer(final String aProtocol, final Sender aSender) {
    gson =
        new GsonBuilder()
            .registerTypeAdapter(OPTIONAL_CRAWLERURLDTO_TYPE_TOKEN, new WdrVideoLinkDeserializer())
            .registerTypeAdapter(
                OPTIONAL_WDRMEDIADTO_TYPE_TOKEN, new WdrVideoJsonDeserializer(aProtocol))
            .create();

    sender = aSender;
  }

  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    final Optional<String> dateTime =
        HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (dateTime.isPresent()) {
      final LocalDateTime localDateTime =
          LocalDateTime.parse(dateTime.get(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return Optional.of(localDateTime);
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final Document aDocument) {
    final Optional<String> duration =
        HtmlDocumentUtils.getElementAttributeString(
            DURATION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (!duration.isPresent()) {
      return Optional.empty();
    }

    final Long durationValue = Long.parseLong(duration.get());
    return Optional.of(Duration.ofSeconds(durationValue));
  }

  /**
   * reads an url.
   *
   * @param aUrl the url
   * @return the content of the url
   */
  private static Optional<String> readContent(final String aUrl) {
    final OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
    final Request request = new Request.Builder().url(aUrl).build();
    try (final okhttp3.Response response = httpClient.newCall(request).execute();
        final ResponseBody body = response.body()) {
      if (response.isSuccessful() && body != null) {
        return Optional.of(body.string());
      } else {
        LOG.error(
            String.format("WdrFilmDetailTask: Request '%s' failed: %s", aUrl, response.code()));
      }
    } catch (final IOException ex) {
      LOG.error("WdrFilmDetailTask: ", ex);
    }

    return Optional.empty();
  }

  /**
   * parses javascript containing media infos and extract the embedded json.
   *
   * @param aJsContent the javscript content
   * @return the embedded json content
   */
  private static Optional<String> extractJsonFromJavaScript(final Optional<String> aJsContent) {
    if (aJsContent.isPresent()) {
      final int indexBegin = aJsContent.get().indexOf('(');
      final int indexEnd = aJsContent.get().lastIndexOf(')');
      final String embeddedJson = aJsContent.get().substring(indexBegin + 1, indexEnd);
      return Optional.of(embeddedJson);
    }

    return Optional.empty();
  }

  public Optional<Film> deserialize(final TopicUrlDTO aUrlDto, final Document aDocument) {
    final Optional<String> title = parseTitle(aDocument, aUrlDto.getTopic());
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);
    final Optional<String> description =
        HtmlDocumentUtils.getElementAttributeString(
            DESCRIPTION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    final Optional<WdrVideoInfoDto> urls = parseUrls(aDocument);

    return createFilm(aUrlDto, urls, title, description, time, duration);
  }

  private void addUrls(final Film aFilm, final WdrVideoInfoDto aVideoInfo)
      throws MalformedURLException {

    Map<Resolution, String> videoUrls = aVideoInfo.getVideoUrls();
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), new FilmUrl(qualitiesEntry.getValue()));
    }

    videoUrls = aVideoInfo.getAudioDescriptionUrls();
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      aFilm.addAudioDescription(qualitiesEntry.getKey(), new FilmUrl(qualitiesEntry.getValue()));
    }

    videoUrls = aVideoInfo.getSignLanguageUrls();
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      aFilm.addSignLanguage(qualitiesEntry.getKey(), new FilmUrl(qualitiesEntry.getValue()));
    }
  }

  private Optional<Film> createFilm(
      final TopicUrlDTO aUrlDto,
      final Optional<WdrVideoInfoDto> aVideoInfo,
      final Optional<String> aTitle,
      final Optional<String> aDescription,
      final Optional<LocalDateTime> aTime,
      final Optional<Duration> aDuration) {

    try {
      if (aVideoInfo.isPresent() && aTitle.isPresent()) {
        final Film film =
            new Film(
                UUID.randomUUID(),
                sender,
                aTitle.get(),
                aUrlDto.getTopic(),
                aTime.orElse(LocalDateTime.now()),
                aDuration.orElse(Duration.ZERO));

        film.setWebsite(new URL(aUrlDto.getUrl()));
        if (aDescription.isPresent()) {
          film.setBeschreibung(aDescription.get());
        }

        final WdrVideoInfoDto videoInfo = aVideoInfo.get();
        if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
          film.addSubtitle(new URL(videoInfo.getSubtitleUrl()));
        }

        addUrls(film, videoInfo);
        film.setGeoLocations(
            GeoLocationGuesser.getGeoLocations(sender, videoInfo.getDefaultVideoUrl()));

        return Optional.of(film);

      } else {
        LOG.error("WdrFilmDeserializer: no title or video found for url " + aUrlDto.getUrl());
      }
    } catch (final MalformedURLException ex) {
      LOG.fatal("WdrFilmDeserializer: url can't be parsed.", ex);
    }

    return Optional.empty();
  }

  private Optional<WdrVideoInfoDto> parseUrls(final Document aDocument) {
    final Optional<CrawlerUrlDTO> videoUrlDto = parseVideoLink(aDocument);
    if (!videoUrlDto.isPresent()) {
      return Optional.empty();
    }
    final Optional<String> javaScriptContent = readContent(videoUrlDto.get().getUrl());
    final Optional<String> embeddedJson = extractJsonFromJavaScript(javaScriptContent);
    if (!embeddedJson.isPresent()) {
      return Optional.empty();
    }

    final Optional<WdrMediaDto> mediaDto =
        gson.fromJson(embeddedJson.get(), OPTIONAL_WDRMEDIADTO_TYPE_TOKEN);
    if (!mediaDto.isPresent()) {
      return Optional.empty();
    }

    final WdrMediaDto dto = mediaDto.get();
    if (dto.getUrl().endsWith(".m3u8")) {
      return parseM3U8(dto);
    } else if (dto.getUrl().endsWith(".mp4")) {
      return parseMP4Url(dto);
    }

    return Optional.empty();
  }

  private Optional<WdrVideoInfoDto> parseM3U8(final WdrMediaDto aMediaDto) {
    final Map<Resolution, String> urlMap = parseM3U8Url(aMediaDto.getUrl());
    if (!urlMap.isEmpty()) {
      final WdrVideoInfoDto videoInfoDto = new WdrVideoInfoDto();
      urlMap.forEach(videoInfoDto::putVideo);

      if (aMediaDto.getSubtitle().isPresent()) {
        videoInfoDto.setSubtitleUrl(aMediaDto.getSubtitle().get());
      }

      if (aMediaDto.getAudioDescriptionUrl().isPresent()) {
        final Map<Resolution, String> adUrlMap =
            parseM3U8Url(aMediaDto.getAudioDescriptionUrl().get());
        adUrlMap.forEach(videoInfoDto::putAudioDescription);
      }

      if (aMediaDto.getSignLanguageUrl().isPresent()) {
        final Map<Resolution, String> slUrlMap = parseM3U8Url(aMediaDto.getSignLanguageUrl().get());
        slUrlMap.forEach(videoInfoDto::putSignLanguage);
      }

      return Optional.of(videoInfoDto);
    }

    return Optional.empty();
  }

  private Optional<WdrVideoInfoDto> parseMP4Url(final WdrMediaDto aMediaDto) {
    final WdrVideoInfoDto dto = new WdrVideoInfoDto();
    dto.putVideo(Resolution.NORMAL, aMediaDto.getUrl());

    return Optional.of(dto);
  }

  /**
   * parses the video link.
   *
   * @param aDocument the html document
   * @return the url of the javascript file containing media infos
   */
  private Optional<CrawlerUrlDTO> parseVideoLink(final Document aDocument) {
    final Optional<String> videoLink =
        HtmlDocumentUtils.getElementAttributeString(
            VIDEO_LINK_SELECTOR, ATTRIBUTE_DATA_EXTENSION, aDocument);
    if (videoLink.isPresent()) {

      return gson.fromJson(videoLink.get(), OPTIONAL_CRAWLERURLDTO_TYPE_TOKEN);
    }

    return Optional.empty();
  }

  private Map<Resolution, String> parseM3U8Url(final String aUrl) {
    final Map<Resolution, String> urlMap = new EnumMap<>(Resolution.class);

    final Optional<String> m3u8Content = readContent(aUrl);
    if (!m3u8Content.isPresent()) {
      return urlMap;
    }

    final M3U8Parser parser = new M3U8Parser();
    final List<M3U8Dto> m3u8Data = parser.parse(m3u8Content.get());

    m3u8Data.forEach(
        entry -> {
          final Optional<Resolution> resolution = entry.getResolution();
          resolution.ifPresent(value -> urlMap.put(value, entry.getUrl()));
        });

    return urlMap;
  }

  private Optional<String> parseTitle(final Document aDocument, final String aTopic) {
    final Optional<String> titleValue =
        HtmlDocumentUtils.getElementAttributeString(TITLE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (!titleValue.isPresent()) {
      return Optional.empty();
    }

    String title = titleValue.get();
    if (title.startsWith(aTopic) && !title.equals(aTopic)) {
      title = title.replaceFirst(aTopic, "").trim();
      if (title.trim().startsWith("-")) {
        title = title.replaceFirst("-", "").trim();
      }
      if (title.trim().startsWith(":")) {
        title = title.replaceFirst(":", "").trim();
      }
    }

    return Optional.of(title);
  }
}
