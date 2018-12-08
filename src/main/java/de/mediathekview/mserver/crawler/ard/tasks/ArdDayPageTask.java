package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdDayPageDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ArdDayPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type SET_FILMINFO_TYPE_TOKEN = new TypeToken<Set<ArdFilmInfoDto>>() {
  }.getType();

  public ArdDayPageTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    registerJsonDeserializer(SET_FILMINFO_TYPE_TOKEN, new ArdDayPageDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    final Set<ArdFilmInfoDto> filmUrls = deserialize(aTarget, SET_FILMINFO_TYPE_TOKEN);
    taskResults.addAll(filmUrls);
  }

  @Override
  protected AbstractRecrusivConverterTask createNewOwnInstance(ConcurrentLinkedQueue aElementsToProcess) {
    return new ArdDayPageTask(crawler, aElementsToProcess);
  }
}
