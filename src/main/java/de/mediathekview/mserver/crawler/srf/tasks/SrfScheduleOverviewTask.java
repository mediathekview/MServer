package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.tasks.ArdTaskBase;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfScheduleDeserializer;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;

public class SrfScheduleOverviewTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = 1978224352345L;

  private static final Type SRF_SCHEDULE_DESERIALIZER =
      new TypeToken<Set<CrawlerUrlDTO>>() {}.getType();

  public SrfScheduleOverviewTask(
      final AbstractCrawler crawler, final Queue<CrawlerUrlDTO> urlsToCrawl) {
    super(crawler, urlsToCrawl);

    registerJsonDeserializer(SRF_SCHEDULE_DESERIALIZER, new SrfScheduleDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> elementsToProcess) {
    return new SrfScheduleOverviewTask(crawler, elementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Set<CrawlerUrlDTO> results = deserialize(aTarget, SRF_SCHEDULE_DESERIALIZER, aDTO);
    taskResults.addAll(results);
  }

}
