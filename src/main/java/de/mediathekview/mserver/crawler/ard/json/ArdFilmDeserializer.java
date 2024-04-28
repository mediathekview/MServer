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
  private static final String ELEMENT_SUBTITLES = "subtitles";
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
    return Optional.of(JsonUtils.getElement(itemObject, ELEMENT_MEDIA_COLLECTION, ELEMENT_EMBEDDED).get().getAsJsonObject());  
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
    Set<String> urls = new HashSet<String>();
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
    final Sender sender = determinePartner(itemObject);
    final Optional<ArdVideoInfoDto> videoInfoStandard = parseVideoUrls(itemObject, "standard", "video/mp4");
    final Optional<ArdVideoInfoDto> videoInfoAdaptive = parseVideoUrls(itemObject, "standard", "application/vnd.apple.mpegurl");
    final Optional<ArdVideoInfoDto> videoInfoAD = parseVideoUrls(itemObject, "standard", "audio-description");
    final Optional<ArdVideoInfoDto> videoInfoDGS = parseVideoUrls(itemObject, "standard", "sign-language");
    // FUNK provides adaptive only
    Optional<ArdVideoInfoDto> videoInfo = videoInfoStandard;
    if (videoInfoStandard.isEmpty() && videoInfoAD.isEmpty()) {
      videoInfo = fallbackToM3U(videoInfoAdaptive);
    }
    
    
    if (topic.isPresent()
        && title.isPresent()
        && videoInfo.isPresent()
        && videoInfo.get().getVideoUrls().size() > 0) {
      videoInfo.get().setSubtitleUrl(prepareSubtitleUrl(jsonElement));
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
      films.add(filmDto);
      //
      if (topic.isPresent()
          && title.isPresent()
          && videoInfoAD.isPresent()
          && videoInfoAD.get().getVideoUrls().size() > 0) {
        videoInfoAD.get().setSubtitleUrl(prepareSubtitleUrl(jsonElement));
        // add film to ARD
        final ArdFilmDto filmDtoAD =
            new ArdFilmDto(
                createFilm(
                    sender,
                    topic.get(),
                    title.get(),
                    description.orElse(null),
                    date.orElse(null),
                    duration.orElse(null),
                    videoInfo.get()));
        films.add(filmDtoAD);
        //
        if (widgets.size() > 1) {
          parseRelatedFilms(filmDto, widgets.get(1).getAsJsonObject());
        }
      }
    } else {
      LOG.debug("No title, topic or video found for {} {} {} ", title, topic, sender);
    }

    return films;
  }
  
  private Optional<ArdVideoInfoDto> fallbackToM3U(Optional<ArdVideoInfoDto> m3u) {
    // FUNK provides adaptive only
    if (m3u.isPresent() && m3u.get().containsResolution(Resolution.NORMAL)) {
      Map<Resolution, URL> resolutionUrlMapFromM3U;
      try {
        resolutionUrlMapFromM3U = videoDeserializer.loadM3U8(new URL(m3u.get().getVideoUrls().get(Resolution.NORMAL)));
        if (resolutionUrlMapFromM3U.size() > 0) {
          ArdVideoInfoDto newVideoInfo = new ArdVideoInfoDto();
          resolutionUrlMapFromM3U.forEach((key, value) -> newVideoInfo.put(key, value.toString()));
          return Optional.of(newVideoInfo);
        }
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }

  private Optional<String> parseTitle(final JsonObject playerPageObject) {
    final Optional<String> title =
        JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    return title.map(nonNullTitle -> nonNullTitle.replace("Hörfassung", "Audiodeskription"));
  }
  
  
  private Sender determinePartner(JsonObject playerPageObject) {
    final Optional<String> partner = parsePartner(playerPageObject);
    final Sender sender;
    // If partner is present and an existing sender set it. Like for RBB
    if (partner.isPresent()) {
      final Optional<Sender> additionalSender = Sender.getSenderByName(partner.get());
      sender = additionalSender.orElse(Sender.ARD);
    } else {
      sender = Sender.ARD;
    }
    return sender;
  }

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

    film.setGeoLocations(
        GeoLocationGuesser.getGeoLocations(Sender.ARD, videoInfo.getDefaultVideoUrl()));
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

  private Optional<ArdVideoInfoDto> parseVideoUrls(final JsonObject playerPageObject, String videoType, String mimeType) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isEmpty())
      return Optional.empty();
    final Optional<JsonElement> streams = JsonUtils.getElement(mediaCollectionObject.get(), ELEMENT_STREAMS);
    if (streams.isEmpty() || !streams.get().isJsonArray() || (streams.get().getAsJsonArray().size() == 0))
      return Optional.empty();
    final Optional<JsonElement> media = JsonUtils.getElement(streams.get().getAsJsonArray().get(0), ELEMENT_MEDIA);
    if (media.isEmpty() || !media.get().isJsonArray() || (media.get().getAsJsonArray().size() == 0))
      return Optional.empty();
    ArdVideoInfoDto videoInfo = new ArdVideoInfoDto();
    for (JsonElement video : media.get().getAsJsonArray()) {
      Optional<String> mime = JsonUtils.getElementValueAsString(video, ATTRIBUTE_MIME);
      if (mime.isPresent() && mime.get().equalsIgnoreCase(mimeType)) {
        Optional<JsonElement> audios = JsonUtils.getElement(video, ELEMENT_AUDIO);
        if (audios.isPresent() && audios.get().isJsonArray() && audios.get().getAsJsonArray().size() > 0) {
          Optional<String> kind = JsonUtils.getElementValueAsString(audios.get().getAsJsonArray().get(0), ATTRIBUTE_KIND);
          Optional<String> res_h = JsonUtils.getElementValueAsString(video, ATTRIBUTE_RESOLUTION_H);
          Optional<String> url = JsonUtils.getElementValueAsString(video, ATTRIBUTE_URL);
          if (url.isPresent() && res_h.isPresent() && kind.isPresent() && kind.get().equalsIgnoreCase(videoType)) {
            Resolution resolution = Resolution.getResolutionFromWidth(Integer.parseInt(res_h.get()));
            videoInfo.put(resolution, url.get());
          }
        }
      }
    }
    if (videoInfo.getVideoUrls().size() > 0) {
      return Optional.of(videoInfo);
    } else {
      return Optional.empty();
    }
  }
}
