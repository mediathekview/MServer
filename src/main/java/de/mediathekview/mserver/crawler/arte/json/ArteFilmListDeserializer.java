package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import java.lang.reflect.Type;
import java.util.Optional;

public class ArteFilmListDeserializer implements JsonDeserializer<ArteSendungOverviewDto> {

  private static final String JSON_ELEMENT_VIDEOS = "videos";
  private static final String ATTRIBUTE_PROGRAM_ID = "programId";
  private static final String ATTRIBUTE_NEXT_PAGE = "nextPage";

  private final ArteLanguage language;

  public ArteFilmListDeserializer(ArteLanguage language) {
    this.language = language;
  }

  @Override
  public ArteSendungOverviewDto deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {

    final ArteSendungOverviewDto result = new ArteSendungOverviewDto();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();

      JsonUtils.getAttributeAsString(mainObj, ATTRIBUTE_NEXT_PAGE).ifPresent(result::setNextPageId);

      if (JsonUtils.checkTreePath(mainObj, Optional.empty(), getBaseElementName())) {
        mainObj.get(getBaseElementName()).getAsJsonArray().forEach(filmElement ->
            parseFilmUrl(filmElement.getAsJsonObject()).ifPresent(result::addUrl));
      }
    }
    return result;
  }

  private Optional<ArteFilmUrlDto> parseFilmUrl(final JsonObject jsonObject) {
    Optional<String> programId = JsonUtils.getAttributeAsString(jsonObject, ATTRIBUTE_PROGRAM_ID);
    if (programId.isPresent()) {
      String filmUrl = String.format(ArteConstants.URL_FILM_DETAILS, language.getLanguageCode().toLowerCase(), programId.get());
      String videoUrl = String.format(ArteConstants.URL_FILM_VIDEOS, language.getLanguageCode().toLowerCase(), programId.get());
      return Optional.of(new ArteFilmUrlDto(filmUrl, videoUrl));
    }

    return Optional.empty();
  }

  protected String getBaseElementName() {
    return JSON_ELEMENT_VIDEOS;
  }

}
