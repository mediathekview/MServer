package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryVideosDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ArteSubcategoryVideosTask extends ArteTaskBase<ArteFilmUrlDto, TopicUrlDTO> {

  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN = new TypeToken<ArteSendungOverviewDto>() {
  }.getType();

  private final int pageNumber;
  private final ArteLanguage language;
  private final String baseUrl;

  public ArteSubcategoryVideosTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl,
      final ArteLanguage aLanguage, final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(ArteConstants.AUTH_TOKEN));

    registerJsonDeserializer(SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteSubcategoryVideosDeserializer(aLanguage));

    baseUrl = aBaseUrl;
    this.pageNumber = aPageNumber;
    this.language = aLanguage;
  }

  public ArteSubcategoryVideosTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos, final String aBaseUrl, final ArteLanguage aLanguage) {
    this(aCrawler, aUrlToCrawlDtos, aBaseUrl, aLanguage, 1);
  }

  @Override
  protected void processRestTarget(TopicUrlDTO aDTO, WebTarget aTarget) {
    ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());

      Optional<String> nextPageId = result.getNextPageId();
      if (nextPageId.isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
        // the nextPageId cannot be used to load the next page because authorization is not valid
        // => build new url
        String url = String.format(ArteConstants.URL_SUBCATEGORY_VIDEOS, baseUrl, language.getLanguageCode().toLowerCase(), aDTO.getTopic(),
            pageNumber + 1);
        processNextPage(url, aDTO.getTopic());
      }
    }
  }

  private void processNextPage(final String aUrl, final String aTopic) {
    final ConcurrentLinkedQueue<TopicUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new TopicUrlDTO(aTopic, aUrl));
    Set<ArteFilmUrlDto> results = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecrusivConverterTask<ArteFilmUrlDto, TopicUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new ArteSubcategoryVideosTask(crawler, aElementsToProcess, baseUrl, language, pageNumber + 1);
  }
}
