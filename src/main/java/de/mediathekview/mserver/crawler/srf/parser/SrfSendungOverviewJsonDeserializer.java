package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Type;
import java.util.Optional;

public class SrfSendungOverviewJsonDeserializer
    implements JsonDeserializer<Optional<PagedElementListDTO<CrawlerUrlDTO>>> {

  private static final org.apache.logging.log4j.Logger LOG =
      LogManager.getLogger(SrfSendungOverviewJsonDeserializer.class);

  private static final String ELEMENT_NEXT_PAGE = "nextPageUrl";
  private static final String ELEMENT_EPISODES = "episodes";
  private static final String ELEMENT_ID = "id";

  private final String baseUrl;

  public SrfSendungOverviewJsonDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  @Override
  public Optional<PagedElementListDTO<CrawlerUrlDTO>> deserialize(
      final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aJdc) {

    try {
      final PagedElementListDTO<CrawlerUrlDTO> dto = new PagedElementListDTO<>();

      final JsonObject object = aJsonElement.getAsJsonObject();
      parseNextPage(dto, object);
      parseEpisodes(dto, object);
      return Optional.of(dto);
    } catch (final Exception e) {
      LOG.error(e);
    }
    return Optional.empty();
  }

  private void parseNextPage(
      final PagedElementListDTO<CrawlerUrlDTO> aDto, final JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_NEXT_PAGE)) {
      final JsonElement nextPageElement = aJsonObject.get(ELEMENT_NEXT_PAGE);

      if (!nextPageElement.isJsonNull()) {
        aDto.setNextPage(Optional.of(baseUrl + nextPageElement.getAsString()));
      }
    }
  }

  private void parseEpisodes(
      final PagedElementListDTO<CrawlerUrlDTO> aDto, final JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_EPISODES)) {
      final JsonElement episodesElement = aJsonObject.get(ELEMENT_EPISODES);
      if (episodesElement.isJsonArray()) {
        episodesElement.getAsJsonArray().forEach(episode -> parseEpisode(aDto, episode));
      }
    }
  }

  private void parseEpisode(
      final PagedElementListDTO<CrawlerUrlDTO> aDto, final JsonElement aEpisode) {
    if (!aEpisode.isJsonNull()) {
      final JsonObject episodeObject = aEpisode.getAsJsonObject();
      if (episodeObject.has(ELEMENT_ID)) {
        aDto.addElement(new CrawlerUrlDTO(getUrl(episodeObject.get(ELEMENT_ID).getAsString())));
      }
    }
  }

  private String getUrl(final String aId) {
    return String.format(SrfConstants.SHOW_DETAIL_PAGE_URL, aId);
  }
}
