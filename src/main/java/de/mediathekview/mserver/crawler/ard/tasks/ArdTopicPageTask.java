package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicPageDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ArdTopicPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type SET_FILMINFO_TYPE_TOKEN = new TypeToken<Set<ArdFilmInfoDto>>() {
  }.getType();

  public ArdTopicPageTask(AbstractCrawler aCrawler,
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
  protected AbstractRecrusivConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicPageTask(crawler, aElementsToProcess);
  }
}
