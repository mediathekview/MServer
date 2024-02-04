package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.PaginationUrlDto;
import de.mediathekview.mserver.crawler.ard.json.ArdTopicsLetterDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdTopicsLetterTask extends ArdTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicsLetterTask.class);
  private static final String PAGE_NUMBER = "pageNumber";
  private static final String URL_PAGE_NUMBER_REPLACE_REGEX = PAGE_NUMBER + "=\\d+";
  private static final String PAGE_NUMBER_URL_ENCODED = PAGE_NUMBER + "=";
  private static final Type PAGINATION_URL_DTO_TYPE_TOKEN =
      new TypeToken<PaginationUrlDto>() {}.getType();
  private final String sender;

  public ArdTopicsLetterTask(
      final AbstractCrawler crawler,
      final String sender,
      final Queue<CrawlerUrlDTO> urlToCrawlDtos) {
    super(crawler, urlToCrawlDtos);
    this.sender = sender;
    registerJsonDeserializer(PAGINATION_URL_DTO_TYPE_TOKEN, new ArdTopicsLetterDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicsLetterTask(crawler, sender, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final PaginationUrlDto results = deserialize(aTarget, PAGINATION_URL_DTO_TYPE_TOKEN, aDTO);
    LOG.debug("Found {} shows for {}.", results.getUrls().size(), sender);
    taskResults.addAll(results.getUrls());

    if (results.getActualPage() == 0 && results.getMaxPages() > 1) {
      final Queue<CrawlerUrlDTO> subpages = createSubPageUrls(aTarget, results.getMaxPages());
      if (!subpages.isEmpty()) {
        taskResults.addAll(createNewOwnInstance(subpages).fork().join());
      }
    }
  }

  private Queue<CrawlerUrlDTO> createSubPageUrls(final WebTarget aTarget, final int maxPages) {

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
