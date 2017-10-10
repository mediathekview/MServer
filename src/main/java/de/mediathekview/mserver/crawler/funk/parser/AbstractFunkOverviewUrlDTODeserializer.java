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
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.funk.tasks.FunkSendungDTO;

public abstract class AbstractFunkOverviewUrlDTODeserializer
    implements JsonDeserializer<FunkOverviewDTO<FunkSendungDTO>> {
  private static final String JSON_ELEMENT_ATTRIBUTES = "attributes";
  private static final String JSON_ELEMENT_TITLE = "title";
  private static final String NEXT_PAGE_ID_REGEX_PATTERN = "(?<=page=)\\d+";
  private static final String JSON_ELEMENT_NEXT = "next";
  private static final String JSON_ELEMENT_LINKS = "links";
  private static final String JSON_ELEMENT_TYPE = "type";
  private static final String JSON_ELEMENT_ID = "id";
  private static final String FUNK_API_URL_TEMPLARE = "https://api.funk.net/v1.1/content/%s/%s";
  private final AbstractCrawler crawler;
  private final boolean incrementMaxCount;

  public AbstractFunkOverviewUrlDTODeserializer(final AbstractCrawler aCrawler,
      final boolean aIncrementMaxCount) {
    crawler = aCrawler;
    incrementMaxCount = aIncrementMaxCount;
  }

  @Override
  public FunkOverviewDTO<FunkSendungDTO> deserialize(final JsonElement aJsonElement,
      final Type aType, final JsonDeserializationContext aJsonDeserializationContext) {
    final FunkOverviewDTO<FunkSendungDTO> funkOverviewDTO = new FunkOverviewDTO<>();

    if (hasElements(aJsonElement, getArrayElementName())) {
      for (final JsonElement dataElement : aJsonElement.getAsJsonObject().get(getArrayElementName())
          .getAsJsonArray()) {
        addUrl(funkOverviewDTO, dataElement);
      }
    }
    addSubpage(aJsonElement, funkOverviewDTO);

    return funkOverviewDTO;
  }

  private void addSubpage(final JsonElement aJsonElement,
      final FunkOverviewDTO<FunkSendungDTO> funkOverviewDTO) {
    if (hasElements(aJsonElement, JSON_ELEMENT_LINKS)
        && hasElements(aJsonElement.getAsJsonObject().get(JSON_ELEMENT_LINKS), JSON_ELEMENT_NEXT)) {
      funkOverviewDTO.setNextPageId(getNextPageId(aJsonElement.getAsJsonObject()
          .getAsJsonObject(JSON_ELEMENT_LINKS).get(JSON_ELEMENT_NEXT).getAsString()));
    }
  }


  private void addUrl(final FunkOverviewDTO<FunkSendungDTO> funkOverviewDTO,
      final JsonElement dataElement) {
    if (hasElements(dataElement, JSON_ELEMENT_ID, JSON_ELEMENT_TYPE)) {
      final JsonObject dataObj = dataElement.getAsJsonObject();
      final FunkSendungDTO sendung = new FunkSendungDTO(
          String.format(FUNK_API_URL_TEMPLARE, dataObj.get(JSON_ELEMENT_TYPE).getAsString(),
              dataObj.get(JSON_ELEMENT_ID).getAsString()));
      if (hasElements(dataObj, JSON_ELEMENT_ATTRIBUTES)
          && hasElements(dataObj.get(JSON_ELEMENT_ATTRIBUTES), JSON_ELEMENT_TITLE)) {
        sendung.setThema(Optional.of(dataObj.get(JSON_ELEMENT_ATTRIBUTES).getAsJsonObject()
            .get(JSON_ELEMENT_TITLE).getAsString()));
      }

      funkOverviewDTO.addUrl(sendung);
      if (incrementMaxCount) {
        crawler.incrementAndGetMaxCount();
        crawler.updateProgress();
      }
    }

  }


  private Optional<Integer> getNextPageId(final String aNextPageLink) {
    final Matcher matcher = Pattern.compile(NEXT_PAGE_ID_REGEX_PATTERN).matcher(aNextPageLink);
    if (matcher.find()) {
      return Optional.of(Integer.parseInt(matcher.group()));
    }
    return Optional.empty();
  }


  protected abstract String getArrayElementName();


}
