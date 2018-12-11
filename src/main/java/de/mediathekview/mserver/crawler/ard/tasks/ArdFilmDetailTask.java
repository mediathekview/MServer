package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdFilmDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdFilmDetailTask extends ArdTaskBase<Film, ArdFilmInfoDto> {

  private static final Logger LOG = LogManager.getLogger(ArdFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<ArdFilmDto>>() {
  }.getType();

  public ArdFilmDetailTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ArdFilmDeserializer(crawler));
  }

  @Override
  protected void processRestTarget(ArdFilmInfoDto aDTO, WebTarget aTarget) {
    final Optional<ArdFilmDto> filmDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);

    if (filmDtoOptional.isPresent()) {
      ArdFilmDto filmDto = filmDtoOptional.get();

      final Film result = filmDto.getFilm();
      result.setWebsite(getWebsiteUrl(aDTO));
      taskResults.add(result);

      if (aDTO.getNumberOfClips() > 1) {
        processRelatedFilms(filmDto.getRelatedFilms());
      }

      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    } else {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }


  private void processRelatedFilms(final Set<ArdFilmInfoDto> relatedFilms) {
    if (relatedFilms != null && !relatedFilms.isEmpty()) {
      ConcurrentLinkedQueue<ArdFilmInfoDto> queue = new ConcurrentLinkedQueue<>(relatedFilms);
      ArdFilmDetailTask task = (ArdFilmDetailTask) createNewOwnInstance(queue);
      task.fork();
      taskResults.addAll(task.join());
    }
  }

  private Optional<URL> getWebsiteUrl(final ArdFilmInfoDto aDTO) {
    String url = String.format(ArdConstants.WEBSITE_URL, aDTO.getId());
    try {
      return Optional.of(new URL(url));
    } catch (MalformedURLException e) {
      LOG.error(e);
    }
    return Optional.empty();
  }

  @Override
  protected AbstractRecrusivConverterTask createNewOwnInstance(ConcurrentLinkedQueue aElementsToProcess) {
    return new ArdFilmDetailTask(crawler, aElementsToProcess);
  }
}
