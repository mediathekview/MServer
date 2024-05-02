package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicPageDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ArdTopicPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicPageTask.class);

  private static final Type ARDTOPICINFODTO_TYPE_TOKEN =
      new TypeToken<ArdTopicInfoDto>() {}.getType();
  private static final String PAGE_NUMBER = "pageNumber";
  private static final String URL_PAGE_NUMBER_REPLACE_REGEX = PAGE_NUMBER + "%22%3A\\d+";
  private static final String PAGE_NUMBER_URL_ENCODED = PAGE_NUMBER + "%22%3A";

  public ArdTopicPageTask(
      final AbstractCrawler aCrawler, final Queue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(ARDTOPICINFODTO_TYPE_TOKEN, new ArdTopicPageDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArdTopicInfoDto topicInfo = deserialize(aTarget, ARDTOPICINFODTO_TYPE_TOKEN, aDTO);
    if (topicInfo != null
        && topicInfo.getFilmInfos() != null
        && !topicInfo.getFilmInfos().isEmpty()) {
      taskResults.addAll(topicInfo.getFilmInfos());
      LOG.debug("Found {} shows for a topic of ARD.", topicInfo.getFilmInfos().size());

      final Queue<CrawlerUrlDTO> subpages = createSubPageUrls(aTarget, topicInfo);
      if (!subpages.isEmpty()) {
        taskResults.addAll(createNewOwnInstance(subpages).fork().join());
      }
    }
  }

  private Queue<CrawlerUrlDTO> createSubPageUrls(
      final WebTarget aTarget, final ArdTopicInfoDto topicInfo) {
    final Queue<CrawlerUrlDTO> subpages = new ConcurrentLinkedQueue<>();
    if (topicInfo.getTotalElements() < topicInfo.getPageSize() || topicInfo.getPageNumber() > 0) {
      return subpages;
    }
    //
    final Integer maximumAllowedSubpages = crawler.getCrawlerConfig().getMaximumSubpages();
    int index = 0;
    while ((topicInfo.getPageSize() + (index * topicInfo.getPageSize())) < topicInfo.getTotalElements()) {
      subpages.add(new CrawlerUrlDTO(changePageNumber(aTarget, index + 1)));
      index++;
      if (index >= maximumAllowedSubpages) {
        LOG.debug("ignore more subpage due to limit of {} pages but found {}", maximumAllowedSubpages, Integer.valueOf(topicInfo.getTotalElements() / topicInfo.getPageSize()));
        break;
      }
    }
    LOG.debug("Found {} subpage", subpages.size());
    return subpages;
  }

  private String changePageNumber(final WebTarget aTarget, final int newPageNumber) {
    return aTarget.getUri().toString().contains(PAGE_NUMBER)
        ? aTarget
            .getUriBuilder()
            .replaceQuery(
                aTarget
                    .getUri()
                    .getRawQuery()
                    .replaceAll(
                        URL_PAGE_NUMBER_REPLACE_REGEX, PAGE_NUMBER_URL_ENCODED + newPageNumber))
            .build()
            .toString()
        : aTarget.queryParam(PAGE_NUMBER, newPageNumber).getUri().toString();
  }

  @Override
  protected AbstractRecursiveConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicPageTask(crawler, aElementsToProcess);
  }
}
