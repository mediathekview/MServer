package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.crawler.arte.json.ArteDayPageDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteDayPageTask extends ArteTaskBase<ArteFilmUrlDto, CrawlerUrlDTO> {

  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
      new TypeToken<ArteSendungOverviewDto>() {}.getType();

  private final int pageNumber;
  private final ArteLanguage language;

  public ArteDayPageTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final ArteLanguage language,
      final int pageNumber) {
    super(crawler, urlToCrawlDTOs, ArteConstants.AUTH_TOKEN);

    registerJsonDeserializer(SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteDayPageDeserializer(language));

    this.pageNumber = pageNumber;
    this.language = language;
  }

  public ArteDayPageTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final ArteLanguage aLanguage) {
    this(aCrawler, aUrlToCrawlDtos, aLanguage, 1);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      final Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
        processNextPage(nextPageId.get());
      }
    }
  }

  private void processNextPage(final String aUrl) {
    final Queue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aUrl));
    final Set<ArteFilmUrlDto> results = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecursiveConverterTask<ArteFilmUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArteDayPageTask(crawler, aElementsToProcess, language, pageNumber + 1);
  }
}
