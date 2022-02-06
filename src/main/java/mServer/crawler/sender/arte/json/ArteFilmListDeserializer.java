package mServer.crawler.sender.arte.json;

import com.google.gson.*;
import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.ArteFilmUrlDto;
import mServer.crawler.sender.arte.ArteLanguage;
import mServer.crawler.sender.arte.ArteSendungOverviewDto;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArteFilmListDeserializer implements JsonDeserializer<ArteSendungOverviewDto> {

  private static final String JSON_ELEMENT_VIDEOS = "videos";
  private static final String ATTRIBUTE_PROGRAM_ID = "programId";
  private static final String ATTRIBUTE_NEXT_PAGE = "nextPage";

  private final ArteLanguage language;

  public ArteFilmListDeserializer(final ArteLanguage language) {
    this.language = language;
  }

  @Override
  public ArteSendungOverviewDto deserialize(
      final JsonElement aJsonElement,
      final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext)
      throws JsonParseException {

    final ArteSendungOverviewDto result = new ArteSendungOverviewDto();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();

      JsonUtils.getAttributeAsString(mainObj, ATTRIBUTE_NEXT_PAGE).ifPresent(result::setNextPageId);

      if (JsonUtils.checkTreePath(mainObj, getBaseElementName())) {
        mainObj
            .get(getBaseElementName())
            .getAsJsonArray()
            .forEach(
                filmElement ->
                    parseFilmUrl(filmElement.getAsJsonObject()).ifPresent(result::addUrl));
      }
    }
    return result;
  }

  private Optional<ArteFilmUrlDto> parseFilmUrl(final JsonObject jsonObject) {
    final Optional<String> programId =
        JsonUtils.getAttributeAsString(jsonObject, ATTRIBUTE_PROGRAM_ID);
    if (programId.isPresent()) {
      final String filmUrl =
          String.format(
              ArteConstants.URL_FILM_DETAILS,
              language.getLanguageCode().toLowerCase(),
              programId.get());
      final String videoUrl =
          String.format(
              ArteConstants.URL_FILM_VIDEOS,
              language.getLanguageCode().toLowerCase(),
              programId.get());
      return Optional.of(new ArteFilmUrlDto(filmUrl, videoUrl));
    }

    return Optional.empty();
  }

  protected String getBaseElementName() {
    return JSON_ELEMENT_VIDEOS;
  }
}
