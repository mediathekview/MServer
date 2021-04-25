package de.mediathekview.mserver.crawler.br.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractGraphQlTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.br.BrClipQueryDto;
import de.mediathekview.mserver.crawler.br.json.BrClipDetailsDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;

public class BrGetClipDetailsTask
    extends AbstractGraphQlTask<Film, Optional<Film>, BrClipQueryDto> {

  private static final Logger LOG = LogManager.getLogger(BrGetClipDetailsTask.class);
  private static final Type OPTIONAL_FILM_TYPE = new TypeToken<Optional<Film>>() {}.getType();

  public BrGetClipDetailsTask(
      final AbstractCrawler crawler, final Queue<BrClipQueryDto> clipQueue) {
    super(crawler, clipQueue, null);
  }

  @Override
  protected Object getParser(BrClipQueryDto aDTO) {
    return new BrClipDetailsDeserializer(crawler.getSender(), aDTO.getId());
  }

  @Override
  protected Type getType() {
    return OPTIONAL_FILM_TYPE;
  }

  @Override
  protected void handleHttpError(final BrClipQueryDto dto, final URI url, final Response response) {
    crawler.printErrorMessage();
    LOG.error("HTTP error {}: id: {}", response.getStatus(), dto.getId().getId());
    crawler.incrementAndGetErrorCount();
  }

  @Override
  protected void postProcessing(Optional<Film> aResponseObj, BrClipQueryDto aDTO) {
    if (aResponseObj.isPresent()) {
      taskResults.add(aResponseObj.get());
      crawler.incrementAndGetActualCount();
    } else {
      LOG.error("no film object received: {}", aDTO.getId().getId());
      crawler.incrementAndGetErrorCount();
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, BrClipQueryDto> createNewOwnInstance(
      Queue<BrClipQueryDto> aElementsToProcess) {
    return new BrGetClipDetailsTask(crawler, aElementsToProcess);
  }
}
