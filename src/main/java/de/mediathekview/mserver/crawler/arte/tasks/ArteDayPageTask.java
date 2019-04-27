package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.crawler.arte.json.ArteDayPageDeserializer;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryVideosDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ArteDayPageTask extends ArteTaskBase<ArteFilmUrlDto, CrawlerUrlDTO> {

  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
      new TypeToken<ArteSendungOverviewDto>() {}.getType();

  private final int pageNumber;
  private final ArteLanguage language;

  public ArteDayPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final ArteLanguage aLanguage,
      final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(ArteConstants.AUTH_TOKEN));

    registerJsonDeserializer(
        SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteDayPageDeserializer(aLanguage));

    this.pageNumber = aPageNumber;
    this.language = aLanguage;
  }

  public ArteDayPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final ArteLanguage aLanguage) {
    this(aCrawler, aUrlToCrawlDtos, aLanguage, 1);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
        processNextPage(nextPageId.get());
      }
    }
  }

  private void processNextPage(final String aUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aUrl));
    Set<ArteFilmUrlDto> results = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecrusivConverterTask<ArteFilmUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArteDayPageTask(crawler, aElementsToProcess, language, pageNumber + 1);
  }
}
