package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.base.utils.UrlParseException;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryVideosDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

import jakarta.ws.rs.client.WebTarget;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteSubcategoryVideosTask extends ArteTaskBase<ArteFilmUrlDto, TopicUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArteSubcategoryVideosTask.class);
  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
      new TypeToken<ArteSendungOverviewDto>() {}.getType();

  private final ArteLanguage language;
  private final String baseUrl;

  public ArteSubcategoryVideosTask(
      final AbstractCrawler crawler,
      final Queue<TopicUrlDTO> urlToCrawlDTOs,
      final String baseUrl,
      final ArteLanguage language) {
    super(crawler, urlToCrawlDTOs, ArteConstants.AUTH_TOKEN);

    registerJsonDeserializer(
        SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteSubcategoryVideosDeserializer(language));

    this.baseUrl = baseUrl;
    this.language = language;
  }

  @Override
  protected void processRestTarget(final TopicUrlDTO aDTO, final WebTarget aTarget) {
    final ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());
      //
      int nextPageId = 0;
      try {
        nextPageId = Integer.parseInt(UrlUtils.getUrlParameterValue(aDTO.getUrl(),"page").get());
      } catch (UrlParseException|NumberFormatException e) {
        LOG.error("Failed to parse page from url {} error {}",aDTO.getUrl(), e.getMessage());
      }
      if (result.getUrls().size() == ArteConstants.SUBCATEGORY_LIMIT 
          && nextPageId < crawler.getCrawlerConfig().getMaximumSubpages()) {
        // the nextPageId cannot be used to load the next page because authorization is not valid
        // => build new url
        final String url =
            String.format(
                ArteConstants.URL_SUBCATEGORY_VIDEOS,
                baseUrl,
                language.getLanguageCode().toLowerCase(),
                aDTO.getTopic(),
                nextPageId + 1);
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
        crawler, aElementsToProcess, baseUrl, language);
  }
}
