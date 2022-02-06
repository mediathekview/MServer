package mServer.crawler.sender.arte.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.ArteFilmUrlDto;
import mServer.crawler.sender.arte.ArteLanguage;
import mServer.crawler.sender.arte.ArteSendungOverviewDto;
import mServer.crawler.sender.arte.json.ArteDayPageDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteDayPageTask extends ArteTaskBase<ArteFilmUrlDto, CrawlerUrlDTO> {

  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
          new TypeToken<ArteSendungOverviewDto>() {
          }.getType();

  private final int pageNumber;
  private final ArteLanguage language;

  public ArteDayPageTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs,
          final ArteLanguage language,
          final int pageNumber) {
    super(crawler, urlToCrawlDTOs, Optional.of(ArteConstants.AUTH_TOKEN));

    registerJsonDeserializer(SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteDayPageDeserializer(language));

    this.pageNumber = pageNumber;
    this.language = language;
  }

  public ArteDayPageTask(
          final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
          final ArteLanguage aLanguage) {
    this(aCrawler, aUrlToCrawlDtos, aLanguage, 1);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      final Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent()) {
        processNextPage(nextPageId.get());
      }
    }
  }

  private void processNextPage(final String aUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aUrl));
    final Set<ArteFilmUrlDto> results = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecursivConverterTask<ArteFilmUrlDto, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArteDayPageTask(crawler, aElementsToProcess, language, pageNumber + 1);
  }
}
