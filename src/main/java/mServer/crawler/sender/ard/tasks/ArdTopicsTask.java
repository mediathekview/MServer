package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.json.ArdTopicsDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArdTopicsTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicsTask.class);
  private static final Type SET_CRAWLER_URL_TYPE_TOKEN = new TypeToken<Set<CrawlerUrlDTO>>() {
  }.getType();
  private final String sender;

  public ArdTopicsTask(MediathekReader aCrawler, String sender, ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(aCrawler, urlToCrawlDTOs);
    this.sender = sender;
    registerJsonDeserializer(SET_CRAWLER_URL_TYPE_TOKEN, new ArdTopicsDeserializer(sender));
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicsTask(this.crawler, sender, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    final Set<CrawlerUrlDTO> results = deserialize(aTarget, SET_CRAWLER_URL_TYPE_TOKEN);
    LOG.debug("Found {} topics for {}.", results.size(), sender);
    taskResults.addAll(results);

  }
}
