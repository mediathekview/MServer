package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;

public class ArteVideoInfoDeserializer implements JsonDeserializer<PagedElementListDTO<ArteVideoInfoDto>> {
  private static final String EXTRAIT = "EXTRAIT";
  private static final HashSet<String> INCLUDE_KIND = new HashSet<>();
  static {
    INCLUDE_KIND.add("SHOW");
    INCLUDE_KIND.add("MANUAL_CLIP");
    INCLUDE_KIND.add("BONUS");
  };
  
  private static final String[] TAG_NEXT_PAGE_NEXT = {"meta","videos","links","next","href"};
  private static final String[] TAG_NEXT_PAGE_TOTAL = {"meta","videos","totalCount"};
  private static final String[] TAG_NEXT_PAGE_PAGES = {"meta","videos","pages"};
  private static final String[] TAG_NEXT_PAGE_PAGE = {"meta","videos","page"};
  
  private static final String TAG_SUBTITLES = "subtitles";
  private static final String TAG_SUBTITLES_ARTECODE = "arteCode";
  private static final String TAG_SUBTITLES_VERSION = "version";
  private static final String TAG_SUBTITLES_ISO6392CODE = "iso6392Code";
  private static final String TAG_SUBTITLES_ISO6391CODE = "iso6391Code";
  private static final String TAG_SUBTITLES_LABEL = "label";
  private static final String TAG_SUBTITLES_CLOSEDCAPTIONING = "closedCaptioning";
  private static final String TAG_SUBTITLES_BURNED = "burned";
  private static final String TAG_SUBTITLES_FILENAME = "filename";

  private static final String TAG_VIDEO_INFO = "videos";
  
  private static final String TAG_FIRST_BROADCAST_DATE = "firstBroadcastDate";
  private static final String TAG_ID = "id";
  private static final String TAG_PROGRAM_ID = "programId";
  private static final String TAG_CHANNEL = "channel";
  private static final String TAG_LANGUAGE = "language";
  private static final String TAG_KIND = "kind";
  private static final String TAG_CATALOG_TYPE = "catalogType";
  private static final String TAG_PROGRAM_TYPE = "programType";
  private static final String TAG_PLATFORM = "platform";
  private static final String TAG_PLATFORM_LABEL = "platformLabel";
  private static final String TAG_TITLE = "title";
  private static final String TAG_SUBTITLE = "subtitle";
  private static final String TAG_ORIGINAL_TITLE = "originalTitle";
  private static final String TAG_DURATION_SECONDS = "durationSeconds";
  private static final String TAG_SHORT_DESCRIPTION = "shortDescription";
  private static final String TAG_FULL_DESCRIPTION = "fullDescription";
  private static final String TAG_HEADER_TEXT = "headerText";
  private static final String TAG_GEOBLOCKING_ZONE = "geoblockingZone";
  private static final String TAG_URL = "url";
  private static final String TAG_SEASON = "season";
  private static final String TAG_EPISODE = "episode";
  private static final String TAG_BROADCAST_BEGIN = "broadcastBegin";
  private static final String TAG_CREATIONDATE = "creationDate";
  private static final String TAG_BROADCAST_BEGIN_ROUNDED = "broadcastBeginRounded";
  private static final String[] TAG_CATEGORY_CODE = {"category","code"};
  private static final String[] TAG_CATEGORY_NAME = {"category","name"};
  private static final String[] TAG_SUBCATEGORY_NAME = {"subcategory","name"};
   
