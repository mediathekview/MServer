package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.*;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.GeoLocationGuesser;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ArdFilmDeserializer implements JsonDeserializer<List<ArdFilmDto>> {

  private static final org.apache.logging.log4j.Logger LOG =
      LogManager.getLogger(ArdFilmDeserializer.class);

  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";

  private static final String ELEMENT_EMBEDDED = "embedded";
  private static final String ELEMENT_MEDIA_COLLECTION = "mediaCollection";
  private static final String ELEMENT_PUBLICATION_SERVICE = "publicationService";
  private static final String ELEMENT_SHOW = "show";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_WIDGETS = "widgets";
  private static final String[] ELEMENT_SUBTITLES = {"mediaCollection","embedded","subtitles"};
  private static final String ELEMENT_SOURCES = "sources";
  private static final String ELEMENT_STREAMS = "streams";
  private static final String ELEMENT_MEDIA = "media";
  private static final String ELEMENT_AUDIO = "audios";
  

  private static final String ATTRIBUTE_BROADCAST = "broadcastedOn";
  private static final String[] ATTRIBUTE_DURATION = {"meta","duration"};
  private static final String[] ATTRIBUTE_DURATION_SEC = {"meta","durationSeconds"};
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_PARTNER = "partner";
  private static final String ATTRIBUTE_SYNOPSIS = "synopsis";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_RESOLUTION_H = "maxHResolutionPx";
  private static final String ATTRIBUTE_MIME = "mimeType";
  private static final String ATTRIBUTE_KIND = "kind";

  private static final String MARKER_VIDEO_MP4 = "video/mp4"; 
  private static final String MARKER_VIDEO_STANDARD = "standard";
  private static final String MARKER_VIDEO_CATEGORY_MAIN = "main";
  private static final String MARKER_VIDEO_CATEGORY_MPEG = "application/vnd.apple.mpegurl";
  private static final String MARKER_VIDEO_AD = "audio-description";
  private static final String MARKER_VIDEO_DGS = "sign-language";  

  private final ArdVideoInfoJsonDeserializer videoDeserializer;
  private final AbstractCrawler crawler;

  public ArdFilmDeserializer(final AbstractCrawler crawler) {
    videoDeserializer = new ArdVideoInfoJsonDeserializer(crawler);
    this.crawler = crawler;
  }

  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject itemObject) {
    Optional<JsonElement> mc = JsonUtils.getElement(itemObject, ELEMENT_MEDIA_COLLECTION, ELEMENT_EMBEDDED);
    if (mc.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(mc.get().getAsJsonObject());  
  }

  private static Optional<String> parseTopic(final JsonObject playerPageObject) {
    final Optional<String> topic;
    if (playerPageObject.has(ELEMENT_SHOW) && !playerPageObject.get(ELEMENT_SHOW).isJsonNull()) {
      final JsonObject showObject = playerPageObject.get(ELEMENT_SHOW).getAsJsonObject();
      topic = JsonUtils.getAttributeAsString(showObject, ATTRIBUTE_TITLE);
    } else {
      // no show element found -> use title as topic
      topic = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    }
    // remove time in topic
    if (topic.isPresent() && topic.get().contains("MDR aktuell")) {
      return Optional.of(topic.get().replaceAll("\\d{2}:\\\\d{2} Uhr$", "").trim());
    }

    return topic;
  }

  private static Optional<LocalDateTime> parseDate(final JsonObject playerPageObject) {
    final Optional<String> dateValue =
        JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_BROADCAST);
    if (dateValue.isPresent()) {
      try {
        final ZonedDateTime inputDateTime = ZonedDateTime.parse(dateValue.get());
        final LocalDateTime localDateTime =
            inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      } catch (final DateTimeParseException ex) {
        LOG.error("Error parsing date time value {}", dateValue.get(), ex);
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent()) {
      final Optional<JsonElement> durationElement = JsonUtils.getElement(mediaCollectionObject.get(), ATTRIBUTE_DURATION);
      final Optional<JsonElement> durationElementSec = JsonUtils.getElement(mediaCollectionObject.get(), ATTRIBUTE_DURATION_SEC);
      if (durationElement.isPresent()) {
        return Optional.of(Duration.ofSeconds(durationElement.get().getAsLong()));
      } else if (durationElementSec.isPresent()) {
        return Optional.of(Duration.ofSeconds(durationElementSec.get().getAsLong()));
      }
    }
    return Optional.empty();
  }

  private Optional<Set<String>> prepareSubtitleUrl(final JsonElement embeddedElement) {
    Optional<JsonElement> subtitle = JsonUtils.getElement(embeddedElement, ELEMENT_SUBTITLES);
    if (subtitle.isEmpty() || !subtitle.get().isJsonArray() || (subtitle.get().getAsJsonArray().size() == 0))
      return Optional.empty();
    Optional<JsonElement> sources = JsonUtils.getElement(subtitle.get().getAsJsonArray().get(0), ELEMENT_SOURCES);
    if (sources.isEmpty() || !sources.get().isJsonArray())
      return Optional.empty();
    Set<String> urls = new HashSet<>();
    for (JsonElement url : sources.get().getAsJsonArray()) {
      JsonUtils.getElementValueAsString(url, ATTRIBUTE_URL).ifPresent(urls::add);
    }
    return Optional.of(urls);
  }

  @Override
  public List<ArdFilmDto> deserialize(
      final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {

    final List<ArdFilmDto> films = new ArrayList<>();

    if (!JsonUtils.hasElements(jsonElement, ELEMENT_WIDGETS)
        || !jsonElement.getAsJsonObject().get(ELEMENT_WIDGETS).isJsonArray()) {
      return films;
    }

    final JsonArray widgets = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_WIDGETS);
    if (widgets.size() == 0) {
      return films;
    }

    final JsonObject itemObject = widgets.get(0).getAsJsonObject();

    final Optional<String> topic = parseTopic(itemObject);
    final Optional<String> title = parseTitle(itemObject);
    final Optional<String> description =
        JsonUtils.getAttributeAsString(itemObject, ATTRIBUTE_SYNOPSIS);
    final Optional<LocalDateTime> date = parseDate(itemObject);
    final Optional<Duration> duration = parseDuration(itemObject);
    //final Sender sender = determinePartner(itemObject);
    final Optional<String> partner = parsePartner(itemObject);
    final Sender sender = ArdConstants.PARTNER_TO_SENDER.get(partner.orElse(""));
    final Optional<ArdVideoInfoDto> videoInfo = parseVideos(itemObject, title.orElse(""));

    if(title.orElse("").contains("- Die Antwort auf fast")) {
      
      System.out.println("stop");
    }
    
    if (title.isEmpty() || topic.isEmpty() || videoInfo.isEmpty()) {
      return films;
    }
    
    if (sender == null) {
      LOG.error("Missing Partner {}", partner.orElse(""));
      if (partner.isEmpty()) {
        LOG.error("Missing Partner Element {}", jsonElement);
      }
      
      return films;
    }
    
    
    // add film to ARD
    final ArdFilmDto filmDto =
        new ArdFilmDto(
            createFilm(
                sender,
                topic.get(),
                title.get(),
                description.orElse(null),
                date.orElse(null),
                duration.orElse(null),
                videoInfo.get()));
    if (widgets.size() > 1) {
      parseRelatedFilms(filmDto, widgets.get(1).getAsJsonObject());
    }
    films.add(filmDto);
    return films;
  }
  
  private Optional<Map<Resolution, String>> fallbackToM3U(Optional<ArdVideoInfoDto> m3u) {
    if (m3u.isPresent() && !m3u.get().getVideoUrls().isEmpty()) {
      String m3uUrl = m3u.get().getVideoUrls().values().toArray(new String[1])[0];
      Map<Resolution, URL> resolutionUrlMapFromM3U;
      try {
        resolutionUrlMapFromM3U = videoDeserializer.loadM3U8(new URL(m3uUrl));
        if (resolutionUrlMapFromM3U.size() > 0) {
          Map<Resolution, String> newUrls = new EnumMap<>(Resolution.class);
          resolutionUrlMapFromM3U.forEach((key, value) -> newUrls.put(key, value.toString()));
          return Optional.of(newUrls);
        }
      } catch (MalformedURLException e) {
        LOG.error("Could not convert {} to url", m3uUrl, e );
      }
    }
    return Optional.empty();
  }

  private Optional<String> parseTitle(final JsonObject playerPageObject) {
    final Optional<String> title =
        JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    return title.map(nonNullTitle -> nonNullTitle.replace("Hörfassung", "Audiodeskription"));
  }
  
  /*
  private Sender determinePartner(JsonObject playerPageObject) {
    final Optional<String> partner = parsePartner(playerPageObject);
    // If partner is present and an existing sender set it. Like for RBB
    Sender partnerSender = null;
    if (partner.isPresent()) {
      partnerSender = ArdConstants.PARTNER_TO_SENDER.get(partner.get());
      if (partnerSender != null) {
        return partnerSender;
      } else {
        partnerSender = Sender.ARD;
        LOG.info("unkown partner {}", partner.get() );
      }
    } else {
      partnerSender = Sender.ARD;
      LOG.info("missing partner {}", playerPageObject);
    }
    return partnerSender;

  } */

  private Optional<String> parsePartner(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_PUBLICATION_SERVICE)) {
      final JsonObject publicationServiceObject =
          playerPageObject.get(ELEMENT_PUBLICATION_SERVICE).getAsJsonObject();
      final Optional<String> channelAttribute =
          JsonUtils.getAttributeAsString(publicationServiceObject, ATTRIBUTE_PARTNER);
      if (channelAttribute.isPresent()) {
        return channelAttribute;
      }

      final Optional<String> nameAttribute =
          JsonUtils.getAttributeAsString(publicationServiceObject, ATTRIBUTE_NAME);
      if (nameAttribute.isPresent()) {
        return Optional.of(nameAttribute.get().split(" ")[0]);
      }
    }

    return Optional.empty();
  }

  private void parseRelatedFilms(final ArdFilmDto filmDto, final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_TEASERS)) {
      final JsonElement teasersElement = playerPageObject.get(ELEMENT_TEASERS);
      if (teasersElement.isJsonArray()) {
        for (final JsonElement teasersItemElement : teasersElement.getAsJsonArray()) {
          final JsonObject teasersItemObject = teasersItemElement.getAsJsonObject();
          final Optional<String> id =
              JsonUtils.getAttributeAsString(teasersItemObject, ATTRIBUTE_ID);
          if (id.isPresent()) {
            final String url = String.format(ArdConstants.ITEM_URL, id.get());
            filmDto.addRelatedFilm(new ArdFilmInfoDto(id.get(), url, 0));
          }
        }
      }
    }
  }

  private Film createFilm(
      final Sender sender,
      final String topic,
      final String title,
      @Nullable final String description,
      @Nullable final LocalDateTime date,
      @Nullable final Duration duration,
      final ArdVideoInfoDto videoInfo) {

    final Film film =
        new Film(
            UUID.randomUUID(),
            sender,
            title,
            topic,
            date,
            duration == null ? Duration.ofSeconds(0) : duration);

    Optional.ofNullable(description).ifPresent(film::setBeschreibung);

    
    film.setGeoLocations(GeoLocationGuesser.getGeoLocations(Sender.ARD, videoInfo.getDefaultVideoUrl()));
    
    if (videoInfo.getSubtitleUrl().isPresent()) {
      for (String subtitleUrl : videoInfo.getSubtitleUrl().get()) {
        try {
          film.addSubtitle(new URL(subtitleUrl));
        } catch (final MalformedURLException ex) {
          LOG.error(
              "{}, {}, {} Invalid subtitle url: {}",
              topic,
              title,
              date,
              videoInfo.getSubtitleUrl(),
              ex);
        }
      }
    }
    addUrls(film, videoInfo.getVideoUrls());
    addADUrls(film, videoInfo.getVideoUrlsAD());
    addDGSUrls(film, videoInfo.getVideoUrlsDGS());

    return film;
  }

  private void addUrls(final Film film, final Map<Resolution, String> videoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      final String url = qualitiesEntry.getValue();
      try {
        film.addUrl(qualitiesEntry.getKey(), new FilmUrl(url, crawler.determineFileSizeInKB(url)));
      } catch (final MalformedURLException ex) {
        LOG.error("InvalidUrl: {}", url, ex);
      }
    }
  }
  
  private void addADUrls(final Film film, final Map<Resolution, String> videoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      final String url = qualitiesEntry.getValue();
      try {
        film.addAudioDescription(qualitiesEntry.getKey(), new FilmUrl(url, crawler.determineFileSizeInKB(url)));
      } catch (final MalformedURLException ex) {
        LOG.error("InvalidUrl AD: {}", url, ex);
      }
    }
  }
  
  private void addDGSUrls(final Film film, final Map<Resolution, String> videoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      final String url = qualitiesEntry.getValue();
      try {
        film.addSignLanguage(qualitiesEntry.getKey(), new FilmUrl(url, crawler.determineFileSizeInKB(url)));
      } catch (final MalformedURLException ex) {
        LOG.error("InvalidUrl AD: {}", url, ex);
      }
    }
  }
  
  private Optional<ArdVideoInfoDto> parseVideos(final JsonObject playerPageObject, final String title) {
    ArdVideoInfoDto allVideoUrls = new ArdVideoInfoDto();
    //
    final Optional<Map<Resolution, String>> videoInfoStandard = parseVideoUrls(playerPageObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_STANDARD, MARKER_VIDEO_MP4);
    final Optional<Map<Resolution, String>> videoInfoAdaptive = parseVideoUrls(playerPageObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_STANDARD, MARKER_VIDEO_CATEGORY_MPEG);
    final Optional<Map<Resolution, String>> videoInfoAD = parseVideoUrls(playerPageObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_AD, MARKER_VIDEO_MP4);
    final Optional<Map<Resolution, String>> videoInfoDGS = parseVideoUrls(playerPageObject, MARKER_VIDEO_DGS, MARKER_VIDEO_STANDARD, MARKER_VIDEO_MP4);
    final Optional<Set<String>> subtitles = prepareSubtitleUrl(playerPageObject);
    //
    if (subtitles.isPresent()) {
      allVideoUrls.setSubtitleUrl(subtitles);
    }
    if (videoInfoAD.isPresent()) {
      allVideoUrls.putAllAD(videoInfoAD.get());
    }
    if (videoInfoDGS.isPresent()) {
      allVideoUrls.putAllDGS(videoInfoDGS.get());
    }
    // Sometimes we have DGS & standard
    if (videoInfoStandard.isPresent() && !(videoInfoDGS.isPresent() && title.contains("Gebärdensprache"))) {
      allVideoUrls.putAll(videoInfoStandard.get());
    } else if (videoInfoAdaptive.isPresent() && !(videoInfoDGS.isPresent() && title.contains("Gebärdensprache"))) {
      ArdVideoInfoDto fallbackM3UUrl = new ArdVideoInfoDto();
      fallbackM3UUrl.putAll(videoInfoAdaptive.get());
      Optional<Map<Resolution, String>> fallback = fallbackToM3U(Optional.of(fallbackM3UUrl));
      if (fallback.isPresent()) {
        allVideoUrls.putAll(fallback.get());
      }
    }
    if (allVideoUrls.getVideoUrls().isEmpty() && allVideoUrls.getVideoUrlsAD().isEmpty() && allVideoUrls.getVideoUrlsDGS().isEmpty() ) {
      return Optional.empty();
    }    
    return Optional.of(allVideoUrls);
  }

  private Optional<Map<Resolution, String>> parseVideoUrls(final JsonObject playerPageObject, String streamType, String aduioType, String mimeType) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isEmpty())
      return Optional.empty();
    final Optional<JsonElement> streams = JsonUtils.getElement(mediaCollectionObject.get(), ELEMENT_STREAMS);
    if (streams.isEmpty() || !streams.get().isJsonArray() || (streams.get().getAsJsonArray().size() == 0))
      return Optional.empty();
    //
    Map<Resolution, String> videoInfo = new EnumMap<>(Resolution.class);
    for (JsonElement streamsCategory : streams.get().getAsJsonArray()) {
      final Optional<String> streamKind = JsonUtils.getElementValueAsString(streamsCategory, ATTRIBUTE_KIND);
      final Optional<JsonElement> media = JsonUtils.getElement(streamsCategory, ELEMENT_MEDIA);
      if (media.isEmpty() || !media.get().isJsonArray() || (media.get().getAsJsonArray().size() == 0))
        return Optional.empty();
      if (streamKind.orElse("").equalsIgnoreCase(streamType)) {
        for (JsonElement video : media.get().getAsJsonArray()) {
          Optional<String> mime = JsonUtils.getElementValueAsString(video, ATTRIBUTE_MIME);
          if (mime.isPresent() && mime.get().equalsIgnoreCase(mimeType)) {
            Optional<JsonElement> audios = JsonUtils.getElement(video, ELEMENT_AUDIO);
            if (audios.isPresent() && audios.get().isJsonArray() && audios.get().getAsJsonArray().size() > 0) {
              Optional<String> kind = JsonUtils.getElementValueAsString(audios.get().getAsJsonArray().get(0), ATTRIBUTE_KIND);
              Optional<String> resh = JsonUtils.getElementValueAsString(video, ATTRIBUTE_RESOLUTION_H);
              Optional<String> url = JsonUtils.getElementValueAsString(video, ATTRIBUTE_URL);
              if (url.isPresent() && resh.isPresent() && kind.isPresent() && kind.get().equalsIgnoreCase(aduioType)) {
                Resolution resolution = Resolution.getResolutionFromWidth(Integer.parseInt(resh.get()));
                videoInfo.put(resolution, url.get());
              }
            }
          }
        }
      }
    }
    if (videoInfo.size() == 0) {
      return Optional.empty();
    }
    // TODO: FIX ME
    if (!videoInfo.containsKey(Resolution.NORMAL)) {
      Resolution r = videoInfo.keySet().stream().findFirst().get();
      videoInfo.put(Resolution.NORMAL, videoInfo.get(r));
      videoInfo.remove(r);
    }
    return Optional.of(videoInfo);
  }
}
