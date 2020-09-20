package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.tasks.ArdTaskBase;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import de.mediathekview.mserver.crawler.srf.parser.SrfTopicsDeserializer;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SrfTopicsOverviewTask extends ArdTaskBase<TopicUrlDTO, CrawlerUrlDTO> {

  private static final Type SET_CRAWLER_URL_TYPE_TOKEN =
      new TypeToken<Set<TopicUrlDTO>>() {}.getType();

  public SrfTopicsOverviewTask(
          final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    super(aCrawler, aURLsToCrawl);

    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new SrfTopicsDeserializer());
  }

  @Override
  protected AbstractRecrusivConverterTask createNewOwnInstance(
          final ConcurrentLinkedQueue aElementsToProcess) {
    return new SrfTopicsOverviewTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Set<TopicUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN, aDTO);
    taskResults.addAll(results);
    taskResults.addAll(addSpecialShows());
  }

  private Set<TopicUrlDTO> addSpecialShows() {
    final Set<TopicUrlDTO> shows = new HashSet<>();
    shows.add(
        new TopicUrlDTO(
            SrfConstants.ID_SHOW_SPORT_CLIP,
            String.format(
                SrfConstants.SHOW_OVERVIEW_PAGE_URL,
                SrfConstants.BASE_URL,
                SrfConstants.ID_SHOW_SPORT_CLIP)));

    return shows;
  }
}