  @Override
  public PagedElementListDTO<ArteVideoInfoDto> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final PagedElementListDTO<ArteVideoInfoDto> videoUrls = new PagedElementListDTO<>();
    //
    Optional<String> nextPage = JsonUtils.getElementValueAsString(json, TAG_NEXT_PAGE_NEXT);
    if (nextPage.isPresent()) {
      videoUrls.setNextPage(nextPage);
    }
    //
    final JsonObject searchElement = json.getAsJsonObject();
    final JsonArray itemArray = searchElement.getAsJsonArray(TAG_VIDEO_INFO);
    for (JsonElement arrayElement : itemArray) {
      parseVideoInfoElement(arrayElement).ifPresent(videoUrls::addElement);
    }
    return videoUrls;
  }
  
  protected Optional<ArteVideoInfoDto> parseVideoInfoElement(final JsonElement arrayElement) {
    // EXTRAIT
    if (JsonUtils.getElementValueAsString(arrayElement, TAG_PLATFORM).get().equalsIgnoreCase(EXTRAIT) ||
        !INCLUDE_KIND.contains(JsonUtils.getElementValueAsString(arrayElement, TAG_KIND).get().toUpperCase())) {
      return Optional.empty();
    }
    //
    List<ArteSubtitleLinkDto> arteRestSubtitleLinkDto = new ArrayList<>();
    if (arrayElement.getAsJsonObject().has(TAG_SUBTITLES) &&
        arrayElement.getAsJsonObject().get(TAG_SUBTITLES).isJsonArray()) {
      final JsonArray subtitles = arrayElement.getAsJsonObject().get(TAG_SUBTITLES).getAsJsonArray();
      for (JsonElement subs : subtitles) {
        arteRestSubtitleLinkDto.add(
            new ArteSubtitleLinkDto(
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_ARTECODE), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_VERSION), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_ISO6392CODE), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_ISO6391CODE), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_LABEL), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_CLOSEDCAPTIONING), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_BURNED), 
                JsonUtils.getElementValueAsString(subs, TAG_SUBTITLES_FILENAME)));
      }
    }
    //
    ArteVideoInfoDto arteRestVideoInfoDto = new ArteVideoInfoDto(
        JsonUtils.getElementValueAsString(arrayElement, TAG_FIRST_BROADCAST_DATE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_ID),
        JsonUtils.getElementValueAsString(arrayElement, TAG_PROGRAM_ID),
        JsonUtils.getElementValueAsString(arrayElement, TAG_CHANNEL),
        JsonUtils.getElementValueAsString(arrayElement, TAG_LANGUAGE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_KIND),
        JsonUtils.getElementValueAsString(arrayElement, TAG_CATALOG_TYPE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_PROGRAM_TYPE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_PLATFORM),
        JsonUtils.getElementValueAsString(arrayElement, TAG_PLATFORM_LABEL),
        JsonUtils.getElementValueAsString(arrayElement, TAG_TITLE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_SUBTITLE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_ORIGINAL_TITLE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_DURATION_SECONDS),
        JsonUtils.getElementValueAsString(arrayElement, TAG_SHORT_DESCRIPTION),
        JsonUtils.getElementValueAsString(arrayElement, TAG_FULL_DESCRIPTION),
        JsonUtils.getElementValueAsString(arrayElement, TAG_HEADER_TEXT),
        JsonUtils.getElementValueAsString(arrayElement, TAG_GEOBLOCKING_ZONE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_URL),
        JsonUtils.getElementValueAsString(arrayElement, TAG_SEASON),
        JsonUtils.getElementValueAsString(arrayElement, TAG_EPISODE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_BROADCAST_BEGIN),
        JsonUtils.getElementValueAsString(arrayElement, TAG_BROADCAST_BEGIN_ROUNDED),
        JsonUtils.getElementValueAsString(arrayElement, TAG_CATEGORY_CODE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_CATEGORY_NAME),
        JsonUtils.getElementValueAsString(arrayElement, TAG_SUBCATEGORY_NAME),
        JsonUtils.getElementValueAsString(arrayElement, TAG_CREATIONDATE),
        JsonUtils.getElementValueAsString(arrayElement, TAG_NEXT_PAGE_PAGE)
    );
    
    arteRestVideoInfoDto.setSubtitleLinks(arteRestSubtitleLinkDto);
    
    return Optional.of(arteRestVideoInfoDto);
  }

}
