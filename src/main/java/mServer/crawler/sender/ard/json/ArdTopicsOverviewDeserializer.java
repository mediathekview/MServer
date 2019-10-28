package mServer.crawler.sender.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdUrlBuilder;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsonUtils;

public class ArdTopicsOverviewDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {

  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_GLOSSARY = "glossary";
  private static final String ELEMENT_SHOWS_PAGE = "showsPage";

  private static final String ATTRIBUTE_ID = "id";

  @Override
  public Set<CrawlerUrlDTO> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    Set<CrawlerUrlDTO> results = new HashSet<>();

    if (JsonUtils.checkTreePath(jsonElement, ELEMENT_DATA, ELEMENT_SHOWS_PAGE, ELEMENT_GLOSSARY)) {
      JsonObject glossaryObject = jsonElement.getAsJsonObject()
              .get(ELEMENT_DATA).getAsJsonObject()
              .get(ELEMENT_SHOWS_PAGE).getAsJsonObject()
              .get(ELEMENT_GLOSSARY).getAsJsonObject();

      for (String key : glossaryObject.keySet()) {
        if (key.startsWith("shows")) {
          results.addAll(parseShowEntry(glossaryObject.get(key)));
        }
      }
    }

    return results;
  }

  private Set<CrawlerUrlDTO> parseShowEntry(JsonElement jsonElement) {
    Set<CrawlerUrlDTO> results = new HashSet<>();

    if (jsonElement.isJsonArray()) {
      for (JsonElement entryElement : jsonElement.getAsJsonArray()) {
        JsonObject entryObject = entryElement.getAsJsonObject();
        Optional<String> id = JsonUtils.getAttributeAsString(entryObject, ATTRIBUTE_ID);
        id.ifPresent(s -> results.add(createUrlDto(s)));
      }
    }

    return results;
  }

  private CrawlerUrlDTO createUrlDto(final String id) {
    String url = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
            .addShowId(id)
            .addPageNumber(0)
            .addSavedQuery(ArdConstants.QUERY_TOPIC_VERSION, ArdConstants.QUERY_TOPIC_HASH)
            .build();

    return new CrawlerUrlDTO(url);
  }
}
