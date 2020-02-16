package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicPageDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArdTopicPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicPageTask.class);

  private static final Type ARDTOPICINFODTO_TYPE_TOKEN =
      new TypeToken<ArdTopicInfoDto>() {}.getType();
  private static final String URL_PAGE_NUMBER_REPLACE_REGEX = "pageNumber%22%3A\\d+";
  private static final String PAGE_NUMBER_URL_ENCODED = "pageNumber%22%3A";

  public ArdTopicPageTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(ARDTOPICINFODTO_TYPE_TOKEN, new ArdTopicPageDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArdTopicInfoDto topicInfo = deserialize(aTarget, ARDTOPICINFODTO_TYPE_TOKEN);
    if (topicInfo != null
        && topicInfo.getFilmInfos() != null
        && !topicInfo.getFilmInfos().isEmpty()) {
      taskResults.addAll(topicInfo.getFilmInfos());

      final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = createSubPageUrls(aTarget, topicInfo);
      if (!subpages.isEmpty()) {
        taskResults.addAll(createNewOwnInstance(subpages).fork().join());
      }
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createSubPageUrls(
      final WebTarget aTarget, final ArdTopicInfoDto topicInfo) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = new ConcurrentLinkedQueue<>();
    for (int newPageNumber = topicInfo.getSubPageNumber() + 1;
        newPageNumber <= topicInfo.getMaxSubPageNumber();
        newPageNumber++) {
      try {
        subpages.add(new CrawlerUrlDTO(changePageNumber(aTarget, newPageNumber).toURL()));
      } catch (final MalformedURLException malformedURLException) {
        LOG.fatal("A ARD sub page URL couldn't be build!", malformedURLException);
      }
    }
    return subpages;
  }

  private URI changePageNumber(final WebTarget aTarget, final int newPageNumber) {
    return aTarget
        .getUriBuilder()
        .replaceQuery(
            aTarget
                .getUri()
                .getRawQuery()
                .replaceAll(URL_PAGE_NUMBER_REPLACE_REGEX, PAGE_NUMBER_URL_ENCODED + newPageNumber))
        .build();
  }

  @Override
  protected AbstractRecrusivConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicPageTask(crawler, aElementsToProcess);
  }
}
