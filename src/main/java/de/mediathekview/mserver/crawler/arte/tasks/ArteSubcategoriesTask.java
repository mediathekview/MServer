package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.SendungOverviewDto;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ArteSubcategoriesTask extends ArteTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN = new TypeToken<SendungOverviewDto>() {
  }.getType();

  private final int pageNumber;

  public ArteSubcategoriesTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, int pageNumber) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(ArteConstants.AUTH_TOKEN));

    registerJsonDeserializer(SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteSubcategoryDeserializer());

    this.pageNumber = pageNumber;
  }

  public ArteSubcategoriesTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    this(aCrawler, aUrlToCrawlDtos, 1);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    SendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
        processNextPage(nextPageId.get());
      }
    }
  }

  private void processNextPage(String aNextPageId) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aNextPageId));
    Set<CrawlerUrlDTO> results = new ArteSubcategoriesTask(crawler, urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return null;
  }
}
