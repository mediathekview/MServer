package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.FileSizeDeterminer;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmDeserializer;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoDetailsDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class ArteFilmTask extends ArteTaskBase<Film, ArteFilmUrlDto> {

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<Film>>() {}.getType();
  private static final Type OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN =
      new TypeToken<Optional<ArteVideoDetailDTO>>() {}.getType();
  private static final Logger LOG = LogManager.getLogger(ArteFilmTask.class);

  private final Sender sender;
  private final LocalDateTime today;

  public ArteFilmTask(
      final AbstractCrawler crawler,
      final Queue<ArteFilmUrlDto> urlToCrawlDTOs,
      final Sender sender,
      final LocalDateTime today) {
    super(crawler, urlToCrawlDTOs, ArteConstants.AUTH_TOKEN);
    this.sender = sender;
    this.today = today;

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ArteFilmDeserializer(sender, today));
    registerJsonDeserializer(
        OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN,
        new ArteVideoDetailsDeserializer(this.crawler.getSender()));
  }

  @Override
  protected void processRestTarget(final ArteFilmUrlDto aDTO, final WebTarget aTarget) {
    try {
      final Optional<Film> filmDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);

      if (filmDtoOptional.isPresent()) {
        final Optional<ArteVideoDetailDTO> videoDetailDTO =
            deserializeVideoDetail(aDTO.getVideoDetailsUrl());
        if (videoDetailDTO.isPresent()) {
          final ArteVideoDetailDTO arteVideoDetailDTO = videoDetailDTO.get();
          final Film film = filmDtoOptional.get();

          addFilm(film, arteVideoDetailDTO.getUrls());

          addSpecialFilm(film, arteVideoDetailDTO.getUrlsWithSubtitle(), " (mit Untertitel)");
          addSpecialFilm(film, arteVideoDetailDTO.getUrlsAudioDescription(), " (Audiodeskription)");

          crawler.incrementAndGetActualCount();
        } else {
          LOG.error("no video: " + aDTO.getUrl());
          crawler.incrementAndGetErrorCount();
        }
      } else {
        LOG.error("no film: " + aDTO.getUrl());
        crawler.incrementAndGetErrorCount();
      }
      crawler.updateProgress();
    } catch (final Exception e) {
      LOG.error("exception: " + aDTO.getUrl(), e);
    }
  }

  private void addSpecialFilm(
      final Film film, final Map<Resolution, String> urls, final String titleSuffix) {
    if (!urls.isEmpty()) {
      final Film specialFilm =
          new Film(
              UUID.randomUUID(),
              film.getSender(),
              film.getTitel() + titleSuffix,
              film.getThema(),
              film.getTime(),
              film.getDuration());
      specialFilm.setBeschreibung(film.getBeschreibung());
      specialFilm.setGeoLocations(film.getGeoLocations());
      specialFilm.setWebsite(film.getWebsite().orElse(null));
      addFilm(specialFilm, urls);
    }
  }

  private void addFilm(final Film film, final Map<Resolution, String> urls) {
    addUrls(film, urls);
    taskResults.add(film);
  }

  private void addUrls(final Film film, final Map<Resolution, String> videoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      final String url = qualitiesEntry.getValue();
      try {
        film.addUrl(
            qualitiesEntry.getKey(),
            new FilmUrl(url, new FileSizeDeterminer(url).getFileSizeInMiB()));
      } catch (final MalformedURLException ex) {
        LOG.error("InvalidUrl: {}", url, ex);
      }
    }
  }

  private Optional<ArteVideoDetailDTO> deserializeVideoDetail(final CrawlerUrlDTO videoUrl) {
    final WebTarget webTarget = createWebTarget(videoUrl.getUrl());
    return deserializeOptional(webTarget, OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN);
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, ArteFilmUrlDto> createNewOwnInstance(
      final Queue<ArteFilmUrlDto> aElementsToProcess) {
    return new ArteFilmTask(crawler, aElementsToProcess, sender, today);
  }
}
