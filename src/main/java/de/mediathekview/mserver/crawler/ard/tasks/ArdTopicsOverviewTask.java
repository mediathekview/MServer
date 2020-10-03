package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicsOverviewDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;

public class ArdTopicsOverviewTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicsOverviewTask.class);
  private static final Type SET_CRAWLER_URL_TYPE_TOKEN =
      new TypeToken<Set<CrawlerUrlDTO>>() {}.getType();
  private final String sender;

  public ArdTopicsOverviewTask(
      final AbstractCrawler crawler,
      final String sender,
      final Queue<CrawlerUrlDTO> urlToCrawlDtos) {
    super(crawler, urlToCrawlDtos);
    this.sender = sender;
    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new ArdTopicsOverviewDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicsOverviewTask(crawler, sender, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Set<CrawlerUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN, aDTO);
    LOG.debug("Found {} shows for {}.", results.size(), sender);
    taskResults.addAll(results);
  }
}
