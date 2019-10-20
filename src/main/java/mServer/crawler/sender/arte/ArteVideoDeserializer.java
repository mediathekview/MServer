package mServer.crawler.sender.arte;

import java.lang.reflect.Type;

import mServer.crawler.sender.newsearch.Qualities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.Const;
import java.util.Optional;
import mServer.crawler.sender.base.JsonUtils;

public class ArteVideoDeserializer implements
        JsonDeserializer<ArteVideoDTO> {

  private static final String JSON_OBJECT_KEY_PLAYER = "videoJsonPlayer";
  //  private static final String JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS = "videoDurationSeconds";
  private static final String JSON_OBJECT_KEY_VSR = "VSR";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_VERSION_SMALL_LIBELLE = "versionShortLibelle";

  private static final String VERSION_SMALL_LIBELLE_SUBTITLE_DE = "UT";
  private static final String VERSION_SMALL_LIBELLE_SUBTITLE_FR = "VOSTF";
  private static final String VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_DE = "AD (frz.)";
  private static final String VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_FR = "AD";

  private final String sender;

  public ArteVideoDeserializer(String aSender) {
    this.sender = aSender;
  }

  @Override
  public ArteVideoDTO deserialize(JsonElement aJsonElement, Type aType,
          JsonDeserializationContext aContext) {
    ArteVideoDTO arteVideoDTO = new ArteVideoDTO();
    if (aJsonElement.isJsonObject()
            && aJsonElement.getAsJsonObject().has(JSON_OBJECT_KEY_PLAYER)
            && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).isJsonObject()
            && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject()
                    .has(JSON_OBJECT_KEY_VSR)
            && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject()
                    .get(JSON_OBJECT_KEY_VSR).isJsonObject()) {
      JsonObject playerObject = aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER)
              .getAsJsonObject();
      JsonObject vsrJsonObject = playerObject.get(JSON_OBJECT_KEY_VSR).getAsJsonObject();

      parseVideoUrls(arteVideoDTO, vsrJsonObject);
      parseVideoWithSubtitleUrls(arteVideoDTO, vsrJsonObject);
      parseVideoAudioDescriptionUrls(arteVideoDTO, vsrJsonObject);

      /*
      if(!playerObject.get(JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS).isJsonNull())
      {
        arteVideoDTO.setDurationInSeconds(playerObject.get(JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS).getAsLong());
      }*/
      return arteVideoDTO;
    }

    return null;
  }

  private void parseVideoUrls(ArteVideoDTO arteVideoDTO, JsonObject vsrJsonObject) {
    Optional<String> smallUrl = getUrl(vsrJsonObject, "HTTPS_HQ_1", "HTTPS_MP4_HQ_1", "");
    if (smallUrl.isPresent()) {
      arteVideoDTO.addVideo(Qualities.SMALL, smallUrl.get());
    }

    Optional<String> normalUrl = getUrl(vsrJsonObject, "HTTPS_EQ_1", "HTTPS_MP4_EQ_1", "");
    if (normalUrl.isPresent()) {
      arteVideoDTO.addVideo(Qualities.NORMAL, normalUrl.get());
    }
    Optional<String> hdUrl = getUrl(vsrJsonObject, "HTTPS_SQ_1", "HTTPS_MP4_SQ_1", "");
    if (hdUrl.isPresent()) {
      arteVideoDTO.addVideo(Qualities.HD, hdUrl.get());
    }
  }

  private void parseVideoWithSubtitleUrls(ArteVideoDTO arteVideoDTO,
          JsonObject vsrJsonObject) {
    final String libelle = getSubtitleLibelle();
    Optional<String> smallUrl = getUrl(vsrJsonObject, "HTTPS_HQ_2", "HTTPS_MP4_HQ_2", libelle);
    if (smallUrl.isPresent()) {
      arteVideoDTO.addVideoWithSubtitle(Qualities.SMALL, smallUrl.get());
    }

    Optional<String> normalUrl = getUrl(vsrJsonObject, "HTTPS_EQ_2", "HTTPS_MP4_EQ_2", libelle);
    if (normalUrl.isPresent()) {
      arteVideoDTO.addVideoWithSubtitle(Qualities.NORMAL, normalUrl.get());
    }
    Optional<String> hdUrl = getUrl(vsrJsonObject, "HTTPS_SQ_2", "HTTPS_MP4_SQ_2", libelle);
    if (hdUrl.isPresent()) {
      arteVideoDTO.addVideoWithSubtitle(Qualities.HD, hdUrl.get());
    }
  }

  private void parseVideoAudioDescriptionUrls(ArteVideoDTO arteVideoDTO,
          JsonObject vsrJsonObject) {
    final String libelle = getAudioDescriptionLibelle();
    Optional<String> smallUrl = getUrl(vsrJsonObject, "HTTPS_HQ_3", "HTTPS_MP4_HQ_3", libelle);
    if (smallUrl.isPresent()) {
      arteVideoDTO.addVideoWithAudioDescription(Qualities.SMALL, smallUrl.get());
    }

    Optional<String> normalUrl = getUrl(vsrJsonObject, "HTTPS_EQ_3", "HTTPS_MP4_EQ_3", libelle);
    if (normalUrl.isPresent()) {
      arteVideoDTO.addVideoWithAudioDescription(Qualities.NORMAL, normalUrl.get());
    }
    Optional<String> hdUrl = getUrl(vsrJsonObject, "HTTPS_SQ_3", "HTTPS_MP4_SQ_3", libelle);
    if (hdUrl.isPresent()) {
      arteVideoDTO.addVideoWithAudioDescription(Qualities.HD, hdUrl.get());
    }
  }

  private String getSubtitleLibelle() {
    switch (sender) {
      case Const.ARTE_DE:
        return VERSION_SMALL_LIBELLE_SUBTITLE_DE;
      case Const.ARTE_FR:
        return VERSION_SMALL_LIBELLE_SUBTITLE_FR;
    }
    return null;
  }

  private String getAudioDescriptionLibelle() {
    switch (sender) {
      case Const.ARTE_DE:
        return VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_DE;
      case Const.ARTE_FR:
        return VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_FR;
    }
    return null;
  }

  private static Optional<String> getUrl(JsonObject vsrJsonObject, String firstId, String secondId,
          String smallLibelle) {
    if (vsrJsonObject.has(firstId)) {
      return getVideoUrl(vsrJsonObject, firstId, smallLibelle);
    } else if (vsrJsonObject.has(secondId)) {
      return getVideoUrl(vsrJsonObject, secondId, smallLibelle);
    }

    return Optional.empty();
  }

  private static boolean isRelevantSmallLibelle(JsonObject vsrJsonObject, String qualityTag,
          String smallLibelle) {
    // null bedeutet, nicht relevant, während "" gleich bedeutend ist mit einfach verwenden
    if (smallLibelle == null) {
      return false;
    }
    if (smallLibelle.isEmpty()) {
      return true;
    }

    Optional<String> currentSmallLibelle = JsonUtils
            .getAttributeAsString(vsrJsonObject.get(qualityTag).getAsJsonObject(),
                    ATTRIBUTE_VERSION_SMALL_LIBELLE);

    return currentSmallLibelle.isPresent() && currentSmallLibelle.get()
            .equalsIgnoreCase(smallLibelle);
  }

  private static Optional<String> getVideoUrl(JsonObject vsrJsonObject, String qualityTag,
          String smallLibelle) {
    if (vsrJsonObject.has(qualityTag) && isRelevantSmallLibelle(vsrJsonObject, qualityTag,
            smallLibelle)) {

      Optional<String> url = JsonUtils
              .getAttributeAsString(vsrJsonObject.get(qualityTag).getAsJsonObject(), ATTRIBUTE_URL);
      if (url.isPresent()) {
        return Optional.of(url.get());
      }
    }
    return Optional.empty();
  }
}
