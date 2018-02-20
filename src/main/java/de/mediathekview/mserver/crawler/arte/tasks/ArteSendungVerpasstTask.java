package de.mediathekview.mserver.crawler.arte.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmListDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.tasks.AbstractFunkRestTask;

public class ArteSendungVerpasstTask
    extends AbstractFunkRestTask<JsonElement, Set<JsonElement>, CrawlerUrlDTO> {
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
    return new TypeToken<Set<JsonElement>>() {}.getType();
  }

  @Override
  protected void postProcessing(final Set<JsonElement> aResponseObj, final CrawlerUrlDTO aDTO) {
    taskResults.addAll(aResponseObj);
    crawler.getAndSetMaxCount(aResponseObj.size());
    crawler.updateProgress();
  }
}
