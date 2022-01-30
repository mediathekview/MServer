package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryVideosDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import jakarta.ws.rs.client.WebTarget;

import java.lang.reflect.Type;
import java.util.Queue;

public class ArteAllVideosTask extends ArteTaskBase<ArteFilmUrlDto, CrawlerUrlDTO> {
  private static final Type SENDUNG_OVERVIEW_TYPE_TOKEN =
      new TypeToken<ArteSendungOverviewDto>() {}.getType();

  private final ArteLanguage language;

  public ArteAllVideosTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final ArteLanguage language) {
    super(crawler, urlToCrawlDTOs, ArteConstants.AUTH_TOKEN);

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
  protected AbstractRecursiveConverterTask<ArteFilmUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArteAllVideosTask(crawler, aElementsToProcess, language);
  }
}
