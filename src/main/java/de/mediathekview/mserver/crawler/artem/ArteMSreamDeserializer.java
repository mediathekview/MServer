package de.mediathekview.mserver.crawler.artem;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ArteMSreamDeserializer implements JsonDeserializer<PagedElementListDTO<ArteMStreamDto>> {
  private static final String NEXT_PAGE[] = {"meta","videoStreams", "links", "next", "href"};
  private static final String ELEMENT_STREAMS = "videoStreams";
  private static final String ATTR_LANGUAGE = "language";
  private static final String ATTR_QUALITY = "quality";
  private static final String ATTR_MIMETYPE = "mimeType";
  private static final String ATTR_AUDIOCODE = "audioCode";
  private static final String ATTR_URL = "url";
  private static final String ELEMENT_SUBTITLES = "subtitles";
  private static final String ATTR_SUBTITLES_VERSION = "version";
  private static final String ATTR_SUBTITLES_FILENAME = "filename";
  
  @Override
  public PagedElementListDTO<ArteMStreamDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    PagedElementListDTO<ArteMStreamDto> list = new PagedElementListDTO<>();
    //
    list.setNextPage(JsonUtils.getElementValueAsString(jsonElement, NEXT_PAGE));
    //
    Optional<JsonElement> videos = JsonUtils.getElement(jsonElement, ELEMENT_STREAMS);
    if (videos.isEmpty()) {
      return list;
    }
    Optional<JsonElement> subtitle = JsonUtils.getElement(jsonElement, ELEMENT_SUBTITLES);
    Optional<Map<String,String>> subtitleStreams = Optional.empty();
    if (subtitle.isPresent()) {
      Map<String,String> subtitleEntries = new HashMap<>();
      for (JsonElement sub : subtitle.get().getAsJsonArray()) {
        subtitleEntries.put(
            JsonUtils.getElementValueAsString(sub, ATTR_SUBTITLES_VERSION).get(),
            JsonUtils.getElementValueAsString(sub, ATTR_SUBTITLES_FILENAME).get()
            );
      }
      subtitleStreams = Optional.of(subtitleEntries);
    }
    
    for (JsonElement stream : videos.get().getAsJsonArray()) {
      list.addElement(new ArteMStreamDto(
          JsonUtils.getElementValueAsString(stream, ATTR_LANGUAGE),
          JsonUtils.getElementValueAsString(stream, ATTR_QUALITY),
          JsonUtils.getElementValueAsString(stream, ATTR_MIMETYPE),
          JsonUtils.getElementValueAsString(stream, ATTR_AUDIOCODE),
          JsonUtils.getElementValueAsString(stream, ATTR_URL),
          subtitleStreams));
    }
    
    return list;
  }
  
  
}
