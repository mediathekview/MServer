package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfTopicSeasonDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;

public class ZdfTopicSeasonTask extends ZdfTaskBase<ZdfFilmDto, TopicUrlDTO> {

  private static final Type SET_FILMDTO_TYPE_TOKEN =
      new TypeToken<Set<ZdfFilmDto>>() {}.getType();

  public ZdfTopicSeasonTask(
          AbstractCrawler aCrawler, Queue<TopicUrlDTO> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    registerJsonDeserializer(SET_FILMDTO_TYPE_TOKEN, new ZdfTopicSeasonDeserializer());
  }

  @Override
  protected void processRestTarget(TopicUrlDTO aDTO, WebTarget aTarget) {
    final Set<ZdfFilmDto> actual = deserialize(aTarget, SET_FILMDTO_TYPE_TOKEN);
    if (actual != null) {
      actual.forEach(film -> film.setTopic(aDTO.getTopic()));
      taskResults.addAll(actual);
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<ZdfFilmDto, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new ZdfTopicSeasonTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
