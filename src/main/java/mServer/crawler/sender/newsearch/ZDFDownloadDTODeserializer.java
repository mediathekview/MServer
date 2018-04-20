package mServer.crawler.sender.newsearch;

import java.lang.reflect.Type;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mediathekview.mlib.tool.Log;

/**
 * A JSON deserializer to gather the needed information for a
 * {@link DownloadDTO}.
 */
public class ZDFDownloadDTODeserializer implements JsonDeserializer<DownloadDTO> {

  private static final String JSON_ELEMENT_ATTRIBUTES = "attributes";
  private static final String JSON_ELEMENT_AUDIO = "audio";
  private static final String JSON_ELEMENT_CAPTIONS = "captions";
  private static final String JSON_ELEMENT_FORMITAET = "formitaeten";
  private static final String JSON_ELEMENT_GEOLOCATION = "geoLocation";
  private static final String JSON_ELEMENT_HD = "hd";
  private static final String JSON_ELEMENT_LANGUAGE = "language";
  private static final String JSON_ELEMENT_MIMETYPE = "mimeType";
  private static final String JSON_ELEMENT_PRIORITYLIST = "priorityList";
  private static final String JSON_ELEMENT_QUALITIES = "qualities";
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
  public DownloadDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    try {
      DownloadDTO dto = new DownloadDTO();

      JsonObject rootNode = aJsonElement.getAsJsonObject();

      parseVideoUrls(dto, rootNode);
      parseSubtitle(dto, rootNode);
      parseGeoLocation(dto, rootNode);

      return dto;
    } catch (Exception ex) {
      Log.errorLog(496583257, ex);
    }

    return null;
  }

  private void parseGeoLocation(DownloadDTO dto, JsonObject rootNode) {
    JsonElement attributes = rootNode.get(JSON_ELEMENT_ATTRIBUTES);
    if (attributes != null) {
      JsonElement geoLocation = attributes.getAsJsonObject().get(JSON_ELEMENT_GEOLOCATION);
      if (geoLocation != null) {
        JsonElement geoValue = geoLocation.getAsJsonObject().get(JSON_PROPERTY_VALUE);
        if (geoValue != null) {
          switch (geoValue.getAsString()) {
            case GEO_LOCATION_DACH:
              dto.setGeoLocation(GeoLocations.GEO_DE_AT_CH);
              break;
            case GEO_LOCATION_DE:
              dto.setGeoLocation(GeoLocations.GEO_DE);
              break;
            case GEO_LOCATION_EBU:
              dto.setGeoLocation(GeoLocations.GEO_DE_AT_CH_EU);
              break;
          }
        }
      }
    }
  }

  private void parseVideoUrls(DownloadDTO dto, JsonObject rootNode) {
    // array priorityList
    JsonArray priorityList = rootNode.getAsJsonArray(JSON_ELEMENT_PRIORITYLIST);
    Iterator<JsonElement> priorityIterator = priorityList.iterator();
    while (priorityIterator.hasNext()) {

      JsonObject priority = priorityIterator.next().getAsJsonObject();
      if (priority != null) {

        // array formitaeten
        JsonArray formitaetList = priority.getAsJsonArray(JSON_ELEMENT_FORMITAET);
        Iterator<JsonElement> formitaetIterator = formitaetList.iterator();
        while (formitaetIterator.hasNext()) {

          JsonObject formitaet = formitaetIterator.next().getAsJsonObject();

          // only mp4-videos are relevant 
          JsonElement mimeType = formitaet.get(JSON_ELEMENT_MIMETYPE);
          if (mimeType != null && mimeType.getAsString().equalsIgnoreCase(RELEVANT_MIME_TYPE)) {

            // array qualities
            JsonArray qualityList = formitaet.getAsJsonArray(JSON_ELEMENT_QUALITIES);
            Iterator<JsonElement> qualityIterator = qualityList.iterator();
            while (qualityIterator.hasNext()) {
              JsonObject quality = qualityIterator.next().getAsJsonObject();
              Qualities qualityValue = parseVideoQuality(quality);

              // subelement audio
              JsonElement audio = quality.get(JSON_ELEMENT_AUDIO);
              if (audio != null) {

                // array tracks
                JsonArray tracks = audio.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_TRACKS);
                tracks.forEach(track -> {
                  JsonObject trackObject = track.getAsJsonObject();
                  String language = trackObject.get(JSON_ELEMENT_LANGUAGE).getAsString();
                  String uri = trackObject.get(JSON_ELEMENT_URI).getAsString();
                  if (qualityValue != null && uri != null) {
                    dto.addUrl(language, qualityValue, uri);
                  } else {
                    throw new RuntimeException("either quality or uri is null");
                  }
                });

              }
            }
          }
        }
      }
    }
  }

  private Qualities parseVideoQuality(JsonObject quality) {
    Qualities qualityValue = null;
    JsonElement hd = quality.get(JSON_ELEMENT_HD);
    if (hd != null && hd.getAsBoolean() == true) {
      qualityValue = Qualities.HD;
    } else {
      String zdfQuality = quality.get(JSON_ELEMENT_QUALITY).getAsString();
      switch (zdfQuality) {
        case "low":
          qualityValue = Qualities.SMALL;
          break;
        case "med":
          qualityValue = Qualities.SMALL;
          break;
        case "high":
          qualityValue = Qualities.SMALL;
          break;
        case "veryhigh":
          qualityValue = Qualities.NORMAL;
          break;
        default:
          throw new RuntimeException("quality not supported: " + zdfQuality);
      }
    }
    return qualityValue;
  }

  private void parseSubtitle(DownloadDTO dto, JsonObject rootNode) {
    JsonArray captionList = rootNode.getAsJsonArray(JSON_ELEMENT_CAPTIONS);
    Iterator<JsonElement> captionIterator = captionList.iterator();
    while (captionIterator.hasNext()) {
      JsonObject caption = captionIterator.next().getAsJsonObject();
      JsonElement uri = caption.get(JSON_ELEMENT_URI);
      if (uri != null) {
        String uriValue = uri.getAsString();

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
}
