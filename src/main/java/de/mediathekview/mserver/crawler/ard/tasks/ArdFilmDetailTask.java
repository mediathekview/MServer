package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdFilmDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArdFilmDetailTask extends ArdTaskBase<Film, ArdFilmInfoDto> {

  private static final Logger LOG = LogManager.getLogger(ArdFilmDetailTask.class);

  private static final Type LIST_FILM_TYPE_TOKEN = new TypeToken<List<ArdFilmDto>>() {}.getType();

  public ArdFilmDetailTask(
      final AbstractCrawler aCrawler, final Queue<ArdFilmInfoDto> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    registerJsonDeserializer(LIST_FILM_TYPE_TOKEN, new ArdFilmDeserializer(crawler));
  }

  @Override
  protected void processRestTarget(final ArdFilmInfoDto aDTO, final WebTarget aTarget) {
    try {
      final List<ArdFilmDto> filmDtos = deserialize(aTarget, LIST_FILM_TYPE_TOKEN, aDTO);

      if (filmDtos == null || filmDtos.isEmpty()) {
        LOG.error("no film: {}", aDTO.getUrl());
        crawler.incrementAndGetErrorCount();
      } else {
        /* Increase the max count because before we counted the shows and on the deserializing we get more then one film
         * for one show. This is because we find related films in the document as well. The minus 1 because we already
         * counted for the show itself.
         */
        crawler.incrementMaxCountBySizeAndGetNewSize(filmDtos.size() - 1L);

        for (final ArdFilmDto filmDto : filmDtos) {

          final Film result = filmDto.getFilm();
          result.setWebsite(getWebsiteUrl(aDTO).orElse(null));
          taskResults.add(result);

          if (aDTO.getNumberOfClips() > 1) {
            processRelatedFilms(filmDto.getRelatedFilms());
          }
        }
        crawler.incrementAndGetActualCount();
      }
      crawler.updateProgress();
      // TODO Find concrete exceptions and handle them
    } catch (final Exception exception) {
      LOG.error("exception: {}", aDTO.getUrl(), exception);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  private void processRelatedFilms(final Set<ArdFilmInfoDto> relatedFilms) {
    if (relatedFilms != null && !relatedFilms.isEmpty()) {
      final Queue<ArdFilmInfoDto> queue = new ConcurrentLinkedQueue<>(relatedFilms);
      final ArdFilmDetailTask task = (ArdFilmDetailTask) createNewOwnInstance(queue);
      task.fork();
      taskResults.addAll(task.join());
    }
  }

  private Optional<URL> getWebsiteUrl(final ArdFilmInfoDto aDTO) {
    final String url = String.format(ArdConstants.WEBSITE_URL, aDTO.getId());
    try {
      return Optional.of(new URL(url));
    } catch (final MalformedURLException e) {
      LOG.error(e);
    }
    return Optional.empty();
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, ArdFilmInfoDto> createNewOwnInstance(
      final Queue<ArdFilmInfoDto> aElementsToProcess) {
    return new ArdFilmDetailTask(crawler, aElementsToProcess);
  }
}
