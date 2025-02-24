package de.mediathekview.mserver.crawler.zdf.json;

import com.google.gson.*;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;

/** A JSON deserializer to gather the needed information for a {@link DownloadDto}. */
public class ZdfDownloadDtoDeserializer implements JsonDeserializer<Optional<DownloadDto>> {

  private static final String ZDF_QUALITY_UHD = "uhd";
  private static final String ZDF_QUALITY_FHD = "fhd";
  private static final String ZDF_QUALITY_HD = "hd";
  private static final String ZDF_QUALITY_VERYHIGH = "veryhigh";
  private static final String ZDF_QUALITY_HIGH = "high";
  private static final String ZDF_QUALITY_MED = "med";
  private static final String ZDF_QUALITY_MEDIUM = "medium";
  private static final String ZDF_QUALITY_LOW = "low";
  private static final Logger LOG = LogManager.getLogger(ZdfDownloadDtoDeserializer.class);
  private static final String JSON_ELEMENT_ATTRIBUTES = "attributes";
  private static final String JSON_ELEMENT_AUDIO = "audio";
  private static final String JSON_ELEMENT_CAPTIONS = "captions";
  private static final String JSON_ELEMENT_CLASS = "class";
  private static final String JSON_ELEMENT_DURATION = "duration";
  private static final String JSON_ELEMENT_FORMITAET = "formitaeten";
  private static final String JSON_ELEMENT_GEOLOCATION = "geoLocation";
  private static final String JSON_ELEMENT_HIGHEST_VERTIVAL_RESOLUTION =
      "highestVerticalResolution";
  private static final String JSON_ELEMENT_LANGUAGE = "language";
  private static final String JSON_ELEMENT_MIMETYPE = "mimeType";
  private static final String JSON_ELEMENT_PRIORITYLIST = "priorityList";
  private static final String JSON_ELEMENT_QUALITY = "quality";
  private static final String JSON_ELEMENT_TRACKS = "tracks";
  private static final String JSON_ELEMENT_URI = "uri";

  private static final String JSON_PROPERTY_VALUE = "value";

  private static final String CLASS_AD = "ad";

  private static final String RELEVANT_MIME_TYPE = "video/mp4";
  private static final String RELEVANT_SUBTITLE_TYPE = ".xml";
  private static final String JSON_ELEMENT_QUALITIES = "qualities";

  private static AbstractMap.SimpleEntry<String, String> extractTrack(JsonElement aTrackElement) {
    JsonObject trackObject = aTrackElement.getAsJsonObject();
    String classValue = trackObject.get(JSON_ELEMENT_CLASS).getAsString();
    String language = trackObject.get(JSON_ELEMENT_LANGUAGE).getAsString();
    String uri = trackObject.get(JSON_ELEMENT_URI).getAsString();

    // films with audiodescription are handled as a language
    if (CLASS_AD.equalsIgnoreCase(classValue)) {
      language += "-ad";
    }
    if (uri != null) {
      return new AbstractMap.SimpleEntry<>(language, uri);
    } else {
      throw new RuntimeException("uri is null");
    }
  }

