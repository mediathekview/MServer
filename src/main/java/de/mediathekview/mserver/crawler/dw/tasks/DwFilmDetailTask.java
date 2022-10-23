package de.mediathekview.mserver.crawler.dw.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.DWTaskBase;
import de.mediathekview.mserver.crawler.dw.parser.DwFilmDetailDeserializer;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;
import jakarta.ws.rs.client.WebTarget;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;

public class DwFilmDetailTask extends DWTaskBase<Film, CrawlerUrlDTO> {

  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<Film>>() {}.getType();

  public DwFilmDetailTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs, null);

    registerJsonDeserializer(
        OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new DwFilmDetailDeserializer(this.crawler));
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new DwFilmDetailTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<Film> filmDetailDtoOptional =
        deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    if (filmDetailDtoOptional.isEmpty()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }
    this.taskResults.add(filmDetailDtoOptional.get());
    crawler.incrementAndGetActualCount();
    crawler.updateProgress();
  }
}
