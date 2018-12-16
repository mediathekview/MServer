package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.json.ArdTopicPageDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

public class ArdTopicPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type SET_FILMINFO_TYPE_TOKEN = new TypeToken<Set<ArdFilmInfoDto>>() {
  }.getType();

  public ArdTopicPageTask(final MediathekReader aCrawler,
          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(SET_FILMINFO_TYPE_TOKEN, new ArdTopicPageDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Set<ArdFilmInfoDto> filmUrls = deserialize(aTarget, SET_FILMINFO_TYPE_TOKEN);

    if (filmUrls != null && !filmUrls.isEmpty()) {
      taskResults.addAll(filmUrls);
    }
  }

  @Override
  protected AbstractRecursivConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicPageTask(crawler, aElementsToProcess);
  }
}
