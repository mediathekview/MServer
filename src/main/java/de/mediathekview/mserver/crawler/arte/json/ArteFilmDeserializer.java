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
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import mServer.crawler.CrawlerTool;

public class ArteFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  private static final String ELEMENT_CATEGORY = "category";
  private static final String ELEMENT_CREATION_DATE = "creationDate";
  private static final String ELEMENT_DURATION = "duration";
  private static final String ELEMENT_DURATION_SECONDS = "durationSeconds";
  private static final String ELEMENT_GEOBLOCKING_ZONE = "geoblockingZone";
  private static final String ELEMENT_NAME = "name";
  private static final String ELEMENT_PREVIEW_RIGHTS_BEGIN = "previewRightsBegin";
  private static final String ELEMENT_PROGRAM_ID = "programId";
  private static final String ELEMENT_SHORT_DESCRIPTION = "shortDescription";
  private static final String ELEMENT_SUBCATEGORY = "subcategory";
  private static final String ELEMENT_SUBTITLE = "subtitle";
  private static final String ELEMENT_TITLE = "title";
  private static final String ELEMENT_VIDEO_RIGHTS_BEGIN = "videoRightsBegin";
  private static final String ENCODING_GZIP = "gzip";
  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final Type OPTIONAL_ARTE_VIDEO_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<ArteVideoDetailDTO>>() {}.getType();
  /**
   * <ul>
   * <li>1. Parameter Program-ID</li>
   * <li>2. Language-Code upper case</li>
   * <li>3. Language-Code lower case</li>
   * </ul>
   */
  private static final String VIDEO_STREAM_URL_PATTERN =
      "https://api.arte.tv/api/opa/v3/videoStreams?programId=%s&channel=%s&language=%s&limit=100&profileAmm=AMM-PTWEB";
  private final String authKey;

  private final Client client;
  private final AbstractCrawler crawler;

  private final ArteLanguage language;
  private final Optional<String> subcategoryName;

  public ArteFilmDeserializer(final AbstractCrawler aCrawler, final String aAuthKey,
      final ArteLanguage aLanguage, final Optional<String> aSubcategoryName) {
    crawler = aCrawler;
    authKey = aAuthKey;
    language = aLanguage;
    subcategoryName = aSubcategoryName;
    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }

  @Override
  public Optional<Film> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    if (JsonUtils.hasElements(aJsonElement, Optional.of(crawler), ELEMENT_TITLE, ELEMENT_PROGRAM_ID)
        && (JsonUtils.hasElements(aJsonElement, ELEMENT_DURATION_SECONDS)
            || JsonUtils.hasElements(aJsonElement, ELEMENT_DURATION))) {
      final JsonObject baseObject = aJsonElement.getAsJsonObject();

      final Optional<TitleThemaDTO> titleThema = getTitleThema(baseObject);
      if (titleThema.isPresent()) {

        final String videoDetailsLink = String.format(VIDEO_STREAM_URL_PATTERN,
            baseObject.get(ELEMENT_PROGRAM_ID).getAsString(), language.getLanguageCode(),
            language.getLanguageCode().toLowerCase());

        final Optional<ArteVideoDetailDTO> videoDetails = gatherVideoDetails(videoDetailsLink);
        if (videoDetails.isPresent()) {
          return createFilm(baseObject, titleThema, videoDetails);
        }
      }
    }
    return Optional.empty();
  }

  private Film addVideoStreams(final ArteVideoDetailDTO videoDetails, final Film film) {
    videoDetails.getUrls().entrySet().forEach(videoDetailUrl -> film.addUrl(videoDetailUrl.getKey(),
        CrawlerTool.uriToFilmUrl(videoDetailUrl.getValue())));
    return film;

  }

  private Optional<Film> createFilm(final JsonObject baseObject,
      final Optional<TitleThemaDTO> titleThema, final Optional<ArteVideoDetailDTO> videoDetails) {
    // creationDate alternativ: previewRightsBegin videoRightsBegin
    final Optional<LocalDateTime> time =
        gatherDateTime(baseObject, videoDetails.get().getCreationDate(), ELEMENT_CREATION_DATE,
            ELEMENT_PREVIEW_RIGHTS_BEGIN, ELEMENT_VIDEO_RIGHTS_BEGIN);
    if (time.isPresent()) {

      // durationSeconds
      final Duration dauer = gatherDauer(baseObject);

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
      return Optional.of(addVideoStreams(videoDetails.get(), film));
    } else {
      crawler.printMissingElementErrorMessage(ELEMENT_CREATION_DATE);
    }
    return Optional.empty();
  }

  private Optional<LocalDateTime> gatherDateTime(final JsonObject aBaseObject,
      final Optional<String> creationDateOfVideoDetails, final String... aElementIds) {
    for (final String elementId : aElementIds) {
      if (aBaseObject.has(elementId)) {
        return Optional.of(toDateTime(aBaseObject.get(elementId).getAsString()));
      }
    }
    if (creationDateOfVideoDetails.isPresent()) {
      return Optional.of(toDateTime(creationDateOfVideoDetails.get()));
    }
    return Optional.empty();
  }

  private Duration gatherDauer(final JsonObject baseObject) {
    Duration dauer;
    if (baseObject.has(ELEMENT_DURATION_SECONDS)
        && !baseObject.get(ELEMENT_DURATION_SECONDS).isJsonNull()) {
      dauer = Duration.of(baseObject.get(ELEMENT_DURATION_SECONDS).getAsLong(), ChronoUnit.SECONDS);
    } else {
      dauer = Duration.of(baseObject.get(ELEMENT_DURATION).getAsLong(), ChronoUnit.SECONDS);
    }
    return dauer;
  }

  private Optional<ArteVideoDetailDTO> gatherVideoDetails(final String aVideoDetailUrl) {
    final String videoDetailUrlWithProfileFilter = aVideoDetailUrl;
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
    } else if (subcategoryName.isPresent()) {
      return Optional.of(
          new TitleThemaDTO(subcategoryName.get(), baseObject.get(ELEMENT_TITLE).getAsString()));
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
