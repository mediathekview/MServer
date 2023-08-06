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
import java.util.HashSet;
import java.util.Set;
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

  private static final Set<String> TOPICS_LOAD_ALL_PAGES = new HashSet<>();

  static {
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3N3ci5kZS8yNDEwMzE1Ng");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMjY4");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzA1");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3N3ci5kZS8yNDEwMzIzNA");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3N3ci5kZS8yNDEwMzAzNA");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy8yOGEwMzU4Yi00N2ViLTQ0MDktOGFmZi02ZjVkMDE5NDA2NDc");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy8wYTNlMzRiYy01OWRhLTRjY2UtOTJlOS01MTAxMjAzZmMzMWM");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3dkci5kZS93ZHJyZXRybw");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3dkci5kZS93ZHJyZXRyb3NwZXppYWw");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3dkci5kZS93ZHJyZXRyb3Nwb3J0");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3NyLW9ubGluZS5kZS9SRVRSTy1BUw");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3NyLW9ubGluZS5kZS9SRVRSTy1EU0Q");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3NyLW9ubGluZS5kZS9SRVRSTy1IRA");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3NyLW9ubGluZS5kZS9SRVRSTy1JRA");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3NyLW9ubGluZS5kZS9SRVRSTy1XTQ");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi5kZS9ha3R1ZWxsZXMtbWFnYXppbg");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi5kZS9iZXJpY2h0ZS1kb2t1cy1yZXBvcnRhZ2Vu");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi5kZS9iZXJsaW4tc3RlbGx0LXZvcg");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi1vbmxpbmUuZGUvYmVybGluZXItYWJlbmRzY2hhdQ");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi5kZS9kYXMtcHJvZmls");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi5kZS9tb3NhaWs");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JiYi5kZS93aWUtaWNoLWFuZ2VmYW5nZW4taGFiZQ");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL25kci5kZS80NTkx");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL25kci5kZS80NTg3");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2hyLW9ubGluZS8zODIyMDA5Nw");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2hyLW9ubGluZS8zODIyMDEzNQ");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2hyLW9ubGluZS8zODIyMDA5NQ");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2hyLW9ubGluZS8zODIyMDA5Ng");
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2IwYTJlZWFlLWI2NjAtNDI5Yi05ZTE3LTM5YzlkZDhmNTc4Ng");
    // Tatort
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhdG9ydA");
    // Filme im Ersten
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Zlcm5zZWhmaWxtZSBpbSBlcnN0ZW4");
    // temporary load all => remove if old entries exists
    // Unter unserem Himmel
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9icm9hZGNhc3RTZXJpZXM6L2JyZGUvZmVybnNlaGVuL2JheWVyaXNjaGVzLWZlcm5zZWhlbi9zZW5kdW5nZW4vdW50ZXItdW5zZXJlbS1oaW1tZWw");
    // in aller freundschaft die jungen ärzte    
    TOPICS_LOAD_ALL_PAGES.add("Y3JpZDovL2Rhc2Vyc3RlLmRlL2luLWFsbGVyLWZyZXVuZHNjaGFmdC1kaWUtanVuZ2VuLWFlcnp0ZQ");
  }

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
    final int maximumAllowedSubpages = getMaximumSubpages(topicInfo.getId());
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

  private int getMaximumSubpages(String id) {
    if (TOPICS_LOAD_ALL_PAGES.contains(id)) {
      return 999;
    }
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
