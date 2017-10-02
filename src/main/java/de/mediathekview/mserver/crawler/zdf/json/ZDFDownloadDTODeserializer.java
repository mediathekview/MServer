package de.mediathekview.mserver.crawler.zdf.json;

import java.lang.reflect.Type;
import java.util.Iterator;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.GeoLocations;

/**
 * A JSON deserializer to gather the needed information for a {@link DownloadDTO}.
 */
public class ZDFDownloadDTODeserializer implements JsonDeserializer<DownloadDTO> {

  private static final String JSON_ELEMENT_ATTRIBUTES = "attributes";
  private static final String JSON_ELEMENT_AUDIO = "audio";
  private static final String JSON_ELEMENT_CAPTIONS = "captions";
  private static final String JSON_ELEMENT_FORMITAET = "formitaeten";
  private static final String JSON_ELEMENT_GEOLOCATION = "geoLocation";
  private static final String JSON_ELEMENT_HD = "hd";
  private static final String JSON_ELEMENT_MIMETYPE = "mimeType";
  private static final String JSON_ELEMENT_PRIORITYLIST = "priorityList";
  private static final String JSON_ELEMENT_Resolution = "Resolution";
  private static final String JSON_ELEMENT_QUALITY = "quality";
  private static final String JSON_ELEMENT_TRACKS = "tracks";
  private static final String JSON_ELEMENT_URI = "uri";

  private static final String JSON_PROPERTY_VALUE = "value";

  private static final String GEO_LOCATION_DACH = "dach";
  private static final String GEO_LOCATION_DE = "de";
  private static final String GEO_LOCATION_EBU = "ebu";

  private static final String RELEVANT_MIME_TYPE = "video/mp4";
  private static final String RELEVANT_SUBTITLE_TYPE = ".xml";

  @Override
  public DownloadDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT,
      final JsonDeserializationContext aJsonDeserializationContext) {
    final DownloadDTO dto = new DownloadDTO();

    final JsonObject rootNode = aJsonElement.getAsJsonObject();

    parseVideoUrls(dto, rootNode);
    parseSubtitle(dto, rootNode);
    parseGeoLocation(dto, rootNode);

    return dto;

  }

  private void parseGeoLocation(final DownloadDTO dto, final JsonObject rootNode) {
    final JsonElement attributes = rootNode.get(JSON_ELEMENT_ATTRIBUTES);
    if (attributes != null) {
      final JsonElement geoLocation = attributes.getAsJsonObject().get(JSON_ELEMENT_GEOLOCATION);
      if (geoLocation != null) {
        final JsonElement geoValue = geoLocation.getAsJsonObject().get(JSON_PROPERTY_VALUE);
        if (geoValue != null) {
          dto.setGeoLocation(GeoLocations.getFromDescription(geoValue.getAsString()));
        }
      }
    }
  }

  private void parseSubtitle(final DownloadDTO dto, final JsonObject rootNode) {
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
        } else if (dto.getSubTitleUrl().isEmpty()) {
          dto.setSubTitleUrl(uriValue);
        }

      }
    }
  }

  private void parseVideoUrls(final DownloadDTO aDto, final JsonObject aRootNode) {
    for (final JsonElement priority : aRootNode.get(JSON_ELEMENT_PRIORITYLIST).getAsJsonArray()) {
      // Map<Resolution, Map<Resolution,String>>
      // TODO
    }

  }


}

