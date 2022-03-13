package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class ArteFilmDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final String JSON_ELEMENT_KEY_CATEGORY = "category";
  private static final String JSON_ELEMENT_KEY_SUBCATEGORY = "subcategory";
  private static final String JSON_ELEMENT_KEY_NAME = "name";
  private static final String JSON_ELEMENT_KEY_TITLE = "title";
  private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
  private static final String JSON_ELEMENT_KEY_URL = "url";
  private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";

  private static final String JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1 = "programs";
  private static final String JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_2 = "broadcastProgrammings";
  private static final String JSON_ELEMENT_BROADCAST = "broadcastBeginRounded";
  private static final String JSON_ELEMENT_BROADCASTTYPE = "broadcastType";
  private static final String JSON_ELEMENT_BROADCAST_VIDEORIGHTS_BEGIN = "videoRightsBegin";
  private static final String JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN = "catchupRightsBegin";
  private static final String JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_END = "catchupRightsEnd";
  private static final String BROADCASTTTYPE_FIRST = "FIRST_BROADCAST";
  private static final String BROADCASTTTYPE_MINOR_RE = "MINOR_REBROADCAST";
  private static final String BROADCASTTTYPE_MAJOR_RE = "MAJOR_REBROADCAST";
  private static final String ATTRIBUTE_DURATION = "durationSeconds";

  private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

  private static final Logger LOG = LogManager.getLogger(ArteFilmDeserializer.class);

  private static final DateTimeFormatter DATE_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

  private final Sender sender;
  private LocalDateTime today;

  public ArteFilmDeserializer(final Sender sender, final LocalDateTime today) {
    this.sender = sender;
    this.today = today;
  }

  @Override
  public Optional<Film> deserialize(
          JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    if (aJsonElement.isJsonObject()
            && aJsonElement
            .getAsJsonObject()
            .get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1)
            .getAsJsonArray()
            .size()
            > 0) {

      JsonObject programElement =
              aJsonElement
                      .getAsJsonObject()
                      .get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1)
                      .getAsJsonArray()
                      .get(0)
                      .getAsJsonObject();

      String titel = getTitle(programElement);
      String thema = getSubject(programElement);
      String beschreibung = getElementValue(programElement, JSON_ELEMENT_KEY_SHORT_DESCRIPTION);
      String urlWeb = getElementValue(programElement, JSON_ELEMENT_KEY_URL);
      Duration duration = parseDuration(programElement);

      Optional<LocalDateTime> date = parseDate(programElement);

      final Film film =
              new Film(UUID.randomUUID(), sender, titel, thema, date.orElse(null), duration);
      film.setBeschreibung(beschreibung);

      try {
        film.setWebsite(new URL(urlWeb));
      } catch (MalformedURLException e) {
        LOG.error("Invalid url: " + urlWeb);
      }

      GeoLocations geoLocation = getGeoLocation(programElement);
      film.addGeolocation(geoLocation);

      return Optional.of(film);
    }

    return Optional.empty();
  }

  private Duration parseDuration(JsonObject programElement) {
    long durationValue = 0;

    if (programElement.has(ATTRIBUTE_DURATION)) {
      durationValue = programElement.get(ATTRIBUTE_DURATION).getAsLong();
    }

    return Duration.ofSeconds(durationValue);
  }

  private static String getSubject(JsonObject programObject) {
    String category = "";
    String subcategory = "";
    String subject;

    JsonElement catElement = programObject.get(JSON_ELEMENT_KEY_CATEGORY);
    if (!catElement.isJsonNull()) {
      JsonObject catObject = catElement.getAsJsonObject();
      category = catObject != null ? getElementValue(catObject, JSON_ELEMENT_KEY_NAME) : "";
    }

    JsonElement subcatElement = programObject.get(JSON_ELEMENT_KEY_SUBCATEGORY);
    if (!subcatElement.isJsonNull()) {
      JsonObject subcatObject = subcatElement.getAsJsonObject();
      subcategory =
              subcatObject != null ? getElementValue(subcatObject, JSON_ELEMENT_KEY_NAME) : "";
    }

    if (!category.equals(subcategory) && !subcategory.isEmpty()) {
      subject = category + " - " + subcategory;
    } else {
      subject = category;
    }

    return subject;
  }

  private static String getTitle(JsonObject programObject) {
    String title = getElementValue(programObject, JSON_ELEMENT_KEY_TITLE);
    String subtitle = getElementValue(programObject, JSON_ELEMENT_KEY_SUBTITLE);

    if (!title.equals(subtitle) && !subtitle.isEmpty()) {
      title = title + " - " + subtitle;
    }

    return title;
  }

  private static String getElementValue(JsonObject jsonObject, String elementName) {
    return !jsonObject.get(elementName).isJsonNull()
            ? jsonObject.get(elementName).getAsString()
            : "";
  }

  private GeoLocations getGeoLocation(JsonObject programElement) {
    GeoLocations geo = GeoLocations.GEO_NONE;

    if (programElement.has("geoblocking")) {
      JsonElement geoElement = programElement.get("geoblocking");
      if (!geoElement.isJsonNull()) {
        JsonObject geoObject = geoElement.getAsJsonObject();
        if (!geoObject.isJsonNull() && geoObject.has("code")) {
          String code = geoObject.get("code").getAsString();
          switch (code) {
            case "DE_FR":
              geo = GeoLocations.GEO_DE_FR;
              break;
            case "EUR_DE_FR":
              geo = GeoLocations.GEO_DE_AT_CH_FR;
              break;
            case "SAT":
              geo = GeoLocations.GEO_DE_AT_CH_EU;
              break;
            case "ALL":
              geo = GeoLocations.GEO_NONE;
              break;
            default:
              LOG.debug("New ARTE GeoLocation: " + code);
          }
        }
      }
    }

    return geo;
  }

  private Optional<LocalDateTime> parseDate(JsonObject programElement) {
    JsonArray broadcastArray =
            programElement.get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_2).getAsJsonArray();

    String value;

    if (broadcastArray.size() > 0) {
      value = getBroadcastDate(broadcastArray);
    } else {
      // keine Ausstrahlungen verfügbar => catchupRightsBegin verwenden
      // wenn es die auch nicht gibt => videoRightsBegin verwenden
      value = getElementValue(programElement, JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN);
      if (value.isEmpty()) {
        value = getElementValue(programElement, JSON_ELEMENT_BROADCAST_VIDEORIGHTS_BEGIN);
      }
    }

    if (value.isEmpty()) {
      return Optional.empty();
    }

    LocalDateTime local = LocalDateTime.parse(value, DATE_FORMATTER);
    ZonedDateTime zoned = local.atZone(ZONE_ID);
    int hoursToAdd = zoned.getOffset().getTotalSeconds() / 3600;
    return Optional.of(local.plusHours(hoursToAdd));
  }

  /**
   * ermittelt Ausstrahlungsdatum aus der Liste der Ausstrahlungen
   */
  private String getBroadcastDate(JsonArray broadcastArray) {
    String broadcastDate = "";
    String broadcastBeginFirst = "";
    String broadcastBeginMajor = "";
    String broadcastBeginMinor = "";

    // nach Priorität der BroadcastTypen den relevanten Eintrag suchen
    // FIRST_BROADCAST => MAJOR_REBROADCAST => MINOR_REBROADCAST
    // dabei die "aktuellste" Ausstrahlung verwenden
    for (int i = 0; i < broadcastArray.size(); i++) {
      JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();

      if (broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE)
              && broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
        String value = this.getBroadcastDateConsideringCatchupRights(broadcastObject);

        if (!value.isEmpty()) {
          String type = broadcastObject.get(JSON_ELEMENT_BROADCASTTYPE).getAsString();
          switch (type) {
            case BROADCASTTTYPE_FIRST:
              broadcastBeginFirst = value;
              break;
            case BROADCASTTTYPE_MAJOR_RE:
              broadcastBeginMajor = value;
              break;
            case BROADCASTTTYPE_MINOR_RE:
              broadcastBeginMinor = value;
              break;
            default:
              LOG.debug("New broadcasttype: " + type);
          }
        }
      }
    }

    if (!broadcastBeginFirst.isEmpty()) {
      broadcastDate = broadcastBeginFirst;
    } else if (!broadcastBeginMajor.isEmpty()) {
      broadcastDate = broadcastBeginMajor;
    } else if (!broadcastBeginMinor.isEmpty()) {
      broadcastDate = broadcastBeginMinor;
    }

    // wenn kein Ausstrahlungsdatum vorhanden, dann die erste Ausstrahlung nehmen
    // egal, wann die CatchupRights liegen, damit ein "sinnvolles" Datum vorhanden ist
    if (broadcastDate.isEmpty()) {
      broadcastDate = getBroadcastDateIgnoringCatchupRights(broadcastArray, BROADCASTTTYPE_FIRST);
    }
    // wenn immer noch leer, dann die Major-Ausstrahlung verwenden
    if (broadcastDate.isEmpty()) {
      broadcastDate =
              getBroadcastDateIgnoringCatchupRights(broadcastArray, BROADCASTTTYPE_MAJOR_RE);
    }

    return broadcastDate;
  }

  /**
   * Liefert den Beginn der Ausstrahlung, wenn - heute im Zeitraum von CatchUpRights liegt - oder
   * heute vor dem Zeitraum liegt - oder CatchUpRights nicht gesetzt ist
   *
   * @return der Beginn der Ausstrahlung oder ""
   */
  private String getBroadcastDateConsideringCatchupRights(JsonObject broadcastObject) {
    String broadcastDate = "";

    JsonElement elementBegin = broadcastObject.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN);
    JsonElement elementEnd = broadcastObject.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_END);

    if (!elementBegin.isJsonNull() && !elementEnd.isJsonNull()) {
      String begin = elementBegin.getAsString();
      String end = elementEnd.getAsString();

      LocalDateTime beginDate = LocalDateTime.parse(begin, DATE_FORMATTER);
      LocalDateTime endDate = LocalDateTime.parse(end, DATE_FORMATTER);

      if (today.isAfter(beginDate) && today.isBefore(endDate) || today.isBefore(beginDate)) {
        // wenn das heutige Datum zwischen begin und end liegt,
        // dann ist es die aktuelle Ausstrahlung
        final JsonElement elementActual = broadcastObject.get(JSON_ELEMENT_BROADCAST);
        if (elementActual != null && !elementActual.isJsonNull()) {
          broadcastDate = elementActual.getAsString();
        }
      }
    } else if (!broadcastObject.get(JSON_ELEMENT_BROADCAST).isJsonNull()) {
      broadcastDate = broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString();
    }
    return broadcastDate;
  }

  /**
   * liefert die erste Ausstrahlung des Typs ohne Berücksichtigung der CatchupRights
   */
  private static String getBroadcastDateIgnoringCatchupRights(
          JsonArray broadcastArray, String broadcastType) {
    String broadcastDate = "";

    for (int i = 0; i < broadcastArray.size(); i++) {
      JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();

      if (broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE)
              && broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
        String type = broadcastObject.get(JSON_ELEMENT_BROADCASTTYPE).getAsString();

        if (type.equals(broadcastType) &&
                !broadcastObject.get(JSON_ELEMENT_BROADCAST).isJsonNull()) {
          broadcastDate = (broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString());
        }
      }
    }

    return broadcastDate;
  }
}
