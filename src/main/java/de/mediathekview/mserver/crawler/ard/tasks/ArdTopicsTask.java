package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicsDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdTopicsTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicsTask.class);
  private static final Type SET_CRAWLER_URL_TYPE_TOKEN = new TypeToken<Set<CrawlerUrlDTO>>() {}.getType();
  private final String sender;

  public ArdTopicsTask(AbstractCrawler aCrawler, String sender, Queue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(aCrawler, urlToCrawlDTOs);
    this.sender = sender;
    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new ArdTopicsDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicsTask(this.crawler, sender, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    final Set<CrawlerUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN, aDTO);
    LOG.debug("Found {} topics for {}.", results.size(), sender);
    taskResults.addAll(results);

  }
}
