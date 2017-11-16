package de.mediathekview.mserver.crawler.funk.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.funk.parser.FunkFilmDeserializer;

public class FunkVideoTask extends AbstractFunkRestTask<Film, Optional<Film>, FunkSendungDTO> {
  private static final long serialVersionUID = 8466909729223013929L;

  public FunkVideoTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<FunkSendungDTO> aUrlToCrawlDTOs,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }

  @Override
  protected AbstractUrlTask<Film, FunkSendungDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<FunkSendungDTO> aURLsToCrawl) {
    return new FunkVideoTask(crawler, aURLsToCrawl, authKey);
  }

  @Override
  protected Object getParser(final FunkSendungDTO aDTO) {
    return new FunkFilmDeserializer(aDTO, crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<Optional<Film>>() {}.getType();
  }

  @Override
  protected void postProcessing(final Optional<Film> aResponseObj,
      final FunkSendungDTO aSendungDTO) {
    if (aResponseObj.isPresent()) {
      taskResults.add(aResponseObj.get());
      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    }
  }


}
