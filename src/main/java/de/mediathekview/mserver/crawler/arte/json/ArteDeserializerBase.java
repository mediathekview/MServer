package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ArteDeserializerBase {

  private static final String JSON_ELEMENT_HREF = "href";
  private static final String JSON_ELEMENT_NEXT = "next";
  private static final String JSON_ELEMENT_LINKS = "links";
  private static final String JSON_ELEMENT_META = "meta";

  protected Optional<String> getNextPageLink(final JsonObject mainObj) {
    if (JsonUtils.checkTreePath(mainObj, Optional.empty(), JSON_ELEMENT_META, getBaseElementName(),
        JSON_ELEMENT_LINKS, JSON_ELEMENT_NEXT, JSON_ELEMENT_HREF)) {

      final String nextPageUrl = mainObj.get(JSON_ELEMENT_META).getAsJsonObject()
          .get(getBaseElementName()).getAsJsonObject().get(JSON_ELEMENT_LINKS).getAsJsonObject()
          .get(JSON_ELEMENT_NEXT).getAsJsonObject().get(JSON_ELEMENT_HREF).getAsString();
      return Optional.of(nextPageUrl);
    }

    return Optional.empty();
  }

  protected abstract String getBaseElementName();
}
