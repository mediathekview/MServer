package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.PaginationUrlDto;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicGroupsDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdTopicGroupsTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicGroupsTask.class);
  private static final String PAGE_NUMBER = "pageNumber";
  private static final String URL_PAGE_NUMBER_REPLACE_REGEX = PAGE_NUMBER + "=\\d+";
  private static final String PAGE_NUMBER_URL_ENCODED = PAGE_NUMBER + "=";
  private static final Type DTO_TYPE_TOKEN =
      new TypeToken<HashSet<CrawlerUrlDTO>>() {}.getType();

  public ArdTopicGroupsTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDtos) {
    super(crawler, urlToCrawlDtos);
    registerJsonDeserializer(DTO_TYPE_TOKEN, new ArdTopicGroupsDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicGroupsTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    taskResults.addAll(deserialize(aTarget, DTO_TYPE_TOKEN, aDTO));
  }


}
