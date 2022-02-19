package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdTopicInfoDto;
import mServer.crawler.sender.ard.json.ArdTopicPageDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArdTopicPageTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicPageTask.class);

  private static final Type ARDTOPICINFODTO_TYPE_TOKEN =
          new TypeToken<ArdTopicInfoDto>() {
          }.getType();
  private static final String PAGE_NUMBER = "pageNumber";
  private static final String URL_PAGE_NUMBER_REPLACE_REGEX = PAGE_NUMBER + "%22%3A\\d+";
  private static final String PAGE_NUMBER_URL_ENCODED = PAGE_NUMBER + "%22%3A";

  public ArdTopicPageTask(MediathekReader aCrawler,
                          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(ARDTOPICINFODTO_TYPE_TOKEN, new ArdTopicPageDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    if (Config.getStop()) {
      return;
    }

    final ArdTopicInfoDto topicInfo = deserialize(aTarget, ARDTOPICINFODTO_TYPE_TOKEN);
    if (topicInfo != null
            && topicInfo.getFilmInfos() != null
            && !topicInfo.getFilmInfos().isEmpty()) {
      taskResults.addAll(topicInfo.getFilmInfos());
      LOG.debug("Found {} shows for a topic of ARD.", topicInfo.getFilmInfos().size());

      final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = createSubPageUrls(aTarget, topicInfo);
      if (!subpages.isEmpty()) {
        taskResults.addAll(createNewOwnInstance(subpages).fork().join());
      }
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createSubPageUrls(
          final WebTarget aTarget, final ArdTopicInfoDto topicInfo) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = new ConcurrentLinkedQueue<>();

    final int actualSubPageNumber = topicInfo.getSubPageNumber();
    final int maximumAllowedSubpages = getMaximumSubpages();
    if (actualSubPageNumber != 0) {
      LOG.debug("Sub page {} is already the maximum allowed sub page.", actualSubPageNumber);
      return subpages;
    }

    final int maxSubPageNumber = topicInfo.getMaxSubPageNumber();
    subpages.addAll(
            IntStream.range(
                            actualSubPageNumber + 1,
                            Math.min(maximumAllowedSubpages, maxSubPageNumber) + 1)
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

  private int getMaximumSubpages() {
    return 0;
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
  protected AbstractRecursivConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicPageTask(crawler, aElementsToProcess);
  }
}
