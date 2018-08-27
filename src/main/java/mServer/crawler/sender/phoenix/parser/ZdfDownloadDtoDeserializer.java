package mServer.crawler.sender.phoenix.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Optional;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.phoenix.DownloadDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A JSON deserializer to gather the needed information for a
 * {@link DownloadDto}.
 */
public class ZdfDownloadDtoDeserializer implements JsonDeserializer<Optional<DownloadDto>> {

  private static final String ZDF_QUALITY_VERYHIGH = "veryhigh";
  private static final String ZDF_QUALITY_HIGH = "high";
  private static final String ZDF_QUALITY_MED = "med";
  private static final String ZDF_QUALITY_LOW = "low";
  private static final Logger LOG = LogManager.getLogger(ZdfDownloadDtoDeserializer.class);
  private static final String JSON_ELEMENT_ATTRIBUTES = "attributes";
  private static final String JSON_ELEMENT_AUDIO = "audio";
  private static final String JSON_ELEMENT_CAPTIONS = "captions";
  private static final String JSON_ELEMENT_FORMITAET = "formitaeten";
  private static final String JSON_ELEMENT_GEOLOCATION = "geoLocation";
  private static final String JSON_ELEMENT_HD = "hd";
  private static final String JSON_ELEMENT_MIMETYPE = "mimeType";
  private static final String JSON_ELEMENT_PRIORITYLIST = "priorityList";
  private static final String JSON_ELEMENT_QUALITY = "quality";
  private static final String JSON_ELEMENT_TRACKS = "tracks";
  private static final String JSON_ELEMENT_URI = "uri";

  private static final String JSON_PROPERTY_VALUE = "value";

  private static final String RELEVANT_MIME_TYPE = "video/mp4";
  private static final String RELEVANT_SUBTITLE_TYPE = ".xml";
  private static final String JSON_ELEMENT_QUALITIES = "qualities";

  @Override
  public Optional<DownloadDto> deserialize(final JsonElement aJsonElement, final Type aTypeOfT,
          final JsonDeserializationContext aJsonDeserializationContext) {
    final DownloadDto dto = new DownloadDto();
    try {
      final JsonObject rootNode = aJsonElement.getAsJsonObject();

      parseVideoUrls(dto, rootNode);
      parseSubtitle(dto, rootNode);
      parseGeoLocation(dto, rootNode);

      return Optional.of(dto);
    } catch (final UnsupportedOperationException unsupportedOperationException) {
      // This will happen when a element is JsonNull.
      LOG.error("ZDF: A needed JSON element is JsonNull.", unsupportedOperationException);
    }

    return Optional.empty();
  }

  private void parseFormitaet(final DownloadDto dto, final JsonElement formitaet) {
    // only mp4-videos are relevant
    final JsonElement mimeType = formitaet.getAsJsonObject().get(JSON_ELEMENT_MIMETYPE);
    if (mimeType != null && mimeType.getAsString().equalsIgnoreCase(RELEVANT_MIME_TYPE)) {

      // array Resolution
      final JsonArray qualityList
              = formitaet.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_QUALITIES);
      for (final JsonElement quality : qualityList) {
        String uri = null;

        final Qualities qualityValue = parseVideoQuality(quality.getAsJsonObject());

        // subelement audio
        final JsonElement audio = quality.getAsJsonObject().get(JSON_ELEMENT_AUDIO);
        if (audio != null) {

          // array tracks
          final JsonArray tracks = audio.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_TRACKS);

          final JsonObject track = tracks.get(0).getAsJsonObject();
          uri = track.get(JSON_ELEMENT_URI).getAsString();
        }

        if (qualityValue != null && uri != null) {
          dto.addUrl(qualityValue, uri);
        }
      }
    }
  }

  private void parseGeoLocation(final DownloadDto dto, final JsonObject rootNode) {
    final JsonElement attributes = rootNode.get(JSON_ELEMENT_ATTRIBUTES);
    if (attributes != null) {
      final JsonElement geoLocation = attributes.getAsJsonObject().get(JSON_ELEMENT_GEOLOCATION);
      if (geoLocation != null) {
        final JsonElement geoValue = geoLocation.getAsJsonObject().get(JSON_PROPERTY_VALUE);
        if (geoValue != null && !geoValue.getAsString().toUpperCase().equals("NONE")) {
          switch (geoValue.getAsString().toUpperCase()) {
            case "DE":
              dto.setGeoLocation(GeoLocations.GEO_DE);
              break;
            default:
              LOG.debug(String.format("Can't find a GeoLocation for \"%s", geoValue.getAsString()));
          }
        }
      }
    }
  }

  private void parsePriority(final DownloadDto dto, final JsonElement priority) {
    if (priority != null) {

      // array formitaeten
      final JsonArray formitaetList
              = priority.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_FORMITAET);
      for (final JsonElement formitaet : formitaetList) {
        parseFormitaet(dto, formitaet);
      }
    }
  }

  private void parseSubtitle(final DownloadDto dto, final JsonObject rootNode) {
    final JsonArray captionList = rootNode.getAsJsonArray(JSON_ELEMENT_CAPTIONS);
    final Iterator<JsonElement> captionIterator = captionList.iterator();
    while (captionIterator.hasNext()) {
      final JsonObject caption = captionIterator.next().getAsJsonObject();
      final JsonElement uri = caption.get(JSON_ELEMENT_URI);
      if (uri != null) {
        final String uriValue = uri.getAsString();

        // prefer xml subtitles
        if (uriValue.endsWith(RELEVANT_SUBTITLE_TYPE)) {
          dto.setSubTitleUrl(uriValue);
          break;
        } else if (dto.getSubTitleUrl().isPresent()) {
          dto.setSubTitleUrl(uriValue);
        }

      }
    }
  }

  private Qualities parseVideoQuality(final JsonObject quality) {
    Qualities qualityValue;
    final JsonElement hd = quality.get(JSON_ELEMENT_HD);
    if (hd != null && hd.getAsBoolean()) {
      qualityValue = Qualities.HD;
    } else {
      final String zdfQuality = quality.get(JSON_ELEMENT_QUALITY).getAsString();
      switch (zdfQuality) {
        case ZDF_QUALITY_LOW:
          qualityValue = Qualities.SMALL;
          break;
        case ZDF_QUALITY_MED:
          qualityValue = Qualities.SMALL;
          break;
        case ZDF_QUALITY_HIGH:
          qualityValue = Qualities.SMALL;
          break;
        case ZDF_QUALITY_VERYHIGH:
          qualityValue = Qualities.NORMAL;
          break;
        default:
          qualityValue = Qualities.SMALL;
      }
    }
    return qualityValue;
  }

  private void parseVideoUrls(final DownloadDto dto, final JsonObject rootNode) {
    // array priorityList
    final JsonArray priorityList = rootNode.getAsJsonArray(JSON_ELEMENT_PRIORITYLIST);
    for (final JsonElement priority : priorityList) {

      parsePriority(dto, priority);
    }
  }
}
