package de.mediathekview.mserver.crawler.kika.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.kika.KikaEntityDto;

import java.lang.reflect.Type;
import java.util.Optional;


public class KikaChannelPageDeserializer implements JsonDeserializer<PagedElementListDTO<KikaEntityDto>> {
  private static final String TAG_ROOT = "plusContent";
  private static final String TAG_BOXTYPE = "boxType";
  private static final String TAG_TYPE = "docType";
  private static final String TAG_ID = "id";
  private static final String TAG_UUID = "uuid";
  private static final String TAG_EXTERNALID = "externalId";
  private static final String TAG_URL_PATH = "urlPath";
  private static final String TAG_MODIFICATIONDATE = "modificationDate";
  private static final String[] TAG_API_ID = new String[] {"api","id"};
  private static final String[] TAG_API_URL = new String[] {"api","url"};
  private static final String[] TAG_NEXT_PAGE = new String[] {"links", "next"};
  

  @Override
  public PagedElementListDTO<KikaEntityDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    final PagedElementListDTO<KikaEntityDto> videoUrls = new PagedElementListDTO<>();
    // next page
    Optional<String> nextPage = JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE);
    videoUrls.setNextPage(nextPage);
    //
    if (jsonElement.getAsJsonObject().has(TAG_ROOT) &&
      jsonElement.getAsJsonObject().get(TAG_ROOT).isJsonArray()) {
      for (JsonElement plusContentElement : jsonElement.getAsJsonObject().getAsJsonArray(TAG_ROOT)) {
        Optional<String> boxType = JsonUtils.getElementValueAsString(plusContentElement, TAG_BOXTYPE);
        if (boxType.orElse("").equalsIgnoreCase("MainVideo") || 
            boxType.orElse("").equalsIgnoreCase("RelatedVideo") ||
            boxType.orElse("").equalsIgnoreCase("Channel")) {
          Optional<KikaEntityDto> channelLink = parseElement(plusContentElement.getAsJsonObject().get("channelLink"));
          channelLink.ifPresent(videoUrls::addElement);
        }
      }
    }
    return videoUrls;
  }
  
  protected Optional<KikaEntityDto> parseElement(JsonElement root) {
    if (JsonUtils.getElementValueAsString(root, TAG_API_URL).isPresent()) {
      KikaEntityDto e = new KikaEntityDto(
          JsonUtils.getElementValueAsString(root, TAG_TYPE),
          JsonUtils.getElementValueAsString(root, TAG_ID),
          JsonUtils.getElementValueAsString(root, TAG_UUID),
          JsonUtils.getElementValueAsString(root, TAG_EXTERNALID),
          JsonUtils.getElementValueAsString(root, TAG_URL_PATH),
          JsonUtils.getElementValueAsString(root, TAG_API_ID),
          Optional.of(JsonUtils.getElementValueAsString(root, TAG_API_URL).get()),
          JsonUtils.getElementValueAsString(root, TAG_MODIFICATIONDATE)
          );
      return Optional.of(e);
    } else {
      return Optional.empty();
    }
  }
}
