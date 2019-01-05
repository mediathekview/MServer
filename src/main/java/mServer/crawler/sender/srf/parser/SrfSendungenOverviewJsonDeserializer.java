package mServer.crawler.sender.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mServer.crawler.sender.srf.SrfShowOverviewUrlBuilder;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfSendungenOverviewJsonDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {

  private static final String ELEMENT_TEASERLIST = "showTeaserList";
  private static final String ELEMENT_ID = "id";

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewJsonDeserializer.class);

  private final SrfShowOverviewUrlBuilder urlBuilder;

  public SrfSendungenOverviewJsonDeserializer(SrfShowOverviewUrlBuilder urlBuilder) {
    this.urlBuilder = urlBuilder;
  }

  @Override
  public Set<CrawlerUrlDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aJdc) throws JsonParseException {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    if (!aJsonElement.isJsonArray()) {
      LOG.error("invalid json elment structure");
    } else {
      JsonArray array = aJsonElement.getAsJsonArray();
      array.forEach(letterElement -> {
        results.addAll(parseLetter(letterElement.getAsJsonObject()));
      });
    }

    return results;
  }

  private Set<CrawlerUrlDTO> parseLetter(JsonObject letterObject) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    if (letterObject.has(ELEMENT_TEASERLIST)) {
      letterObject.get(ELEMENT_TEASERLIST).getAsJsonArray().forEach((JsonElement entry) -> {
        if (!entry.isJsonNull()) {
          Optional<String> urlDTO = parseEntry(entry.getAsJsonObject());

          if (urlDTO.isPresent()) {
            results.add(new CrawlerUrlDTO(urlDTO.get()));
          }
        }
      });
    }

    return results;
  }

  private Optional<String> parseEntry(JsonObject entryObject) {
    if (entryObject.has(ELEMENT_ID)) {
      String id = entryObject.get(ELEMENT_ID).getAsString();

      String url = urlBuilder.buildUrl(id);

      return Optional.of(url);
    }

    return Optional.empty();
  }
}
