package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdTopicInfoDto;
import mServer.crawler.sender.ard.json.ArdTopicCompilationDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArdTopicCompilationTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type ARDTOPICINFODTO_TYPE_TOKEN =
          new TypeToken<ArdTopicInfoDto>() {}.getType();

  public ArdTopicCompilationTask(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(ARDTOPICINFODTO_TYPE_TOKEN, new ArdTopicCompilationDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArdTopicInfoDto topicInfo = deserialize(aTarget, ARDTOPICINFODTO_TYPE_TOKEN);
    if (topicInfo != null
            && topicInfo.getFilmInfos() != null
            && !topicInfo.getFilmInfos().isEmpty()) {
      taskResults.addAll(topicInfo.getFilmInfos());
    }
  }

  @Override
  protected AbstractRecursivConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicCompilationTask(crawler, aElementsToProcess);
  }
}