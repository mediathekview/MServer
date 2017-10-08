package de.mediathekview.mserver.crawler.funk.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkFilmDeserializer;

public class FunkVideoTask extends AbstractFunkRestTask<Film> {
  private static final long serialVersionUID = 8466909729223013929L;

  public FunkVideoTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, false, aAuthKey);
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new FunkVideoTask(crawler, aURLsToCrawl, authKey);
  }

  @Override
  protected Object getParser() {
    return new FunkFilmDeserializer();
  }

  @Override
  protected Type getType() {
    return Film.class;
  }

  @Override
  protected void postProcessing(final Film aResponseObj, final CrawlerUrlDTO aUrlDTO) {
    crawler.incrementAndGetActualCount();
    crawler.updateProgress();
  }


}
