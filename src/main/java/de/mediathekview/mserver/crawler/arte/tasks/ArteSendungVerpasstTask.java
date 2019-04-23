package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteCrawlerUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteJsonElementDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmListDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteSendungVerpasstTask
    extends AbstractJsonRestTask<ArteJsonElementDto, ArteFilmListDTO, ArteCrawlerUrlDto> {

  private static final Logger LOG = LogManager.getLogger(ArteSendungVerpasstTask.class);
  private static final long serialVersionUID = 6599845164042820791L;

  public ArteSendungVerpasstTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<ArteCrawlerUrlDto> aUrlToCrawlDTOs,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }

  @Override
  protected AbstractUrlTask<ArteJsonElementDto, ArteCrawlerUrlDto> createNewOwnInstance(
      final ConcurrentLinkedQueue<ArteCrawlerUrlDto> aURLsToCrawl) {
    return new ArteSendungVerpasstTask(crawler, aURLsToCrawl, authKey);
  }

  @Override
  protected Object getParser(final ArteCrawlerUrlDto aDTO) {
    // TODO....
    return new ArteFilmListDeserializer(ArteLanguage.DE);
  }

  @Override
  protected Type getType() {
    return new TypeToken<ArteFilmListDTO>() {}.getType();
  }

  @Override
  protected void handleHttpError(final URI aUrl, final Response aResponse) {
    crawler.printErrorMessage();
    LOG.fatal(
        String.format(
            "A HTTP error %d occured when getting REST informations from: \"%s\".",
            aResponse.getStatus(), aUrl.toString()));
  }

  @Override
  protected void postProcessing(final ArteFilmListDTO responseObj, final ArteCrawlerUrlDto aDTO) {
    final Optional<URI> nextPageLink = responseObj.getNextPage();
    final Optional<AbstractUrlTask<ArteJsonElementDto, ArteCrawlerUrlDto>> subpageCrawler;
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > 0) {
      final ConcurrentLinkedQueue<ArteCrawlerUrlDto> nextPageLinks = new ConcurrentLinkedQueue<>();
      final ArteCrawlerUrlDto arteCrawlerUrlDto =
          new ArteCrawlerUrlDto(nextPageLink.get().toString());
      arteCrawlerUrlDto.setCategory(aDTO.getCategory());
      nextPageLinks.add(arteCrawlerUrlDto);
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }

    taskResults.addAll(responseObj.getFoundFilms());
    crawler.incrementMaxCountBySizeAndGetNewSize(responseObj.getFoundFilms().size());
    crawler.updateProgress();
    if (subpageCrawler.isPresent()) {
      taskResults.addAll(subpageCrawler.get().join());
    }
  }
}
