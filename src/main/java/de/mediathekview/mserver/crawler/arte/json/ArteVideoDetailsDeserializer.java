package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import java.lang.reflect.Type;
import java.util.Optional;

public class ArteVideoDetailsDeserializer implements
    JsonDeserializer<Optional<ArteVideoDetailDTO>> {

  private static final String JSON_OBJECT_KEY_PLAYER = "videoJsonPlayer";
  //  private static final String JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS = "videoDurationSeconds";
  private static final String JSON_OBJECT_KEY_VSR = "VSR";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_VERSION_SMALL_LIBELLE = "versionShortLibelle";

  private static final String VERSION_SMALL_LIBELLE_SUBTITLE_DE = "UT";
  private static final String VERSION_SMALL_LIBELLE_SUBTITLE_FR = "VOSTF";
  private static final String VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_DE = "AD (frz.)";
  private static final String VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_FR = "AD";


  private final Sender sender;

  public ArteVideoDetailsDeserializer(Sender aSender) {
    this.sender = aSender;
  }

  @Override
  public Optional<ArteVideoDetailDTO> deserialize(JsonElement aJsonElement, Type aType,
      JsonDeserializationContext aContext) {
    ArteVideoDetailDTO arteVideoDTO = new ArteVideoDetailDTO();
    if (aJsonElement.isJsonObject()
        && aJsonElement.getAsJsonObject().has(JSON_OBJECT_KEY_PLAYER)
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).isJsonObject()
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject()
        .has(JSON_OBJECT_KEY_VSR)
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject()
        .get(JSON_OBJECT_KEY_VSR).isJsonObject()
    ) {
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
      return Optional.of(arteVideoDTO);
    }

    return Optional.empty();
  }

  private void parseVideoUrls(ArteVideoDetailDTO arteVideoDTO, JsonObject vsrJsonObject) {
    Optional<String> smallUrl = getUrl(vsrJsonObject, "HTTPS_HQ_1", "HTTPS_MP4_HQ_1", "");
    if (smallUrl.isPresent()) {
      arteVideoDTO.put(Resolution.SMALL, smallUrl.get());
    }

    Optional<String> normalUrl = getUrl(vsrJsonObject, "HTTPS_EQ_1", "HTTPS_MP4_EQ_1", "");
    if (normalUrl.isPresent()) {
      arteVideoDTO.put(Resolution.NORMAL, normalUrl.get());
    }
    Optional<String> hdUrl = getUrl(vsrJsonObject, "HTTPS_SQ_1", "HTTPS_MP4_SQ_1", "");
    if (hdUrl.isPresent()) {
      arteVideoDTO.put(Resolution.HD, hdUrl.get());
    }
  }

  private void parseVideoWithSubtitleUrls(ArteVideoDetailDTO arteVideoDTO,
      JsonObject vsrJsonObject) {
    final String libelle = getSubtitleLibelle();
    Optional<String> smallUrl = getUrl(vsrJsonObject, "HTTPS_HQ_2", "HTTPS_MP4_HQ_2", libelle);
    if (smallUrl.isPresent()) {
      arteVideoDTO.putSubtitle(Resolution.SMALL, smallUrl.get());
    }

    Optional<String> normalUrl = getUrl(vsrJsonObject, "HTTPS_EQ_2", "HTTPS_MP4_EQ_2", libelle);
    if (normalUrl.isPresent()) {
      arteVideoDTO.putSubtitle(Resolution.NORMAL, normalUrl.get());
    }
    Optional<String> hdUrl = getUrl(vsrJsonObject, "HTTPS_SQ_2", "HTTPS_MP4_SQ_2", libelle);
    if (hdUrl.isPresent()) {
      arteVideoDTO.putSubtitle(Resolution.HD, hdUrl.get());
    }
  }

  private void parseVideoAudioDescriptionUrls(ArteVideoDetailDTO arteVideoDTO,
      JsonObject vsrJsonObject) {
    final String libelle = getAudioDescriptionLibelle();
    Optional<String> smallUrl = getUrl(vsrJsonObject, "HTTPS_HQ_3", "HTTPS_MP4_HQ_3", libelle);
    if (smallUrl.isPresent()) {
      arteVideoDTO.putAudioDescription(Resolution.SMALL, smallUrl.get());
    }

    Optional<String> normalUrl = getUrl(vsrJsonObject, "HTTPS_EQ_3", "HTTPS_MP4_EQ_3", libelle);
    if (normalUrl.isPresent()) {
      arteVideoDTO.putAudioDescription(Resolution.NORMAL, normalUrl.get());
    }
    Optional<String> hdUrl = getUrl(vsrJsonObject, "HTTPS_SQ_3", "HTTPS_MP4_SQ_3", libelle);
    if (hdUrl.isPresent()) {
      arteVideoDTO.putAudioDescription(Resolution.HD, hdUrl.get());
    }
  }

  private String getSubtitleLibelle() {
    switch (sender) {
      case ARTE_DE:
        return VERSION_SMALL_LIBELLE_SUBTITLE_DE;
      case ARTE_FR:
        return VERSION_SMALL_LIBELLE_SUBTITLE_FR;
    }
    return null;
  }

  private String getAudioDescriptionLibelle() {
    switch (sender) {
      case ARTE_DE:
        return VERSION_SMALL_LIBELLE_AUDIO_DESCRIPTION_DE;
      case ARTE_FR:
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
