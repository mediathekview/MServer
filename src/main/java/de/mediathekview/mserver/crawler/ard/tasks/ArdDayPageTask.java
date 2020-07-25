package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdDayPageDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArdDayPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type SET_FILMINFO_TYPE_TOKEN =
      new TypeToken<Set<ArdFilmInfoDto>>() {}.getType();

  public ArdDayPageTask(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    registerJsonDeserializer(SET_FILMINFO_TYPE_TOKEN, new ArdDayPageDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Set<ArdFilmInfoDto> filmUrls = deserialize(aTarget, SET_FILMINFO_TYPE_TOKEN, aDTO);

    if (filmUrls != null && !filmUrls.isEmpty()) {
      taskResults.addAll(filmUrls);
    }
  }

  @Override
  protected AbstractRecrusivConverterTask createNewOwnInstance(
          final ConcurrentLinkedQueue aElementsToProcess) {
    return new ArdDayPageTask(crawler, aElementsToProcess);
  }
}
