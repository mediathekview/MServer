package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryVideosDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteSubcategoryVideosTask extends ArteTaskBase<ArteFilmUrlDto, TopicUrlDTO> {

  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
      new TypeToken<ArteSendungOverviewDto>() {}.getType();

  private final int pageNumber;
  private final ArteLanguage language;
  private final String baseUrl;

  public ArteSubcategoryVideosTask(
      final AbstractCrawler crawler,
      final Queue<TopicUrlDTO> urlToCrawlDTOs,
      final String baseUrl,
      final ArteLanguage language,
      final int pageNumber) {
    super(crawler, urlToCrawlDTOs, ArteConstants.AUTH_TOKEN);

    registerJsonDeserializer(
        SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteSubcategoryVideosDeserializer(language));

    this.baseUrl = baseUrl;
    this.pageNumber = pageNumber;
    this.language = language;
  }

  public ArteSubcategoryVideosTask(
      final AbstractCrawler aCrawler,
      final Queue<TopicUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl,
      final ArteLanguage aLanguage) {
    this(aCrawler, aUrlToCrawlDtos, aBaseUrl, aLanguage, 1);
  }

  @Override
  protected void processRestTarget(final TopicUrlDTO aDTO, final WebTarget aTarget) {
    final ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      final Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
        // the nextPageId cannot be used to load the next page because authorization is not valid
        // => build new url
        final String url =
            String.format(
                ArteConstants.URL_SUBCATEGORY_VIDEOS,
                baseUrl,
                language.getLanguageCode().toLowerCase(),
                aDTO.getTopic(),
                pageNumber + 1);
        processNextPage(url, aDTO.getTopic());
      }
    }
  }

  private void processNextPage(final String aUrl, final String aTopic) {
    final Queue<TopicUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new TopicUrlDTO(aTopic, aUrl));
    final Set<ArteFilmUrlDto> results = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecursiveConverterTask<ArteFilmUrlDto, TopicUrlDTO> createNewOwnInstance(
      final Queue<TopicUrlDTO> aElementsToProcess) {
    return new ArteSubcategoryVideosTask(
        crawler, aElementsToProcess, baseUrl, language, pageNumber + 1);
  }
}