  @Override
  public Optional<DownloadDto> deserialize(
      final JsonElement aJsonElement,
      final Type aTypeOfT,
      final JsonDeserializationContext aJsonDeserializationContext) {
    final DownloadDto dto = new DownloadDto();
    try {
      final JsonObject rootNode = aJsonElement.getAsJsonObject();

      parseDuration(dto, rootNode);
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

  private void parseDuration(final DownloadDto dto, final JsonObject rootNode) {
    final JsonElement attributes = rootNode.get(JSON_ELEMENT_ATTRIBUTES);
    if (attributes != null) {
      final JsonElement durationElement = attributes.getAsJsonObject().get(JSON_ELEMENT_DURATION);
      if (durationElement != null) {
        final JsonElement durationValue =
            durationElement.getAsJsonObject().get(JSON_PROPERTY_VALUE);
        if (durationValue != null) {
          dto.setDuration(Duration.ofMillis(durationValue.getAsLong()));
        }
      }
    }
  }

  private void parseFormitaet(final List<DownloadInfo> downloads, final JsonElement formitaet) {
    // only mp4-videos are relevant
    final JsonElement mimeType = formitaet.getAsJsonObject().get(JSON_ELEMENT_MIMETYPE);
    if (mimeType != null && mimeType.getAsString().equalsIgnoreCase(RELEVANT_MIME_TYPE)) {


      // array Resolution
      final JsonArray qualityList =
          formitaet.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_QUALITIES);
      for (final JsonElement quality : qualityList) {

        final Resolution resolution = parseVideoQuality(quality.getAsJsonObject());
        final Optional<Integer> verticalResolution =
            JsonUtils.getAttributeAsInt(
                quality.getAsJsonObject(), JSON_ELEMENT_HIGHEST_VERTIVAL_RESOLUTION);
        // subelement audio
        final JsonElement audio = quality.getAsJsonObject().get(JSON_ELEMENT_AUDIO);
        if (audio != null) {

          // array tracks
          final JsonArray tracks = audio.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_TRACKS);

          for (JsonElement trackElement : tracks) {
            final AbstractMap.SimpleEntry<String, String> languageUri = extractTrack(trackElement);
            downloads.add(
                new DownloadInfo(
                    languageUri.getKey(),
                    languageUri.getValue(),
                    verticalResolution.orElse(0),
                    resolution));
          }
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
        if (geoValue != null) {
          final Optional<GeoLocations> foundGeoLocation = GeoLocations.find(geoValue.getAsString());
          if (foundGeoLocation.isPresent()) {
            dto.setGeoLocation(foundGeoLocation.get());
          } else {
            LOG.debug("Can't find a GeoLocation for {}", geoValue.getAsString());
          }
        }
      }
    }
  }

  private void parsePriority(final List<DownloadInfo> downloads, final JsonElement priority) {
    if (priority != null) {
      // array formitaeten
      final JsonArray formitaetList =
          priority.getAsJsonObject().getAsJsonArray(JSON_ELEMENT_FORMITAET);
      for (final JsonElement formitaet : formitaetList) {
        parseFormitaet(downloads, formitaet);
      }
    }
  }

  private void parseSubtitle(final DownloadDto dto, final JsonObject rootNode) {
    final JsonArray captionList = rootNode.getAsJsonArray(JSON_ELEMENT_CAPTIONS);
    if (captionList != null) {
      final Iterator<JsonElement> captionIterator = captionList.iterator();
      while (captionIterator.hasNext()) {
        final JsonObject caption = captionIterator.next().getAsJsonObject();
        final JsonElement uri = caption.get(JSON_ELEMENT_URI);
        if (uri != null) {
          final String uriValue = uri.getAsString();
          final String language = caption.get(JSON_ELEMENT_LANGUAGE).getAsString();

          // prefer xml subtitles
          if (uriValue.endsWith(RELEVANT_SUBTITLE_TYPE)
              || !dto.getSubTitleUrl(language).isPresent()) {
            dto.addSubTitleUrl(language, uriValue);
          }
        }
      }
    }
  }

  private Resolution parseVideoQuality(final JsonObject quality) {
    Resolution qualityValue;
    final String zdfQuality = quality.get(JSON_ELEMENT_QUALITY).getAsString();
    switch (zdfQuality) {
      case ZDF_QUALITY_LOW:
      case ZDF_QUALITY_MED:
      case ZDF_QUALITY_MEDIUM:
      case ZDF_QUALITY_HIGH:
        qualityValue = Resolution.SMALL;
        break;
      case ZDF_QUALITY_VERYHIGH:
      case ZDF_QUALITY_HD:
        qualityValue = Resolution.NORMAL;
        break;
      case ZDF_QUALITY_FHD:
        qualityValue = Resolution.HD;
        break;
      case ZDF_QUALITY_UHD:
        qualityValue = Resolution.UHD;
        break;
      default:
        LOG.error("unknown quality: {}", zdfQuality);
        qualityValue = Resolution.VERY_SMALL;
    }
    return qualityValue;
  }

  private void parseVideoUrls(final DownloadDto dto, final JsonObject rootNode) {
    List<DownloadInfo> downloads = new ArrayList<>();

    // array priorityList
    final JsonArray priorityList = rootNode.getAsJsonArray(JSON_ELEMENT_PRIORITYLIST);
    for (final JsonElement priority : priorityList) {

      parsePriority(downloads, priority);
    }

    downloads.sort(Comparator.comparingInt(DownloadInfo::verticalResolution));
    downloads.forEach(info -> dto.addUrl(info.language(), info.resolution(), info.uri()));
  }

  private record DownloadInfo(
      String language, String uri, int verticalResolution, Resolution resolution) {}
}
