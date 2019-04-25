package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkVideosToFilmsTask
    extends AbstractJsonRestTask<Film, PagedElementListDTO<Film>, FilmInfoDto> {
  private static final Logger LOG = LogManager.getLogger(FunkVideosToFilmsTask.class);
  private final Long sessionId;

  public FunkVideosToFilmsTask(
      final AbstractCrawler crawler,
      final Long aSessionId,
      final ConcurrentLinkedQueue<FilmInfoDto> aFilmInfos,
      final Optional<String> authKey) {
    super(crawler, aFilmInfos, authKey);
    sessionId = aSessionId;
  }

  @Override
  protected JsonDeserializer<PagedElementListDTO<Film>> getParser(final FilmInfoDto aDTO) {
    // TODO
    return null;
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<Film>>() {}.getType();
  }

  @Override
  protected void handleHttpError(final URI url, final Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        String.format(
            "A HTTP error %d occurred when getting REST information from: \"%s\".",
            response.getStatus(), url.toString()));
  }

  @Override
  protected void postProcessing(
      final PagedElementListDTO<Film> responseObj, final FilmInfoDto filmInfo) {
    final Optional<String> nextPageLink = responseObj.getNextPage();
    // TODO no supbage
    taskResults.addAll(responseObj.getElements());
  }

  @Override
  protected FunkVideosToFilmsTask createNewOwnInstance(
      final ConcurrentLinkedQueue<FilmInfoDto> aElementsToProcess) {
    return new FunkVideosToFilmsTask(crawler, sessionId, aElementsToProcess, authKey);
  }
}
