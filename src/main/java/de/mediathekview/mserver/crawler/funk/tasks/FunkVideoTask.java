package de.mediathekview.mserver.crawler.funk.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.funk.parser.FunkFilmDeserializer;

public class FunkVideoTask extends AbstractFunkRestTask<Film, Optional<Film>, FunkSendungDTO> {
  private static final Logger LOG = LogManager.getLogger(FunkVideoTask.class);
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
  protected void handleHttpError(final URI aUrl, final Response aResponse) {
    crawler.incrementAndGetErrorCount();
    crawler.printErrorMessage();
    LOG.fatal(String.format("A HTTP error %d occured when getting REST informations from: \"%s\".",
        aResponse.getStatus(), aUrl.toString()));
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
