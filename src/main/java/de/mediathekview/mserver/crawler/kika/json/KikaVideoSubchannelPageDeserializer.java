package de.mediathekview.mserver.crawler.kika.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.kika.KikaFilmDto;

import java.lang.reflect.Type;
import java.util.Optional;


public class KikaVideoSubchannelPageDeserializer implements JsonDeserializer<PagedElementListDTO<KikaFilmDto>> {
  private static final String TAG_ROOT = "content";
  private static final String TAG_TYPE = "docType";
  private static final String TAG_ID = "id";
  private static final String TAG_UUID = "uuid";
  private static final String TAG_EXTERNALID = "externalId";
  private static final String TAG_URL_PATH = "urlPath";
  private static final String TAG_MODIFICATIONDATE = "modificationDate";
  private static final String[] TAG_API_ID = new String[] {"api","id"};
  private static final String[] TAG_API_URL = new String[] {"api","url"};
  //
  private static final String TAG_DATE                   = "date";
  private static final String TAG_TITLE                  = "title";
  private static final String TAG_TEASER_TEXT            = "teaserText";
  private static final String TAG_BROADCAST_SERIES_TITLE = "broadcastSeriesTitle";
  private static final String TAG_GEN_DATE               = "genDate";
  private static final String[] TAG_DESCRIPTION            = new String[] {"videoDetails","description"};
  private static final String[] TAG_EPISODE_NUMBER         = new String[] {"videoDetails","episodeNumber"};
  private static final String[] TAG_DURATION_IN_SECONDS    = new String[] {"videoDetails","durationInSeconds"};
  private static final String[] TAG_DURATION_IN_SECONDS2    = new String[] {"relatedVideoDetails","durationInSeconds"};
  private static final String[] TAG_SEASON                 = new String[] {"videoDetails","season"};
  
  //
  private static final String[] TAG_NEXT_PAGE = new String[] {"links", "next"};
  //
  @Override
  public PagedElementListDTO<KikaFilmDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    final PagedElementListDTO<KikaFilmDto> videoUrls = new PagedElementListDTO<>();
    // next page
    Optional<String> nextPage = JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE);
    videoUrls.setNextPage(nextPage);
    // film element
    if (jsonElement.getAsJsonObject().has(TAG_ROOT) &&
      jsonElement.getAsJsonObject().get(TAG_ROOT).isJsonArray()) {
      for (JsonElement element : jsonElement.getAsJsonObject().getAsJsonArray(TAG_ROOT)) {
        Optional<KikaFilmDto> e = parseElement(element);
        if (e.isPresent()) {
          videoUrls.addElement(e.get());
        }
      }
    }
    return videoUrls;
  }
  
  protected Optional<KikaFilmDto> parseElement(JsonElement root) {
    if (JsonUtils.getElementValueAsString(root, TAG_API_URL).isPresent()) {
      Optional<String> durationInSeconds = JsonUtils.getElementValueAsString(root, TAG_DURATION_IN_SECONDS);
      if (durationInSeconds.isEmpty()) {
        durationInSeconds = JsonUtils.getElementValueAsString(root, TAG_DURATION_IN_SECONDS2);
      }
      KikaFilmDto e = new KikaFilmDto(
          JsonUtils.getElementValueAsString(root, TAG_TYPE),
          JsonUtils.getElementValueAsString(root, TAG_ID),
          JsonUtils.getElementValueAsString(root, TAG_UUID),
          JsonUtils.getElementValueAsString(root, TAG_EXTERNALID),
          JsonUtils.getElementValueAsString(root, TAG_URL_PATH),
          JsonUtils.getElementValueAsString(root, TAG_API_ID),
          Optional.of(JsonUtils.getElementValueAsString(root, TAG_API_URL).get().replace("/relatedvideos/", "/videos/") + "/assets"),
          JsonUtils.getElementValueAsString(root, TAG_MODIFICATIONDATE),
          JsonUtils.getElementValueAsString(root, TAG_DATE),
          JsonUtils.getElementValueAsString(root, TAG_TITLE),
          JsonUtils.getElementValueAsString(root, TAG_TEASER_TEXT),
          JsonUtils.getElementValueAsString(root, TAG_BROADCAST_SERIES_TITLE),
          JsonUtils.getElementValueAsString(root, TAG_GEN_DATE),
          JsonUtils.getElementValueAsString(root, TAG_DESCRIPTION),
          JsonUtils.getElementValueAsString(root, TAG_EPISODE_NUMBER),
          durationInSeconds,
          JsonUtils.getElementValueAsString(root, TAG_SEASON)
          );
      return Optional.of(e);
    } else {
      return Optional.empty();
    }
  }
}
