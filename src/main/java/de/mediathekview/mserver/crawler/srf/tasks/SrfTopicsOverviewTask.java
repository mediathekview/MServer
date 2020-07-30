package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.tasks.ArdTaskBase;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfTopicsDeserializer;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class SrfTopicsOverviewTask extends ArdTaskBase<TopicUrlDTO, CrawlerUrlDTO> {

  private static final Type SET_CRAWLER_URL_TYPE_TOKEN =
      new TypeToken<Set<TopicUrlDTO>>() {}.getType();

  public SrfTopicsOverviewTask(
      AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    super(aCrawler, aURLsToCrawl);

    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new SrfTopicsDeserializer());
  }

  @Override
  protected AbstractRecrusivConverterTask createNewOwnInstance(
      ConcurrentLinkedQueue aElementsToProcess) {
    return new SrfTopicsOverviewTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Set<TopicUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN);
    taskResults.addAll(results);
  }
}
