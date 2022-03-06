package de.mediathekview.mserver.crawler.orf.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_SRC;

public class OrfVideoDetailDeserializer implements JsonDeserializer<Optional<OrfVideoInfoDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfVideoDetailDeserializer.class);

  private static final String WRONG_HTTPS_URL_PART = ".apa.";
  private static final String RIGHT_HTTPS_URL_PART = ".sf.apa.";

  private static final String ELEMENT_PLAYLIST = "playlist";
  private static final String ELEMENT_VIDEOS = "videos";
  private static final String ELEMENT_SUBTITLES = "subtitles";
  private static final String ELEMENT_SOURCES = "sources";

  private static final String ATTRIBUTE_DELIVERY = "delivery";
  private static final String ATTRIBUTE_PROTOCOL = "protocol";
  private static final String ATTRIBUTE_QUALITY = "quality";
  private static final String ATTRIBUTE_TYPE = "type";

  private static final String RELEVANT_DELIVERY1 = "progressive";
  private static final String RELEVANT_DELIVERY2 = "hls";
  private static final String RELEVANT_PROTOCOL = "http";
  private static final String RELEVANT_SUBTITLE_TYPE = "ttml";
  private static final String RELEVANT_VIDEO_TYPE1 = "video/mp4";
  private static final String RELEVANT_VIDEO_TYPE2 = "application/x-mpegURL";

  private static String fixHttpsUrl(final String url) {
    if (url.contains(RIGHT_HTTPS_URL_PART)) {
      return url;
    }
    return url.replace(WRONG_HTTPS_URL_PART, RIGHT_HTTPS_URL_PART);
  }

  private static void parseVideo(final JsonElement aVideoElement, final OrfVideoInfoDTO dto) {
    if (aVideoElement.isJsonArray()) {
      aVideoElement
          .getAsJsonArray()
          .forEach(
              videoElement -> {
                final JsonObject videoObject = videoElement.getAsJsonObject();
                if (videoObject.has(ATTRIBUTE_PROTOCOL)
                    && videoObject.has(ATTRIBUTE_QUALITY)
                    && videoObject.has(ATTRIBUTE_SRC)
                    && videoObject.has(ATTRIBUTE_TYPE)) {
                  final String type = videoObject.get(ATTRIBUTE_TYPE).getAsString();
                  final String protocol = videoObject.get(ATTRIBUTE_PROTOCOL).getAsString();
                  final String delivery = videoObject.get(ATTRIBUTE_DELIVERY).getAsString();

                  if (isVideoRelevant(type, protocol, delivery)) {
                    final String quality = videoObject.get(ATTRIBUTE_QUALITY).getAsString();
                    final String url = fixHttpsUrl(videoObject.get(ATTRIBUTE_SRC).getAsString());

                      final Optional<Resolution> resolution = getQuality(quality);
                    resolution.ifPresent(resolution1 -> dto.put(resolution1, url));
                  }
                }
              });
    }
  }

  public Optional<OrfVideoInfoDTO> deserializeVideoObject(final JsonObject aVideoObject) {
    final OrfVideoInfoDTO dto = new OrfVideoInfoDTO();

    if (aVideoObject.has(ELEMENT_SOURCES)) {
      parseVideo(aVideoObject.get(ELEMENT_SOURCES), dto);
    }

    if (aVideoObject.has(ELEMENT_SUBTITLES)) {
      parseSubtitles(aVideoObject.get(ELEMENT_SUBTITLES), dto);
    }

    if (dto.hasVideoUrls()) {
      return Optional.of(dto);
    }

    return Optional.empty();
  }

  private static boolean isVideoRelevant(
      final String type, final String protocol, final String delivery) {
    return (type.equalsIgnoreCase(RELEVANT_VIDEO_TYPE1)
            || type.equalsIgnoreCase(RELEVANT_VIDEO_TYPE2))
        && protocol.equalsIgnoreCase(RELEVANT_PROTOCOL)
        && (delivery.equalsIgnoreCase(RELEVANT_DELIVERY1)
            || delivery.equalsIgnoreCase(RELEVANT_DELIVERY2));
  }

  private static void parseSubtitles(
      final JsonElement aSubtitlesElement, final OrfVideoInfoDTO dto) {
    if (aSubtitlesElement.isJsonArray()) {
      aSubtitlesElement
          .getAsJsonArray()
          .forEach(
              subtitleElement -> {
                final JsonObject subtitleObject = subtitleElement.getAsJsonObject();
                if (subtitleObject.has(ATTRIBUTE_SRC) && subtitleObject.has(ATTRIBUTE_TYPE)) {
                  final String type = subtitleObject.get(ATTRIBUTE_TYPE).getAsString();

                  if (type.equalsIgnoreCase(RELEVANT_SUBTITLE_TYPE)) {
                    final String url = fixHttpsUrl(subtitleObject.get(ATTRIBUTE_SRC).getAsString());
                    dto.setSubtitleUrl(url);
                  }
                }
              });
    }
  }

  private static Optional<Resolution> getQuality(final String aQuality) {
    switch (aQuality) {
      case "Q1A":
        return Optional.of(Resolution.VERY_SMALL);
      case "Q4A":
        return Optional.of(Resolution.SMALL);
      case "Q6A":
        return Optional.of(Resolution.NORMAL);
      case "Q8C":
        return Optional.of(Resolution.HD);
      case "Q0A":
      // QXA/QXB(DRM): another m3u8 has to be loaded which is often geoblocked
      case "QXA":
      case "QXADRM":
      case "QXB":
      case "QXBDRM":
      case "Q8A":
        return Optional.empty();
      default:
        LOG.debug("ORF: unknown quality: {}", aQuality);
    }
    return Optional.empty();
  }

  @Override
  public Optional<OrfVideoInfoDTO> deserialize(
      final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aContext) {

    final JsonObject jsonObject = aJsonElement.getAsJsonObject();
    if (jsonObject.has(ELEMENT_PLAYLIST)) {
      final JsonObject playlistObject = jsonObject.get(ELEMENT_PLAYLIST).getAsJsonObject();
      if (playlistObject.has(ELEMENT_VIDEOS)) {
        final JsonObject videoObject =
            playlistObject.get(ELEMENT_VIDEOS).getAsJsonArray().get(0).getAsJsonObject();

        return deserializeVideoObject(videoObject);
      }
    }

    return Optional.empty();
  }
}
