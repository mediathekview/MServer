package mServer.crawler.sender.arte.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.ArteSubcategoryUrlDto;
import mServer.crawler.sender.arte.json.ArteSubcategoryDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.TopicUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteSubcategoriesTask extends ArteTaskBase<TopicUrlDTO, CrawlerUrlDTO> {

  private static final int MAXIMUM_SUBPAGES = 1;

  private static final Type SUBCATEGORY_URL_TYPE_TOKEN =
          new TypeToken<ArteSubcategoryUrlDto>() {
          }.getType();

  private final int pageNumber;

  public ArteSubcategoriesTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs,
          final int pageNumber) {
    super(crawler, urlToCrawlDTOs, Optional.of(ArteConstants.AUTH_TOKEN));

    registerJsonDeserializer(SUBCATEGORY_URL_TYPE_TOKEN, new ArteSubcategoryDeserializer());

    this.pageNumber = pageNumber;
  }

  public ArteSubcategoriesTask(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    this(aCrawler, aUrlToCrawlDtos, 1);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArteSubcategoryUrlDto result = deserialize(aTarget, SUBCATEGORY_URL_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      final Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent() && pageNumber < MAXIMUM_SUBPAGES) {
        processNextPage(nextPageId.get());
      }
    }
  }

  private void processNextPage(final String aNextPageId) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aNextPageId));
    final Set<TopicUrlDTO> results =
            new ArteSubcategoriesTask(crawler, urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecursivConverterTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return null;
  }
}
