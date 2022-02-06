package mServer.crawler.sender.arte.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.ArteFilmUrlDto;
import mServer.crawler.sender.arte.ArteLanguage;
import mServer.crawler.sender.arte.ArteSendungOverviewDto;
import mServer.crawler.sender.arte.json.ArteSubcategoryVideosDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.TopicUrlDTO;
import mServer.crawler.sender.base.UrlParseException;
import mServer.crawler.sender.base.UrlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteSubcategoryVideosTask extends ArteTaskBase<ArteFilmUrlDto, TopicUrlDTO> {
  private static final int MAXIMUM_SUBPAGES = 1;

  private static final Logger LOG = LogManager.getLogger(ArteSubcategoryVideosTask.class);
  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
          new TypeToken<ArteSendungOverviewDto>() {
          }.getType();

  private final ArteLanguage language;
  private final String baseUrl;

  public ArteSubcategoryVideosTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<TopicUrlDTO> urlToCrawlDTOs,
          final String baseUrl,
          final ArteLanguage language) {
    super(crawler, urlToCrawlDTOs, Optional.of(ArteConstants.AUTH_TOKEN));

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
        nextPageId = Integer.parseInt(UrlUtils.getUrlParameterValue(aDTO.getUrl(), "page").get());
      } catch (UrlParseException | NumberFormatException e) {
        LOG.error("Failed to parse page from url {} error {}", aDTO.getUrl(), e.getMessage());
      }
      if (result.getUrls().size() == ArteConstants.SUBCATEGORY_LIMIT
              && nextPageId < MAXIMUM_SUBPAGES) {
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
    final ConcurrentLinkedQueue<TopicUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new TopicUrlDTO(aTopic, aUrl));
    final Set<ArteFilmUrlDto> results = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(results);
  }

  @Override
  protected AbstractRecursivConverterTask<ArteFilmUrlDto, TopicUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new ArteSubcategoryVideosTask(
            crawler, aElementsToProcess, baseUrl, language);
  }
}
