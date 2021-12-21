package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ArteVideoDetailsDeserializer
    implements JsonDeserializer<Optional<ArteVideoDetailDTO>> {

  private static final Logger LOG = LogManager.getLogger(ArteVideoDetailsDeserializer.class);

  private static final String JSON_OBJECT_KEY_PLAYER = "videoJsonPlayer";
  private static final String JSON_OBJECT_KEY_VSR = "VSR";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_QUALITY = "quality";
  private static final String ATTRIBUTE_VERSION_CODE = "versionCode";

  private final Sender sender;

  public ArteVideoDetailsDeserializer(Sender aSender) {
    this.sender = aSender;
  }

  @Override
  public Optional<ArteVideoDetailDTO> deserialize(
      JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {
    ArteVideoDetailDTO arteVideoDTO = new ArteVideoDetailDTO();
    if (aJsonElement.isJsonObject()
        && aJsonElement.getAsJsonObject().has(JSON_OBJECT_KEY_PLAYER)
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).isJsonObject()
        && aJsonElement
            .getAsJsonObject()
            .get(JSON_OBJECT_KEY_PLAYER)
            .getAsJsonObject()
            .has(JSON_OBJECT_KEY_VSR)
        && aJsonElement
            .getAsJsonObject()
            .get(JSON_OBJECT_KEY_PLAYER)
            .getAsJsonObject()
            .get(JSON_OBJECT_KEY_VSR)
            .isJsonObject()) {
      JsonObject playerObject =
          aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject();
      JsonObject vsrJsonObject = playerObject.get(JSON_OBJECT_KEY_VSR).getAsJsonObject();

      final Set<Map.Entry<String, JsonElement>> entries = vsrJsonObject.entrySet();
      entries.forEach(
          entry -> {
            final JsonObject value = entry.getValue().getAsJsonObject();

            final String code = value.get(ATTRIBUTE_VERSION_CODE).getAsString();
            final String quality = value.get(ATTRIBUTE_QUALITY).getAsString();
            final String url = value.get(ATTRIBUTE_URL).getAsString();

            final Optional<Resolution> resolution = mapQuality(quality);
              final Optional<ArteVideoType> arteVideoType = ArteVideoTypeMapper.map(sender, code);

            if (resolution.isPresent() && arteVideoType.isPresent()) {
              switch (arteVideoType.get()) {
                case DEFAULT -> arteVideoDTO.put(resolution.get(), url);
                case SUBTITLE_INCLUDED -> arteVideoDTO.putSubtitle(resolution.get(), url);
                case AUDIO_DESCRIPTION -> arteVideoDTO.putAudioDescription(resolution.get(), url);
                case ORIGINAL_WITH_SUBTITLE -> arteVideoDTO.putOriginalWithSubtitle(resolution.get(), url);
                case ORIGINAL -> arteVideoDTO.putOriginal(resolution.get(), url);
              }
            }
          });
      return Optional.of(arteVideoDTO);
    }

    return Optional.empty();
  }

  private Optional<Resolution> mapQuality(String quality) {
    switch (quality) {
      case "EQ":
        return Optional.of(Resolution.NORMAL);
      case "HQ":
        return Optional.of(Resolution.SMALL);
      case "SQ":
        return Optional.of(Resolution.HD);
      case "MQ":
        return Optional.of(Resolution.VERY_SMALL);
      case "XQ":
        return Optional.empty();
      default:
        LOG.debug("unknown quality: {}", quality);
        return Optional.empty();
    }
  }
}
