package mServer.crawler.sender.phoenix.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class PhoenixFilmDetailDeserializer implements JsonDeserializer<Optional<PhoenixFilmDetailDto>> {

  private static final String ELEMENT_ABSAETZE = "absaetze";
  private static final String ELEMENT_BASENAME = "basename";
  private static final String ELEMENT_CONTENT = "content";
  private static final String ELEMENT_CANONICAL = "canonical";
  private static final String ELEMENT_META = "meta";
  private static final String ELEMENT_SUBTITLE = "subtitel";
  private static final String ELEMENT_TITLE = "titel";
  private static final String ELEMENT_TYP = "typ";
  private static final String ELEMENT_VERORTUNG = "verortung";
  private static final String ELEMENT_VORSPANN = "vorspann";

  private static final String TYP_VIDEO = "video-smubl";

  @Override
  public Optional<PhoenixFilmDetailDto> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    final JsonObject jsonObject = aJsonElement.getAsJsonObject();

    final Optional<String> title = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_TITLE);
    final Optional<String> subtitle = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_SUBTITLE);
    final Optional<String> description = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_VORSPANN);
    final Optional<String> baseName = parseBaseName(jsonObject);
    final Optional<String> website = parseWebsite(jsonObject);

    if (!title.isPresent() || !subtitle.isPresent() || !baseName.isPresent()) {
      return Optional.empty();
    }

    final Optional<String> verortung = parseFirstVerortung(jsonObject);

    PhoenixFilmDetailDto dto = new PhoenixFilmDetailDto();
    dto.setBaseName(baseName.get());
    dto.setTopic(determineTopic(title, verortung));
    dto.setTitle(determineTitle(title, subtitle, verortung));
    description.ifPresent(dto::setDescription);
    website.ifPresent(dto::setWebsite);

    return Optional.of(dto);
  }


  private String determineTitle(Optional<String> title, Optional<String> subtitle, Optional<String> verortung) {
    if (!verortung.isPresent() || verortung.get().equalsIgnoreCase(title.get())) {
      return subtitle.get();
    }

    if (!subtitle.isPresent() || subtitle.get().isEmpty()) {
      return title.get();
    }

    return String.format("%s - %s", title.get(), subtitle.get());
  }

  private String determineTopic(Optional<String> title, Optional<String> verortung) {
    if (!verortung.isPresent()) {
      return title.get();
    }
    return verortung.get();
  }

  private Optional<String> parseFirstVerortung(JsonObject jsonObject) {
    if (!jsonObject.has(ELEMENT_VERORTUNG)) {
      return Optional.empty();
    }

    final JsonArray verortungArray = jsonObject.get(ELEMENT_VERORTUNG).getAsJsonArray();
    if (verortungArray.size() > 0) {
      return JsonUtils.getAttributeAsString(verortungArray.get(0).getAsJsonObject(), ELEMENT_TITLE);
    }
    return Optional.empty();
  }

  private Optional<String> parseWebsite(JsonObject aJsonObject) {
    if (!aJsonObject.has(ELEMENT_META)) {
      return Optional.empty();
    }

    final JsonObject absatzObject = aJsonObject.get(ELEMENT_META).getAsJsonObject();
    Optional<String> website = JsonUtils.getAttributeAsString(absatzObject, ELEMENT_CANONICAL);
    if (website.isPresent()) {
      return Optional.of(website.get().replace("backend.phoenix.de", "www.phoenix.de"));
    }

    return Optional.empty();
  }

  private Optional<String> parseBaseName(JsonObject aJsonObject) {
    if (!aJsonObject.has(ELEMENT_ABSAETZE)) {
      return Optional.empty();
    }

    final JsonArray absatzArray = aJsonObject.get(ELEMENT_ABSAETZE).getAsJsonArray();
    for (JsonElement absatzElement : absatzArray) {
      // sometimes the json array contains null-entries...
      if (!absatzElement.isJsonNull()) {
        final JsonObject absatzObject = absatzElement.getAsJsonObject();
        final Optional<String> typ = JsonUtils.getAttributeAsString(absatzObject, ELEMENT_TYP);

        if (typ.isPresent() && TYP_VIDEO.equals(typ.get())) {
          if (absatzObject.has(ELEMENT_BASENAME)) {
            return JsonUtils.getAttributeAsString(absatzObject, ELEMENT_BASENAME);
          } else {
            return JsonUtils.getAttributeAsString(absatzObject, ELEMENT_CONTENT);
          }
        }
      }
    }

    return Optional.empty();
  }
}
