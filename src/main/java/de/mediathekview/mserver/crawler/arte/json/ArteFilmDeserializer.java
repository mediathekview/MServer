package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
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
  class TitleThemaDTO {
    private String title;
    private String thema;

    public TitleThemaDTO(final String aTitle, final String aThema) {
      title = aTitle;
      thema = aThema;
    }

    public String getThema() {
      return thema;
    }

    public String getTitle() {
      return title;
    }

    public void setThema(final String aThema) {
      thema = aThema;
    }

    public void setTitle(final String aTitle) {
      title = aTitle;
    }


  }

  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final String ELEMENT_CATEGORY = "category";
  private static final String ELEMENT_CREATION_DATE = "creationDate";
  private static final String ELEMENT_DURATION_SECONDS = "durationSeconds";
  private static final String ELEMENT_GEOBLOCKING_ZONE = "geoblockingZone";
  private static final String ELEMENT_HREF = "href";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_NAME = "name";
  private static final String ELEMENT_PREVIEW_RIGHTS_BEGIN = "previewRightsBegin";
  private static final String ELEMENT_SHORT_DESCRIPTION = "shortDescription";
  private static final String ELEMENT_SUBCATEGORY = "subcategory";
  private static final String ELEMENT_SUBTITLE = "subtitle";
  private static final String ELEMENT_TITLE = "title";
  private static final String ELEMENT_VIDEO_RIGHTS_BEGIN = "videoRightsBegin";
  private static final String ELEMENT_VIDEO_STREAMS = "videoStreams";
  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";
  private static final Type OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<ArteVideoDetailDTO>>() {}.getType();
  private final Client client;

  private final AbstractCrawler crawler;
  private final String authKey;

  public ArteFilmDeserializer(final AbstractCrawler aCrawler, final String aAuthKey) {
    crawler = aCrawler;
    authKey = aAuthKey;
    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }

  @Override
  public Optional<Film> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    if (JsonUtils.hasElements(aJsonElement, Optional.of(crawler), ELEMENT_TITLE,
        ELEMENT_DURATION_SECONDS, ELEMENT_LINKS)) {
      final JsonObject baseObject = aJsonElement.getAsJsonObject();

      final Optional<TitleThemaDTO> titleThema = getTitleThema(baseObject);
      if (titleThema.isPresent()) {

        // creationDate alternativ: previewRightsBegin videoRightsBegin
        final Optional<LocalDateTime> time = gatherDateTime(baseObject, ELEMENT_CREATION_DATE,
            ELEMENT_PREVIEW_RIGHTS_BEGIN, ELEMENT_VIDEO_RIGHTS_BEGIN);
        if (time.isPresent()) {

          // durationSeconds
          final Duration dauer =
              Duration.of(baseObject.get(ELEMENT_DURATION_SECONDS).getAsLong(), ChronoUnit.SECONDS);

          final Film film = new Film(UUID.randomUUID(), crawler.getSender(),
              titleThema.get().getTitle(), titleThema.get().getThema(), time.get(), dauer);

          // geo: geoblockingZone
          if (baseObject.has(ELEMENT_GEOBLOCKING_ZONE)) {
            film.addGeolocation(GeoLocations
                .getFromDescription(baseObject.get(ELEMENT_GEOBLOCKING_ZONE).getAsString()));
          }

          // beschreibung: shortDescription
          if (baseObject.has(ELEMENT_SHORT_DESCRIPTION)
              && !baseObject.get(ELEMENT_SHORT_DESCRIPTION).isJsonNull()) {
            film.setBeschreibung(baseObject.get(ELEMENT_SHORT_DESCRIPTION).getAsString());
          }

          // video streams: links -> videoStreams -> href
          if (JsonUtils.checkTreePath(baseObject, Optional.of(crawler), ELEMENT_LINKS,
              ELEMENT_VIDEO_STREAMS)) {
            final JsonObject videoStreams = baseObject.get(ELEMENT_LINKS).getAsJsonObject()
                .get(ELEMENT_VIDEO_STREAMS).getAsJsonObject();
            if (JsonUtils.hasElements(videoStreams, ELEMENT_HREF)) {
              final String videoDetailsLink = videoStreams.get(ELEMENT_HREF).getAsString();
              if (videoDetailsLink.contains("liveVideoStreams")) {
                // TODO live videso
              } else {
                final Optional<ArteVideoDetailDTO> videoDetails =
                    gatherVideoDetails(videoDetailsLink);
                if (videoDetails.isPresent()) {
                  videoDetails.get().getUrls().entrySet().forEach(
                      e -> film.addUrl(e.getKey(), CrawlerTool.uriToFilmUrl(e.getValue())));
                  return Optional.of(film);
                }
              }
            }
          }

          crawler.printErrorMessage();
          crawler.incrementAndGetErrorCount();
        } else {
          crawler.printMissingElementErrorMessage(ELEMENT_CREATION_DATE);
        }
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
    final String videoDetailUrlWithProfileFilter = aVideoDetailUrl + "&profileAmm=AMM-PTWEB";
    final Builder request = client.target(videoDetailUrlWithProfileFilter).request()
        .header(HEADER_AUTHORIZATION, authKey);

    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();
    final String jsonOutput = response.readEntity(String.class);

    final Gson gson =
        new GsonBuilder().registerTypeAdapter(OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN,
            new ArteVideDetailsDeserializer(crawler)).create();
    return gson.fromJson(jsonOutput, OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN);
  }

  private Optional<TitleThemaDTO> getTitleThema(final JsonObject baseObject) {
    if (JsonUtils.hasElements(baseObject, ELEMENT_SUBTITLE)) {
      return Optional.of(new TitleThemaDTO(baseObject.get(ELEMENT_SUBTITLE).getAsString(),
          baseObject.get(ELEMENT_TITLE).getAsString()));
    } else if (JsonUtils.checkTreePath(baseObject, Optional.empty(), ELEMENT_SUBCATEGORY,
        ELEMENT_NAME)) {
      return Optional.of(new TitleThemaDTO(baseObject.get(ELEMENT_TITLE).getAsString(),
          baseObject.get(ELEMENT_SUBCATEGORY).getAsJsonObject().get(ELEMENT_NAME).getAsString()));
    } else if (JsonUtils.checkTreePath(baseObject, Optional.empty(), ELEMENT_CATEGORY,
        ELEMENT_NAME)) {
      return Optional.of(new TitleThemaDTO(baseObject.get(ELEMENT_TITLE).getAsString(),
          baseObject.get(ELEMENT_CATEGORY).getAsJsonObject().get(ELEMENT_NAME).getAsString()));
    } else {
      crawler.printMissingElementErrorMessage(ELEMENT_SUBTITLE);
      crawler.printMissingElementErrorMessage(ELEMENT_SUBCATEGORY);
      crawler.printMissingElementErrorMessage(ELEMENT_CATEGORY);
      return Optional.empty();
    }
  }

  private LocalDateTime toDateTime(final String aDateTimeTest) {
    final ZonedDateTime inputDateTime = ZonedDateTime.parse(aDateTimeTest);
    return inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
  }

}
