package de.mediathekview.mserver.crawler.zdf.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfPubFormResult;
import de.mediathekview.mserver.crawler.zdf.ZdfTopicUrlDto;
import de.mediathekview.mserver.crawler.zdf.ZdfUrlBuilder;
import java.lang.reflect.Type;
import java.util.Optional;

public class ZdfPubFormDeserializer extends ZdfTopicBaseClass
    implements JsonDeserializer<ZdfPubFormResult> {

  @Override
  public ZdfPubFormResult deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final ZdfPubFormResult result = new ZdfPubFormResult();
    JsonObject rootNode = json.getAsJsonObject();

    JsonObject content = rootNode.getAsJsonObject("data").getAsJsonObject("metaCollectionContent");
    JsonArray nodes = content.getAsJsonArray("smartCollections");

    for (JsonElement element : nodes) {
      JsonObject node = element.getAsJsonObject();
      Optional<String> sender = Optional.empty();
      if (!node.get("contentOwner").isJsonNull()) {
        sender = JsonUtils.getElementValueAsString(node.getAsJsonObject("contentOwner"), "title");
      }  
      final Optional<String> topic = JsonUtils.getElementValueAsString(node, "title");
      final Optional<String> countSeasons = JsonUtils.getElementValueAsString(node, "countSeasons");
      if (ZdfConstants.PARTNER_TO_SENDER.containsKey(sender.orElse("ZDF"))) {
        if (countSeasons.isEmpty()) {
          final Optional<String> collectionType =
              JsonUtils.getElementValueAsString(node, "collectionType");
          if (collectionType.isPresent() && collectionType.get().equals("MOVIE")) {
            result.addFilms(deserializeMovie(element));
          }
        } else {
          for (int i = 0; i < Integer.parseInt(countSeasons.orElse("0")); i++) {
            String canonical = node.get("canonical").getAsString();
            result.addTopic(
                new ZdfTopicUrlDto(
                    topic.orElse(""),
                    i,
                    canonical,
                    ZdfUrlBuilder.buildTopicSeasonUrl(
                        i, ZdfConstants.EPISODES_PAGE_SIZE, canonical)));
          }
        }
      }
    }

    result.setNextPage(parseNextPage(content.getAsJsonObject("pageInfo")));

    return result;
  }

  private Optional<String> parseNextPage(JsonObject pageInfo) {
    if (!pageInfo.isJsonNull()) {
      final Optional<String> hasNextPage = JsonUtils.getAttributeAsString(pageInfo, "hasNextPage");
      if (hasNextPage.isPresent() && hasNextPage.get().equals("true")) {
        return JsonUtils.getAttributeAsString(pageInfo, "endCursor");
      }
    }
    return Optional.empty();
  }
}
