package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicCompilationDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;

public class ArdTopicCompilationTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {

  private static final Type ARDTOPICINFODTO_TYPE_TOKEN =
          new TypeToken<ArdTopicInfoDto>() {}.getType();

  public ArdTopicCompilationTask(
      final AbstractCrawler aCrawler, final Queue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(ARDTOPICINFODTO_TYPE_TOKEN, new ArdTopicCompilationDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArdTopicInfoDto topicInfo = deserialize(aTarget, ARDTOPICINFODTO_TYPE_TOKEN, aDTO);
    if (topicInfo != null
        && topicInfo.getFilmInfos() != null
        && !topicInfo.getFilmInfos().isEmpty()) {
      taskResults.addAll(topicInfo.getFilmInfos());
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicCompilationTask(crawler, aElementsToProcess);
  }
}
