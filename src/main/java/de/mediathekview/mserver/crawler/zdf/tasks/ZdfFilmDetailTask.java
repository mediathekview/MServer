package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ZdfFilmDetailTask extends AbstractRestTask<Film, ZdfEntryDto> {

  public ZdfFilmDetailTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<ZdfEntryDto> aUrlToCrawlDTOs, Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }

  @Override
  protected void processRestTarget(ZdfEntryDto aDTO, WebTarget aTarget) {

  }

  @Override
  protected AbstractRecrusivConverterTask<Film, ZdfEntryDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ZdfEntryDto> aElementsToProcess) {
    return new ZdfFilmDetailTask(crawler, aElementsToProcess, authKey);
  }
}
