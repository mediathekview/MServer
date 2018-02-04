package de.mediathekview.mserver.crawler.orf.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.orf.OrfEpisodeInfoDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Optional;

public class OrfEpisodeDeserializer implements JsonDeserializer<Optional<OrfEpisodeInfoDTO>>{
  
  private static final String ELEMENT_VIDEO = "video";
  
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_DESCRIPTION = "description";
  private static final String ATTRIBUTE_DURATION = "duration";
  
  @Override
  public Optional<OrfEpisodeInfoDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
    
    if (!aJsonElement.getAsJsonObject().has(ELEMENT_VIDEO)) {
      return Optional.empty();
    }
    
    JsonObject videoObject = aJsonElement.getAsJsonObject().get(ELEMENT_VIDEO).getAsJsonObject();
    final Optional<String> title = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_TITLE);
    final Optional<String> description = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_DESCRIPTION);
    final Optional<Duration> duration = parseDuration(videoObject);

    final Optional<OrfVideoInfoDTO> videoInfoOptional = parseUrls(videoObject);

    if (videoInfoOptional.isPresent()) {
      OrfEpisodeInfoDTO episode = new OrfEpisodeInfoDTO(videoInfoOptional.get(), title, description, duration);
      return Optional.of(episode);
    }
    
    return Optional.empty();
  }
  
  private Optional<OrfVideoInfoDTO> parseUrls(final JsonObject aVideoObject) {
    
    OrfVideoDetailDeserializer deserializer = new OrfVideoDetailDeserializer();
    return deserializer.deserializeVideoObject(aVideoObject);
  }
  
  private static Optional<Duration> parseDuration(final JsonObject aVideoObject) {
    if (aVideoObject.has(ATTRIBUTE_DURATION)) {
      Long durationValue = aVideoObject.get(ATTRIBUTE_DURATION).getAsLong();
      
      // Duration ist in Millisekunden angegeben, diese interessieren aber nicht
      return Optional.of(Duration.ofSeconds(durationValue / 1000));
    }
    
    return Optional.empty();
  }
}
