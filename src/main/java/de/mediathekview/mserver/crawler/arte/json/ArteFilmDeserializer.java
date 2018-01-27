package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import mServer.crawler.CrawlerTool;

public class ArteFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  private static final Type OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<ArteVideoDetailDTO>>() {}.getType();
  private static final String ELEMENT_HREF = "href";
  private static final String ELEMENT_VIDEO_STREAMS = "videoStreams";
  private static final String ELEMENT_SHORT_DESCRIPTION = "shortDescription";
  private static final String ELEMENT_GEOBLOCKING_ZONE = "geoblockingZone";
  private static final String ELEMENT_VIDEO_RIGHTS_BEGIN = "videoRightsBegin";
  private static final String ELEMENT_PREVIEW_RIGHTS_BEGIN = "previewRightsBegin";
  private static final String ELEMENT_CREATION_DATE = "creationDate";
  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_DURATION_SECONDS = "durationSeconds";
  private static final String ELEMENT_TITLE = "title";
  private static final String ELEMENT_SUBTITLE = "subtitle";
  private final AbstractCrawler crawler;
  private final Client client;

  public ArteFilmDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }

  @Override
  public Optional<Film> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    if (JsonUtils.hasElements(aJsonElement, Optional.of(crawler), ELEMENT_SUBTITLE, ELEMENT_TITLE,
        ELEMENT_DURATION_SECONDS, ELEMENT_LINKS)) {
      final JsonObject baseObject = aJsonElement.getAsJsonObject();
      final String titel = baseObject.get(ELEMENT_SUBTITLE).getAsString();// subtitle
      final String thema = baseObject.get(ELEMENT_TITLE).getAsString();// title

      // creationDate alternativ: previewRightsBegin videoRightsBegin
      final Optional<LocalDateTime> time = gatherDateTime(baseObject, ELEMENT_CREATION_DATE,
          ELEMENT_PREVIEW_RIGHTS_BEGIN, ELEMENT_VIDEO_RIGHTS_BEGIN);
      if (time.isPresent()) {

        // durationSeconds
        final Duration dauer =
            Duration.of(baseObject.get(ELEMENT_DURATION_SECONDS).getAsLong(), ChronoUnit.SECONDS);

        final Film film =
            new Film(UUID.randomUUID(), crawler.getSender(), titel, thema, time.get(), dauer);

        // geo: geoblockingZone
        if (baseObject.has(ELEMENT_GEOBLOCKING_ZONE)) {
          film.addGeolocation(GeoLocations
              .getFromDescription(baseObject.get(ELEMENT_GEOBLOCKING_ZONE).getAsString()));
        }

        // beschreibung: shortDescription
        if (baseObject.has(ELEMENT_SHORT_DESCRIPTION)) {
          film.setBeschreibung(baseObject.get(ELEMENT_SHORT_DESCRIPTION).getAsString());
        }

        // video streams: links -> videoStreams -> href
        if (JsonUtils.hasElements(baseObject, ELEMENT_VIDEO_STREAMS)) {
          final JsonObject videoStreams = baseObject.get(ELEMENT_VIDEO_STREAMS).getAsJsonObject();
          if (JsonUtils.hasElements(videoStreams, ELEMENT_HREF)) {
            final Optional<ArteVideoDetailDTO> videoDetails =
                gatherVideoDetails(videoStreams.get(ELEMENT_HREF).getAsString());
            if (videoDetails.isPresent()) {
              videoDetails.get().getUrls().entrySet()
                  .forEach(e -> film.addUrl(e.getKey(), CrawlerTool.uriToFilmUrl(e.getValue())));
              return Optional.of(film);
            }
          }
        }

        crawler.printErrorMessage();
        crawler.incrementAndGetErrorCount();
      } else {
        crawler.printMissingElementErrorMessage(ELEMENT_CREATION_DATE);
      }
    }
    return Optional.empty();
  }

  private Optional<LocalDateTime> gatherDateTime(final JsonObject aBaseObject,
      final String... aElementIds) {
    for (final String elementId : aElementIds) {
      if (aBaseObject.has(elementId)) {
        return Optional.of(toDateTime(aBaseObject.get(elementId).getAsString()));
      }
    }
    return Optional.empty();
  }

  private Optional<ArteVideoDetailDTO> gatherVideoDetails(final String aVideoDetailUrl) {
    final WebTarget target = client.target(aVideoDetailUrl);
    final Response response = target.request().get();
    final String jsonOutput = response.readEntity(String.class);

    final Gson gson =
        new GsonBuilder().registerTypeAdapter(OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN,
            new ArteVideDetailsDeserializer(crawler)).create();
    return gson.fromJson(jsonOutput, OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN);
  }

  private LocalDateTime toDateTime(final String aDateTimeTest) {
    final ZonedDateTime inputDateTime =
        ZonedDateTime.parse(aDateTimeTest, DateTimeFormatter.ISO_INSTANT);
    return inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
  }

}
