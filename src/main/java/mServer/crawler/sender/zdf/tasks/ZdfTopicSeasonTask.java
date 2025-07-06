package mServer.crawler.sender.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.zdf.ZdfConstants;
import mServer.crawler.sender.zdf.ZdfFilmDto;
import mServer.crawler.sender.zdf.ZdfTopicUrlDto;
import mServer.crawler.sender.zdf.ZdfUrlBuilder;
import mServer.crawler.sender.zdf.json.ZdfTopicSeasonDeserializer;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfTopicSeasonTask extends ZdfTaskBase<ZdfFilmDto, ZdfTopicUrlDto> {

  private static final Type SET_FILMDTO_TYPE_TOKEN =
      new TypeToken<PagedElementListDTO<ZdfFilmDto>>() {}.getType();

  public ZdfTopicSeasonTask(
          MediathekReader aCrawler, ConcurrentLinkedQueue<ZdfTopicUrlDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(authKey));
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
      final ConcurrentLinkedQueue<ZdfTopicUrlDto> urls = new ConcurrentLinkedQueue<>();
      urls.add(new ZdfTopicUrlDto(topic, season, canonical, ZdfUrlBuilder.buildTopicSeasonUrl(season, ZdfConstants.EPISODES_PAGE_SIZE, canonical, cursor.get())));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }

  @Override
  protected AbstractRestTask<ZdfFilmDto, ZdfTopicUrlDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ZdfTopicUrlDto> aElementsToProcess) {
    return new ZdfTopicSeasonTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
