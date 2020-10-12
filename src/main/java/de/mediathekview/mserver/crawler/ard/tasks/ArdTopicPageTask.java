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

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    final int actualSubPageNumber = topicInfo.getSubPageNumber();
    final Integer maximumAllowedSubpages = crawler.getCrawlerConfig().getMaximumSubpages();
    if (actualSubPageNumber != 0) {
      LOG.debug("Sub page {} is already the maximum allowed sub page.", actualSubPageNumber);
      return subpages;
    }

    final int maxSubPageNumber = topicInfo.getMaxSubPageNumber();
    subpages.addAll(
        IntStream.range(
                actualSubPageNumber + 1,
                (maximumAllowedSubpages >= maxSubPageNumber
                        ? maxSubPageNumber
                        : maximumAllowedSubpages)
                    + 1)
            .parallel()
            .mapToObj(subpageNumber -> changePageNumber(aTarget, subpageNumber))
            .map(CrawlerUrlDTO::new)
            .collect(Collectors.toSet()));

    if (LOG.isDebugEnabled() && maxSubPageNumber > maximumAllowedSubpages) {
      LOG.debug(
          "Found {} sub pages, these are {} more then the allowed {} to crawl. Added {} and skipped the rest.",
          maxSubPageNumber,
          maxSubPageNumber - maximumAllowedSubpages,
          maximumAllowedSubpages,
          subpages.size());
    }
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
