package de.mediathekview.mserver.crawler.orf.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;

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
