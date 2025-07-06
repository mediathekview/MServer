package mServer.crawler.sender.zdf.json;

import com.google.gson.*;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.zdf.ZdfConstants;
import mServer.crawler.sender.zdf.ZdfTopicUrlDto;
import mServer.crawler.sender.zdf.ZdfUrlBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;

public class ZdfLetterPageDeserializer implements JsonDeserializer<PagedElementListDTO<ZdfTopicUrlDto>> {

  private static final Logger LOG = LogManager.getLogger(ZdfLetterPageDeserializer.class);

  @Override
  public PagedElementListDTO<ZdfTopicUrlDto> deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final PagedElementListDTO<ZdfTopicUrlDto> result = new PagedElementListDTO<>();
    JsonObject rootNode = json.getAsJsonObject();

    JsonObject content = rootNode
            .getAsJsonObject("data")
            .getAsJsonObject("specialPageByCanonical")
            .getAsJsonObject("content");
    JsonArray nodes = content.getAsJsonArray("nodes");

    for (JsonElement element : nodes) {
      JsonObject node = element.getAsJsonObject();
      final Optional<String> sender =
          JsonUtils.getElementValueAsString(node.getAsJsonObject("contentOwner"), "title");
      final Optional<String> topic = JsonUtils.getElementValueAsString(node, "title");
      final Optional<String> countSeasons = JsonUtils.getElementValueAsString(node, "countSeasons");
      if (ZdfConstants.PARTNER_TO_SENDER.containsKey(sender.orElse("ZDF"))) {
        if (countSeasons.isEmpty()) {
          // todo -> Sonderszenarien wie terra Xplore, das kleine Fernsehspiel => die Seiten müssten über PubForm verarbeitet werden
          final Optional<String> id = JsonUtils.getElementValueAsString(node, "id");
          id.ifPresent(s -> result.addElement(
                  new ZdfTopicUrlDto(
                          topic.orElse(""),
                          0,
                          s,
                          ZdfUrlBuilder.buildTopicNoSeasonUrl(
                                  ZdfConstants.EPISODES_PAGE_SIZE, s, ZdfConstants.NO_CURSOR)
                  )));
        } else {
          for (int i = 0; i < Integer.parseInt(countSeasons.orElse("0")); i++) {
            String canonical = node.get("canonical").getAsString();
            result.addElement(
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
