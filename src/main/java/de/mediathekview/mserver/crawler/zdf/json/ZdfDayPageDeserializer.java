package de.mediathekview.mserver.crawler.zdf.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

public class ZdfDayPageDeserializer implements JsonDeserializer<ZdfDayPageDto> {

  private static final String JSON_ELEMENT_ENTRIES = "http://zdf.de/rels/search/results";
  private static final String JSON_ELEMENT_MAIN_VIDEO_CONTENT = "mainVideoContent";
  private static final String JSON_ELEMENT_TARGET = "http://zdf.de/rels/target";
  private static final String JSON_ELEMENT_VIDEO_PAGE_TEASER =
      "http://zdf.de/rels/content/video-page-teaser";

  private static final String JSON_ATTRIBUTE_CANONICAL = "canonical";
  private static final String JSON_ATTRIBUTE_NEXT = "next";

  private final String apiUrlBase;

  public ZdfDayPageDeserializer(final String aApiUrlBase) {

    apiUrlBase = aApiUrlBase;
  }

  @Override
  public ZdfDayPageDto deserialize(
      final JsonElement aJsonElement,
      final Type aTypeOfT,
      final JsonDeserializationContext aContext) {

    final ZdfDayPageDto dayPageDto = new ZdfDayPageDto();

    final JsonObject json = aJsonElement.getAsJsonObject();
    parseSearchEntries(dayPageDto, json);
    parseNextPage(dayPageDto, json);

    return dayPageDto;
  }

  private void parseNextPage(final ZdfDayPageDto aDayPageDtp, final JsonObject aJsonObject) {
    if (aJsonObject.has(JSON_ATTRIBUTE_NEXT)) {
      String url = aJsonObject.get(JSON_ATTRIBUTE_NEXT).getAsString();
      url =
          UrlUtils.addDomainIfMissing(url, apiUrlBase)
              // replase type parameter because the link with types as numbers returns no results!
              .replaceFirst("&types=\\d+&", "&types=page-video&");
      aDayPageDtp.setNextPageUrl(url);
    }
  }

  private void parseSearchEntries(final ZdfDayPageDto aDayPageDto, final JsonObject aJsonObject) {
    if (aJsonObject.has(JSON_ELEMENT_ENTRIES)) {
      final JsonElement resultsElement = aJsonObject.get(JSON_ELEMENT_ENTRIES);
      if (resultsElement.isJsonArray()) {
        final JsonArray resultsArray = resultsElement.getAsJsonArray();
        resultsArray.forEach(
            result -> {
              final Optional<TopicUrlDTO> dto = parseSearchEntry(result.getAsJsonObject());
              dto.ifPresent(aDayPageDto::addEntry);
            });
      }
    }
  }

  private Optional<TopicUrlDTO> parseSearchEntry(final JsonObject aResultObject) {
    if (!aResultObject.has(JSON_ELEMENT_TARGET)) {
      return Optional.empty();
    }

    final JsonObject target = getTarget(aResultObject);
    if (target == null) {
      return Optional.empty();
    }

    final JsonObject mainVideoTarget = getMainVideoContentTarget(target);
    if (mainVideoTarget == null) {
      return Optional.empty();
    }

    if (target.has(JSON_ATTRIBUTE_CANONICAL)) {
      String canonical = target.get(JSON_ATTRIBUTE_CANONICAL).getAsString();
      String id = aResultObject.get("id").getAsString().replace("SCMS_", "");
      canonical = UrlUtils.addDomainIfMissing(canonical, apiUrlBase);
      if(id.contains("video_artede") 
        || id.contains("video-ard")
        || id.contains("video-kika")
        || id.contains("video_phoenix")
      ) {
        return Optional.empty();
      }

      final TopicUrlDTO dto = new TopicUrlDTO(id, canonical);
      return Optional.of(dto);
    }

    return Optional.empty();
  }

  private JsonObject getTarget(final JsonObject aResultObject) {
    JsonObject targetObject = aResultObject.getAsJsonObject(JSON_ELEMENT_TARGET);
    if (!targetObject.has(JSON_ELEMENT_MAIN_VIDEO_CONTENT)
        && targetObject.has(JSON_ELEMENT_VIDEO_PAGE_TEASER)) {
      targetObject = targetObject.getAsJsonObject(JSON_ELEMENT_VIDEO_PAGE_TEASER);
    }

    return targetObject;
  }

  private JsonObject getMainVideoContentTarget(final JsonObject aTargetObject) {

    if (aTargetObject.has(JSON_ELEMENT_MAIN_VIDEO_CONTENT)) {
      final JsonObject mainVideoContentObject =
          aTargetObject.getAsJsonObject(JSON_ELEMENT_MAIN_VIDEO_CONTENT);
      if (mainVideoContentObject.has(JSON_ELEMENT_TARGET)) {
        return mainVideoContentObject.getAsJsonObject(JSON_ELEMENT_TARGET);
      }
    }

    return null;
  }
}
