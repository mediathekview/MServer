package de.mediathekview.mserver.crawler.wdr.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.wdr.WdrMediaDto;
import java.lang.reflect.Type;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WdrVideoJsonDeserializer implements JsonDeserializer<Optional<WdrMediaDto>> {

  private static final Logger LOG = LogManager.getLogger(WdrVideoJsonDeserializer.class);

  private static final String ELEMENT_ALT = "alt";
  private static final String ELEMENT_CAPTION_HASH = "captionsHash";
  private static final String ELEMENT_DFLT = "dflt";
  private static final String ELEMENT_MEDIA_RESOURCE = "mediaResource";

  private static final String ATTRIBUTE_AUDIO_DESCRIPTION = "adVideoURL";
  private static final String ATTRIBUTE_CAPTION_URL = "captionURL";
  private static final String ATTRIBUTE_SIGN_LANUAGE = "slVideoURL";
  private static final String ATTRIBUTE_VERSION = "mediaVersion";
  private static final String ATTRIBUTE_VIDEO_URL = "videoURL";
  private static final String ATTRIBUTE_XML = "xml";

  private static final String MEDIA_VERSION_1_1 = "1.1.0";
  private static final String MEDIA_VERSION_1_2 = "1.2.0";
  private static final String MEDIA_VERSION_1_3 = "1.3.0";

  private final String protocol;

  public WdrVideoJsonDeserializer(final String aProtocol) {
    protocol = aProtocol;
  }

  @Override
  public Optional<WdrMediaDto> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {
    JsonObject jsonObject = aJsonElement.getAsJsonObject();

    Optional<String> version = JsonUtils.getAttributeAsString(jsonObject, ATTRIBUTE_VERSION);
    if (version.isPresent()) {
      switch (version.get()) {
        case MEDIA_VERSION_1_1:
          return deserializeVersion11(jsonObject);
        case MEDIA_VERSION_1_2:
          return deserializeVersion12(jsonObject);
        case MEDIA_VERSION_1_3:
          return deserializeVersion13(jsonObject);
        default:
          LOG.error("WdrVideoJsonDeserializer: unsupported media version: " + version.get());
      }
    }

    return Optional.empty();
  }

  private Optional<WdrMediaDto> deserializeVersion11(final JsonObject aJsonObject) {

    if (aJsonObject.has(ELEMENT_MEDIA_RESOURCE)) {
      final JsonObject mediaResourceObject = aJsonObject.get(ELEMENT_MEDIA_RESOURCE).getAsJsonObject();

      if (mediaResourceObject.has(ELEMENT_ALT)) {
        final JsonObject dfltObject = mediaResourceObject.get(ELEMENT_ALT).getAsJsonObject();
        Optional<String> mp4Url = JsonUtils.getAttributeAsString(dfltObject, ATTRIBUTE_VIDEO_URL);

        return createMediaDto(mp4Url, Optional.empty(), Optional.empty(), Optional.empty());
      }
    }

    return Optional.empty();
  }

  private Optional<WdrMediaDto> deserializeVersion12(final JsonObject aJsonObject) {

    if (aJsonObject.has(ELEMENT_MEDIA_RESOURCE)) {
      final JsonObject mediaResourceObject = aJsonObject.get(ELEMENT_MEDIA_RESOURCE).getAsJsonObject();
      Optional<String> subtitleUrl = JsonUtils.getAttributeAsString(mediaResourceObject, ATTRIBUTE_CAPTION_URL);

      if (mediaResourceObject.has(ELEMENT_DFLT)) {
        final JsonObject dfltObject = mediaResourceObject.get(ELEMENT_DFLT).getAsJsonObject();
        Optional<String> m3u8Url = JsonUtils.getAttributeAsString(dfltObject, ATTRIBUTE_VIDEO_URL);

        return createMediaDto(m3u8Url, subtitleUrl, Optional.empty(), Optional.empty());
      }
    }

    return Optional.empty();
  }

  private Optional<WdrMediaDto> deserializeVersion13(final JsonObject aJsonObject) {

    if (aJsonObject.has(ELEMENT_MEDIA_RESOURCE)) {
      final JsonObject mediaResourceObject = aJsonObject.get(ELEMENT_MEDIA_RESOURCE).getAsJsonObject();
      Optional<String> subtitleUrl = Optional.empty();

      if (mediaResourceObject.has(ELEMENT_CAPTION_HASH)) {
        final JsonObject subtitleObject = mediaResourceObject.get(ELEMENT_CAPTION_HASH).getAsJsonObject();
        subtitleUrl = JsonUtils.getAttributeAsString(subtitleObject, ATTRIBUTE_XML);
      }

      if (mediaResourceObject.has(ELEMENT_DFLT)) {
        final JsonObject dfltObject = mediaResourceObject.get(ELEMENT_DFLT).getAsJsonObject();
        Optional<String> m3u8Url = JsonUtils.getAttributeAsString(dfltObject, ATTRIBUTE_VIDEO_URL);
        Optional<String> audioDescriptionUrl = JsonUtils.getAttributeAsString(dfltObject, ATTRIBUTE_AUDIO_DESCRIPTION);
        Optional<String> signLanguageUrl = JsonUtils.getAttributeAsString(dfltObject, ATTRIBUTE_SIGN_LANUAGE);

        return createMediaDto(m3u8Url, subtitleUrl, audioDescriptionUrl, signLanguageUrl);
      }
    }

    return Optional.empty();
  }

  private Optional<WdrMediaDto> createMediaDto(final Optional<String> aM3U8Url,
      final Optional<String> aSubtitleUrl,
      final Optional<String> aAudioDescriptionUrl,
      final Optional<String> aSignLanguageUrl) {
    if (aM3U8Url.isPresent()) {
      WdrMediaDto dto = new WdrMediaDto(UrlUtils.addProtocolIfMissing(aM3U8Url.get(), protocol));
      if (aAudioDescriptionUrl.isPresent() && !aAudioDescriptionUrl.get().isEmpty()) {
        dto.setAudioDescriptionUrl(UrlUtils.addProtocolIfMissing(aAudioDescriptionUrl.get(), protocol));
      }
      if (aSignLanguageUrl.isPresent() && !aSignLanguageUrl.get().isEmpty()) {
        dto.setSignLanguageUrl(UrlUtils.addProtocolIfMissing(aSignLanguageUrl.get(), protocol));
      }
      if (aSubtitleUrl.isPresent() && !aSubtitleUrl.get().isEmpty()) {
        dto.setSubtitle(UrlUtils.addProtocolIfMissing(aSubtitleUrl.get(), protocol));
      }
      return Optional.of(dto);
    }

    return Optional.empty();
  }
}
