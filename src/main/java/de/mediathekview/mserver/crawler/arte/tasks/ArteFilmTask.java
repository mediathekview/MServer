package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmDeserializer;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoDetailsDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteFilmTask extends ArteTaskBase<Film, ArteFilmUrlDto> {

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<Film>>() {}.getType();
  private static final Type OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN =
      new TypeToken<Optional<ArteVideoDetailDTO>>() {}.getType();
  private static final Logger LOG = LogManager.getLogger(ArteFilmTask.class);

  private final Sender sender;
  private final LocalDateTime today;

  public ArteFilmTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<ArteFilmUrlDto> aUrlToCrawlDtos,
      final Sender sender,
      final LocalDateTime today) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(ArteConstants.AUTH_TOKEN));
    this.sender = sender;
    this.today = today;

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ArteFilmDeserializer(sender, today));
    registerJsonDeserializer(OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN, new ArteVideoDetailsDeserializer());
  }

  @Override
  protected void processRestTarget(final ArteFilmUrlDto aDTO, final WebTarget aTarget) {
    try {
      final Optional<Film> filmDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);

      if (filmDtoOptional.isPresent()) {

        final Optional<ArteVideoDetailDTO> videoDetailDTO =
            deserializeVideoDetail(aDTO.getVideoDetailsUrl());
        if (videoDetailDTO.isPresent()) {

          final Film result = filmDtoOptional.get();
          addUrls(result, videoDetailDTO.get().getUrls());

          taskResults.add(result);

          crawler.incrementAndGetActualCount();
          crawler.updateProgress();
        } else {
          LOG.error("no video: " + aDTO.getUrl());
          crawler.incrementAndGetErrorCount();
          crawler.updateProgress();
        }
      } else {
        LOG.error("no film: " + aDTO.getUrl());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (final Exception e) {
      LOG.error("exception: " + aDTO.getUrl(), e);
    }
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      try {
        aFilm.addUrl(qualitiesEntry.getKey(), new FilmUrl(qualitiesEntry.getValue()));
      } catch (final MalformedURLException ex) {
        LOG.error("InvalidUrl: " + qualitiesEntry.getValue(), ex);
      }
    }
  }

  private Optional<ArteVideoDetailDTO> deserializeVideoDetail(final CrawlerUrlDTO videoUrl) {
    final WebTarget webTarget = createWebTarget(videoUrl.getUrl());
    return deserializeOptional(webTarget, OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN);
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, ArteFilmUrlDto> createNewOwnInstance(
      final ConcurrentLinkedQueue<ArteFilmUrlDto> aElementsToProcess) {
    return new ArteFilmTask(crawler, aElementsToProcess, sender, today);
  }
}
