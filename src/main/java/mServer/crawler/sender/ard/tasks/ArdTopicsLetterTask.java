package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.PaginationUrlDto;
import mServer.crawler.sender.ard.json.ArdTopicsLetterDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArdTopicsLetterTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicsLetterTask.class);
  private static final String PAGE_NUMBER = "pageNumber";
  private static final String URL_PAGE_NUMBER_REPLACE_REGEX = PAGE_NUMBER + "=\\d+";
  private static final String PAGE_NUMBER_URL_ENCODED = PAGE_NUMBER + "=";
  private static final Type PAGINATION_URL_DTO_TYPE_TOKEN =
          new TypeToken<PaginationUrlDto>() {
          }.getType();
  private final String sender;

  public ArdTopicsLetterTask(
          final MediathekReader crawler,
          final String sender,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDtos) {
    super(crawler, urlToCrawlDtos);
    this.sender = sender;
    registerJsonDeserializer(PAGINATION_URL_DTO_TYPE_TOKEN, new ArdTopicsLetterDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicsLetterTask(crawler, sender, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final PaginationUrlDto results = deserialize(aTarget, PAGINATION_URL_DTO_TYPE_TOKEN);
    LOG.debug("Found {} shows for {}.", results.getUrls().size(), sender);
    taskResults.addAll(results.getUrls());

    if (results.getActualPage() == 0 && results.getMaxPages() > 1) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = createSubPageUrls(aTarget, results.getMaxPages());
      if (!subpages.isEmpty()) {
        taskResults.addAll(createNewOwnInstance(subpages).fork().join());
      }
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createSubPageUrls(final WebTarget aTarget, final int maxPages) {

    return IntStream.range(1, maxPages)
            .mapToObj(subpageNumber -> changePageNumber(aTarget, subpageNumber))
            .map(CrawlerUrlDTO::new)
            .distinct()
            .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
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
}
