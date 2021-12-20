package mServer.crawler.sender.arte;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.base.Qualities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ArteVideoDeserializer
        implements JsonDeserializer<ArteVideoDTO> {

  private static final Logger LOG = LogManager.getLogger(ArteVideoDeserializer.class);

  private static final String JSON_OBJECT_KEY_PLAYER = "videoJsonPlayer";
  private static final String JSON_OBJECT_KEY_VSR = "VSR";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_QUALITY = "quality";
  private static final String ATTRIBUTE_VERSION_CODE = "versionCode";

  private final String sender;

  public ArteVideoDeserializer(String aSender) {
    this.sender = aSender;
  }

  @Override
  public ArteVideoDTO deserialize(
          JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {
    ArteVideoDTO arteVideoDTO = new ArteVideoDTO();
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

                final Optional<Qualities> resolution = mapQuality(quality);
                final Optional<ArteVideoType> arteVideoType = ArteVideoTypeMapper.map(sender, code);

                if (resolution.isPresent() && arteVideoType.isPresent()) {
                  switch (arteVideoType.get()) {
                    case DEFAULT:
                      arteVideoDTO.addVideo(resolution.get(), url);
                      break;
                    case SUBTITLE_INCLUDED:
                      arteVideoDTO.addVideoWithSubtitle(resolution.get(), url);
                      break;
                    case AUDIO_DESCRIPTION:
                      arteVideoDTO.addVideoWithAudioDescription(resolution.get(), url);
                      break;
                    case ORIGINAL_WITH_SUBTITLE:
                      arteVideoDTO.addVideoOriginalWithSubtitle(resolution.get(), url);
                      break;
                    case ORIGINAL:
                      arteVideoDTO.addVideoOriginal(resolution.get(), url);
                      break;
                  }
                }
              });
    }

    return arteVideoDTO;
  }

  private Optional<Qualities> mapQuality(String quality) {
    switch (quality) {
      case "EQ":
        return Optional.of(Qualities.NORMAL);
      case "HQ":
        return Optional.of(Qualities.SMALL);
      case "SQ":
        return Optional.of(Qualities.HD);
      case "MQ":
      case "XQ":
        return Optional.empty();
      default:
        LOG.debug("unknown quality: {}", quality);
        return Optional.empty();
    }
  }
}
