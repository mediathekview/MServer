package de.mediathekview.mserver.crawler.artem;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaApiConstants;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;


public class ArteMVideoDeserializer implements JsonDeserializer<PagedElementListDTO<ArteMVideoDto>> {
  private static final String NEXT_PAGE[] = {"meta","videos", "links", "next", "href"};
  private static final String ELEMENT_VIDEO = "videos";
  private static final String ATTR_ID = "id";
  private static final String ATTR_PROGRAMID = "programId";
  private static final String ATTR_LANGUAGE = "language";
  private static final String ATTR_KIND = "kind";
  private static final String ATTR_PLATFORM = "platform";
  private static final String ATTR_PLATFORMLABEL = "platformLabel";
  private static final String ATTR_TITLE = "title";
  private static final String ATTR_SUBTITLE = "subtitle";
  private static final String ATTR_ORIGINALTITLE = "originalTitle";
  private static final String ATTR_DURATIONSECONDS = "durationSeconds";
  private static final String ATTR_SHORTDESCRIPTION = "shortDescription";
  private static final String ATTR_HEADERTEXT = "headerText";
  private static final String ATTR_GEOBLOCKINGZONE = "geoblockingZone";
  private static final String ATTR_URL = "url";
  private static final String ATTR_VIDEOSTREAMS[] = {"links","videoStreams","href"};
  private static final String ATTR_CREATIONDATE = "creationDate";
  //
  
  @Override
  public PagedElementListDTO<ArteMVideoDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    PagedElementListDTO<ArteMVideoDto> list = new PagedElementListDTO<>();
    //
    list.setNextPage(JsonUtils.getElementValueAsString(jsonElement, NEXT_PAGE));
    //
    Optional<JsonElement> videos = JsonUtils.getElement(jsonElement, ELEMENT_VIDEO);
    if (videos.isEmpty()) {
      return list;
    }
    for (JsonElement video : videos.get().getAsJsonArray()) {
      list.addElement(new ArteMVideoDto(
          JsonUtils.getElementValueAsString(video, ATTR_ID),
          JsonUtils.getElementValueAsString(video, ATTR_PROGRAMID),
          JsonUtils.getElementValueAsString(video, ATTR_LANGUAGE),
          JsonUtils.getElementValueAsString(video, ATTR_KIND),
          JsonUtils.getElementValueAsString(video, ATTR_PLATFORM),
          JsonUtils.getElementValueAsString(video, ATTR_PLATFORMLABEL),
          JsonUtils.getElementValueAsString(video, ATTR_TITLE),
          JsonUtils.getElementValueAsString(video, ATTR_SUBTITLE),
          JsonUtils.getElementValueAsString(video, ATTR_ORIGINALTITLE),
          JsonUtils.getElementValueAsString(video, ATTR_DURATIONSECONDS),
          JsonUtils.getElementValueAsString(video, ATTR_SHORTDESCRIPTION),
          JsonUtils.getElementValueAsString(video, ATTR_HEADERTEXT),
          JsonUtils.getElementValueAsString(video, ATTR_GEOBLOCKINGZONE),
          JsonUtils.getElementValueAsString(video, ATTR_URL),
          JsonUtils.getElementValueAsString(video, ATTR_VIDEOSTREAMS),
          JsonUtils.getElementValueAsString(video, ATTR_CREATIONDATE)));
    }
    
    return list;
  }
  
  
}
