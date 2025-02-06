package mServer.crawler.sender.arte;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;

import java.util.Optional;

public abstract class ArteListBaseDeserializer {

  private static final String JSON_ELEMENT_DATA = "data";
  private static final String JSON_ELEMENT_PROGRAMID = "programId";
  private static final String JSON_ELEMENT_PAGINATION = "pagination";
  private static final String JSON_ELEMENT_LINKS = "links";
  private static final String JSON_ELEMENT_NEXT = "next";

  private static String buildUrl(String nextUrl) {
    return UrlUtils.addDomainIfMissing(
            nextUrl
                    .replace("/api/emac/", "/api/rproxy/emac/")
                    // fix non reachable host
                    .replace("api-internal.infra-priv.arte.tv", "www.arte.tv")
            , "https://www.arte.tv");
  }

  protected Optional<String> parsePagination(JsonObject jsonObject) {
    if (jsonObject.has(JSON_ELEMENT_PAGINATION) && !jsonObject.get(JSON_ELEMENT_PAGINATION).isJsonNull()) {
      final JsonObject pagionationObject = jsonObject.get(JSON_ELEMENT_PAGINATION).getAsJsonObject();
      if (pagionationObject.has(JSON_ELEMENT_LINKS)) {
        final JsonObject linksObject = pagionationObject.get(JSON_ELEMENT_LINKS).getAsJsonObject();
        final Optional<String> nextUrl = JsonUtils.getAttributeAsString(linksObject, JSON_ELEMENT_NEXT);
        if (nextUrl.isPresent()) {
          return Optional.of(buildUrl(nextUrl.get()));
        }
      }
    }
    return Optional.empty();
  }

  protected void extractProgramIdFromData(JsonObject jsonObectWithData, ArteCategoryFilmsDTO dto) {
    if (jsonObectWithData.has(JSON_ELEMENT_DATA)) {
      for (JsonElement dataElement : jsonObectWithData.get(JSON_ELEMENT_DATA).getAsJsonArray()) {
        if (!dataElement.getAsJsonObject().get(JSON_ELEMENT_PROGRAMID).isJsonNull()) {
          Optional<String> programId = JsonUtils.getAttributeAsString(dataElement.getAsJsonObject(), JSON_ELEMENT_PROGRAMID);
          if (programId.isPresent()) {
            if (programId.get().startsWith("RC-")) {
              try {
                long collectionId = Long.parseLong(programId.get().replace("RC-", ""));
                dto.addCollection(String.format("RC-%06d", collectionId));
              } catch (NumberFormatException e) {
                Log.errorLog(12834939, "Invalid collection id: " + programId);
              }
            } else {
              dto.addProgramId(programId.get());
            }
          }
        }
      }
    }
  }
}
