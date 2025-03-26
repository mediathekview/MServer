package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import de.mediathekview.mserver.crawler.zdf.ZdfTopicUrlDto;
import de.mediathekview.mserver.crawler.zdf.ZdfUrlBuilder;
import de.mediathekview.mserver.crawler.zdf.json.ZdfTopicSeasonDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfTopicSeasonTask extends ZdfTaskBase<ZdfFilmDto, ZdfTopicUrlDto> {

  private static final Type SET_FILMDTO_TYPE_TOKEN =
      new TypeToken<PagedElementListDTO<ZdfFilmDto>>() {}.getType();

  public ZdfTopicSeasonTask(
          AbstractCrawler aCrawler, Queue<ZdfTopicUrlDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    registerJsonDeserializer(SET_FILMDTO_TYPE_TOKEN, new ZdfTopicSeasonDeserializer());
  }

  @Override
  protected void processRestTarget(ZdfTopicUrlDto aDTO, WebTarget aTarget) {
    final PagedElementListDTO<ZdfFilmDto> actual = deserialize(aTarget, SET_FILMDTO_TYPE_TOKEN);
    if (actual != null) {
      actual.getElements().forEach(film -> film.setTopic(aDTO.getTopic()));
      taskResults.addAll(actual.getElements());
      processNextPage(actual.getNextPage(), aDTO.getSeason(),aDTO.getCanonical(), aDTO.getTopic());
    }
  }

  private void processNextPage(final Optional<String> cursor, int season, final String canonical, final String topic) {
    if (cursor.isPresent() && !cursor.get().isEmpty()) {
      final Queue<ZdfTopicUrlDto> urls = new ConcurrentLinkedQueue<>();
      urls.add(new ZdfTopicUrlDto(topic, season, canonical, ZdfUrlBuilder.buildTopicSeasonUrl(season, ZdfConstants.EPISODES_PAGE_SIZE, canonical, cursor.get())));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<ZdfFilmDto, ZdfTopicUrlDto> createNewOwnInstance(
      Queue<ZdfTopicUrlDto> aElementsToProcess) {
    return new ZdfTopicSeasonTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
