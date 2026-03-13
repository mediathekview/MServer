package de.mediathekview.mserver.crawler.dw.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.DWTaskBase;
import de.mediathekview.mserver.crawler.dw.parser.DwFilmDetailDeserializer;
import jakarta.ws.rs.client.WebTarget;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class DwFilmDetailTask extends DWTaskBase<Film, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(DwFilmDetailTask.class);
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
    Optional<Film> filmDetailDtoOptional = Optional.empty();
    try {
      filmDetailDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    } catch (Exception e) {
      LOG.error("error processing {} ", aDTO.getUrl(), e);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
    // Optional can be null if response code is 200 and response body is empty
    if (filmDetailDtoOptional == null|| filmDetailDtoOptional.isEmpty()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }
    if (!this.taskResults.add(filmDetailDtoOptional.get())) {
      crawler.incrementAndGetErrorCount();
      LOG.warn("Entry was rejected because existing {}", filmDetailDtoOptional.get());
    } else {
      crawler.incrementAndGetActualCount();
    }
    crawler.updateProgress();
  }
}
