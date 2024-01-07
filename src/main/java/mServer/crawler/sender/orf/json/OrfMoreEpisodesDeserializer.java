package mServer.crawler.sender.orf.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.orf.OrfConstants;

import java.lang.reflect.Type;
import java.util.Optional;

public class OrfMoreEpisodesDeserializer implements JsonDeserializer<CrawlerUrlDTO> {

  private static final String ATTRIBUTE_URL = "url";

  @Override
  public CrawlerUrlDTO deserialize(
          JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {

    final Optional<String> url =
            JsonUtils.getAttributeAsString(jsonElement.getAsJsonObject(), ATTRIBUTE_URL);
    return url.map(s -> new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(s, OrfConstants.URL_BASE))).orElse(null);
  }
}
