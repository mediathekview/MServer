package de.mediathekview.mserver.crawler.arte.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmListDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.tasks.AbstractFunkRestTask;

public class ArteSendungVerpasstTask
    extends AbstractFunkRestTask<JsonElement, ArteFilmListDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = 6599845164042820791L;
  private static final String AUTH_TOKEN =
      "Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";

  public ArteSendungVerpasstTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.of(AUTH_TOKEN));
  }

  @Override
  protected AbstractUrlTask<JsonElement, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new ArteSendungVerpasstTask(crawler, aURLsToCrawl);
  }

  @Override
  protected Object getParser(final CrawlerUrlDTO aDTO) {
    return new ArteFilmListDeserializer(crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<ArteFilmListDTO>() {}.getType();
  }

  @Override
  protected void postProcessing(final ArteFilmListDTO responseObj, final CrawlerUrlDTO aDTO) {
    final Optional<URI> nextPageLink = responseObj.getNextPage();
    Optional<AbstractUrlTask<JsonElement, CrawlerUrlDTO>> subpageCrawler;
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > 0) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new CrawlerUrlDTO(nextPageLink.get().toString()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }

    taskResults.addAll(responseObj.getFoundFilms());
    crawler.getAndSetMaxCount(responseObj.getFoundFilms().size());
    crawler.updateProgress();
    if (subpageCrawler.isPresent()) {
      taskResults.addAll(subpageCrawler.get().join());
    }
  }
}
