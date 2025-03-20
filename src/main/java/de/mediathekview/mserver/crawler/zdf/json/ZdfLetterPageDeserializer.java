package de.mediathekview.mserver.crawler.zdf.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfUrlBuilder;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfLetterPageDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {

  public static final int EPISODES_PAGE_SIZE = 24;
  private static final Logger LOG = LogManager.getLogger(ZdfLetterPageDeserializer.class);

  @Override
  public Set<TopicUrlDTO> deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final Set<TopicUrlDTO> canonicalSet = new HashSet<>();
    JsonObject rootNode = json.getAsJsonObject();
    JsonArray nodes =
        rootNode
            .getAsJsonObject("data")
            .getAsJsonObject("specialPageByCanonical")
            .getAsJsonObject("content")
            .getAsJsonArray("nodes");

    for (JsonElement element : nodes) {
      JsonObject node = element.getAsJsonObject();
      final Optional<String> sender =
          JsonUtils.getElementValueAsString(node.getAsJsonObject("contentOwner"), "title");
      final Optional<String> topic = JsonUtils.getElementValueAsString(node, "title");
      final Optional<String> countSeasons = JsonUtils.getElementValueAsString(node, "countSeasons");
      if (ZdfConstants.PARTNER_TO_SENDER.containsKey(sender.orElse("ZDF"))) {
        // TODO: gibt es den Fall, dass es keine Season gibt??
        if (countSeasons.isEmpty()) {
          LOG.error("no season found for {}", node.get("canonical").getAsString());
        }
        for (int i = 0; i < Integer.parseInt(countSeasons.orElse("0")); i++) {
          canonicalSet.add(
              new TopicUrlDTO(topic.orElse(""),
                  ZdfUrlBuilder.buildTopicSeasonUrl(
                      i, EPISODES_PAGE_SIZE, node.get("canonical").getAsString())));
        }
      }
    }

    return canonicalSet;
  }
}
