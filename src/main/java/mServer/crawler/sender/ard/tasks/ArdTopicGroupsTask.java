package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.json.ArdTopicGroupsDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

public class ArdTopicGroupsTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Type DTO_TYPE_TOKEN =
          new TypeToken<HashSet<CrawlerUrlDTO>>() {}.getType();

  public ArdTopicGroupsTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDtos) {
    super(crawler, urlToCrawlDtos);
    registerJsonDeserializer(DTO_TYPE_TOKEN, new ArdTopicGroupsDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicGroupsTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    taskResults.addAll(deserialize(aTarget, DTO_TYPE_TOKEN));
  }


}