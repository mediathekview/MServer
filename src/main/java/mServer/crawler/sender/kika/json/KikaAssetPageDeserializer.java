package mServer.crawler.sender.kika.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.kika.KikaAssetDto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class KikaAssetPageDeserializer implements JsonDeserializer<List<KikaAssetDto>> {
  private static final String TAG_ROOT = "assets";
  private static final String TAG_PROFILENAME = "profileName";
  private static final String TAG_FILENAME = "fileName";
  private static final String TAG_FILESIZE = "fileSize";
  private static final String TAG_MEDIATYPE = "mediaType";
  private static final String TAG_FRAMEWIDTH = "frameWidth";
  private static final String TAG_FRAMEHEIGHT = "frameHeight";
  private static final String TAG_BITRATEVIDEO = "bitrateVideo";
  private static final String TAG_BITRATEAUDIO = "bitrateAudio";
  private static final String TAG_TYPE = "type";
  private static final String TAG_URL = "url";
  private static final String TAG_VIDEOSUBTITLE = "videoSubtitle";
  private static final String TAG_WEBVTTURL = "webvttUrl";
  
  @Override
  public List<KikaAssetDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    final List<KikaAssetDto> videos = new ArrayList<>();
    // film element
    if (jsonElement.getAsJsonObject().has(TAG_ROOT) &&
      jsonElement.getAsJsonObject().get(TAG_ROOT).isJsonArray()) {
      Optional<String> videoSubtitle = JsonUtils.getElementValueAsString(jsonElement, TAG_VIDEOSUBTITLE);
      Optional<String> webvttUrl = JsonUtils.getElementValueAsString(jsonElement, TAG_WEBVTTURL);
      for (JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray(TAG_ROOT)) {
        Optional<KikaAssetDto> e = parseElement(element, videoSubtitle, webvttUrl);
        if (e.isPresent()) {
          videos.add(e.get());
        }
      }
    }
    return videos;
  }
  
  protected Optional<KikaAssetDto> parseElement(JsonElement root, Optional<String> videoSubtitle, Optional<String> webvttUrl) {
    KikaAssetDto e = new KikaAssetDto(
        JsonUtils.getElementValueAsString(root, TAG_PROFILENAME),
        JsonUtils.getElementValueAsString(root, TAG_FILENAME),
        JsonUtils.getElementValueAsInteger(root, TAG_FILESIZE),
        JsonUtils.getElementValueAsString(root, TAG_MEDIATYPE),
        JsonUtils.getElementValueAsInteger(root, TAG_FRAMEWIDTH),
        JsonUtils.getElementValueAsInteger(root, TAG_FRAMEHEIGHT),
        JsonUtils.getElementValueAsInteger(root, TAG_BITRATEVIDEO),
        JsonUtils.getElementValueAsInteger(root, TAG_BITRATEAUDIO),
        JsonUtils.getElementValueAsString(root, TAG_TYPE),
        JsonUtils.getElementValueAsString(root, TAG_URL),
        videoSubtitle,
        webvttUrl
        );
    return Optional.of(e);
  }
}
