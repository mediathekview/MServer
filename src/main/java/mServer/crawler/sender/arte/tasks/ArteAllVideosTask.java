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
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteAllVideosTask extends ArteTaskBase<ArteFilmUrlDto, CrawlerUrlDTO> {
  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
          new TypeToken<ArteSendungOverviewDto>() {
          }.getType();

  private final ArteLanguage language;

  public ArteAllVideosTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs,
          final ArteLanguage language) {
    super(crawler, urlToCrawlDTOs, Optional.of(ArteConstants.AUTH_TOKEN));

    registerJsonDeserializer(
            SENDUNG_OVERVIEW_TYPE_TOKEN, new ArteSubcategoryVideosDeserializer(language));

    this.language = language;
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArteSendungOverviewDto result = deserialize(aTarget, SENDUNG_OVERVIEW_TYPE_TOKEN);
    if (result != null) {
      taskResults.addAll(result.getUrls());
    }
  }

  @Override
  protected AbstractRecursivConverterTask<ArteFilmUrlDto, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArteAllVideosTask(crawler, aElementsToProcess, language);
  }
}
