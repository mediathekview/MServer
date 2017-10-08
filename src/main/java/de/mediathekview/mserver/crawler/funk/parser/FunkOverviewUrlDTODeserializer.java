package de.mediathekview.mserver.crawler.funk.parser;

import static de.mediathekview.mserver.base.utils.JsonUtils.hasElements;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class FunkOverviewUrlDTODeserializer implements JsonDeserializer<FunkOverviewDTO> {
  private static final String NEXT_PAGE_ID_REGEX_PATTERN = "(?<=page=)\\d+";
  private static final String JSON_ELEMENT_NEXT = "next";
  private static final String JSON_ELEMENT_LINKS = "links";
  private static final String JSON_ELEMENT_TYPE = "type";
  private static final String JSON_ELEMENT_ID = "id";
  private static final String JSON_ELEMENT_DATA = "data";
  private static final String FUNK_API_URL_TEMPLARE = "https://api.funk.net/v1.1/content/%s/%s";


  @Override
  public FunkOverviewDTO deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) {
    final FunkOverviewDTO funkOverviewDTO = new FunkOverviewDTO();

    if (hasElements(aJsonElement, JSON_ELEMENT_DATA)) {
      for (final JsonElement dataElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_DATA)
          .getAsJsonArray()) {
        addUrl(funkOverviewDTO, dataElement);
      }
    }
    addSubpage(aJsonElement, funkOverviewDTO);

    return funkOverviewDTO;
  }


  private void addSubpage(final JsonElement aJsonElement, final FunkOverviewDTO funkOverviewDTO) {
    if (hasElements(aJsonElement, JSON_ELEMENT_LINKS)
        && hasElements(aJsonElement.getAsJsonObject().get(JSON_ELEMENT_LINKS), JSON_ELEMENT_NEXT)) {
      funkOverviewDTO.setNextPageId(getNextPageId(aJsonElement.getAsJsonObject()
          .getAsJsonObject(JSON_ELEMENT_LINKS).get(JSON_ELEMENT_NEXT).getAsString()));
    }
  }


  private void addUrl(final FunkOverviewDTO funkOverviewDTO, final JsonElement dataElement) {
    if (hasElements(dataElement, JSON_ELEMENT_ID, JSON_ELEMENT_TYPE)) {
      final JsonObject dataObj = dataElement.getAsJsonObject();
      funkOverviewDTO.addUrl(new CrawlerUrlDTO(
          String.format(FUNK_API_URL_TEMPLARE, dataObj.get(JSON_ELEMENT_TYPE).getAsString(),
              dataObj.get(JSON_ELEMENT_ID).getAsString())));
    }
  }


  private Optional<Integer> getNextPageId(final String aNextPageLink) {
    final Matcher matcher = Pattern.compile(NEXT_PAGE_ID_REGEX_PATTERN).matcher(aNextPageLink);
    if (matcher.find()) {
      return Optional.of(Integer.parseInt(matcher.group()));
    }
    return Optional.empty();
  }


}
