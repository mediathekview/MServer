package mServer.crawler.sender.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.ArteFilmDto;
import mServer.crawler.sender.arte.ArteFilmUrlDto;
import mServer.crawler.sender.arte.json.ArteFilmDeserializer;
import mServer.crawler.sender.arte.json.ArteVideoDetailsDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArteFilmTask extends ArteTaskBase<DatenFilm, ArteFilmUrlDto> {

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<ArteFilmDto>>() {
  }.getType();
  private static final Type OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN =
          new TypeToken<Optional<ArteVideoDetailDTO>>() {
          }.getType();

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Logger LOG = LogManager.getLogger(ArteFilmTask.class);

  private final LocalDateTime today;

  public ArteFilmTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<ArteFilmUrlDto> urlToCrawlDTOs,
          final LocalDateTime today) {
    super(crawler, urlToCrawlDTOs, Optional.of(ArteConstants.AUTH_TOKEN));
    this.today = today;
  }

  @Override
  protected void processRestTarget(final ArteFilmUrlDto aDTO, final WebTarget aTarget) {
    try {
      final String sender = aDTO.getSender();
      registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ArteFilmDeserializer(sender, today));
      registerJsonDeserializer(
              OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN,
              new ArteVideoDetailsDeserializer(sender));

      final Optional<ArteFilmDto> filmDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);

      if (filmDtoOptional.isPresent()) {
        final Optional<ArteVideoDetailDTO> videoDetailDTO =
                deserializeVideoDetail(aDTO.getVideoDetailsUrl());
        if (videoDetailDTO.isPresent()) {
          final ArteVideoDetailDTO arteVideoDetailDTO = videoDetailDTO.get();
          final ArteFilmDto film = filmDtoOptional.get();

          // update duration if video contains different duration and the difference is larger than 1
          // e.g. trailers has the original film length in film details but the correct trailer length in video details
          // but difference is 1 second, the film length in film details is the correct one
          if (!arteVideoDetailDTO.getDuration().isZero()
                  && arteVideoDetailDTO.getDuration().getSeconds() != (film.getDuration().getSeconds() +1)) {
            film.setDuration(arteVideoDetailDTO.getDuration());
          }

          if (!arteVideoDetailDTO.getUrls().isEmpty()) {
            addFilm(film, arteVideoDetailDTO.getUrls(), "");
          }

          addFilm(film, arteVideoDetailDTO.getUrlsWithSubtitle(), " (mit Untertitel)");
          addFilm(film, arteVideoDetailDTO.getUrlsOriginalWithSubtitle(), " (Originalversion mit Untertitel)");
          addFilm(film, arteVideoDetailDTO.getUrlsAudioDescription(), " (Audiodeskription)");
          addFilm(film, arteVideoDetailDTO.getUrlsOriginal(), " (Originalversion)");
        } else {
          LOG.error("no video: {}", aDTO.getUrl());
        }
      } else {
        LOG.error("no film: {}", aDTO.getUrl());
      }
    } catch (final Exception e) {
      LOG.error("exception: {}", aDTO.getUrl(), e);
      Log.errorLog(421256665, e);
    } finally {
      deregisterJsonDeserializer();
    }
  }

  private void addFilm(ArteFilmDto arteFilm, Map<Qualities, String> videos, final String titleSuffix) {

    if (!videos.containsKey(Qualities.NORMAL)) {
      return;
    }

    String date = arteFilm.getDate().format(DATE_FORMAT);
    String time = arteFilm.getDate().format(TIME_FORMAT);

    DatenFilm film = new DatenFilm(arteFilm.getSender(),
            arteFilm.getTopic(),
            arteFilm.getWebsite(),
            arteFilm.getTitle() + titleSuffix,
            videos.get(Qualities.NORMAL), "" /*urlRtmp*/,
            date, time, arteFilm.getDuration().getSeconds(), arteFilm.getDescription());
    if (videos.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videos.get(Qualities.HD));
    }
    if (videos.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videos.get(Qualities.SMALL));
    }

    if (arteFilm.getGeoLocations() != GeoLocations.GEO_NONE) {
      film.arr[DatenFilm.FILM_GEO] = arteFilm.getGeoLocations().getDescription();
    }

    taskResults.add(film);
  }

  private Optional<ArteVideoDetailDTO> deserializeVideoDetail(final CrawlerUrlDTO videoUrl) {
    final WebTarget webTarget = createWebTarget(videoUrl.getUrl());
    return deserializeOptional(webTarget, OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN);
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, ArteFilmUrlDto> createNewOwnInstance(
          final ConcurrentLinkedQueue<ArteFilmUrlDto> aElementsToProcess) {
    return new ArteFilmTask(crawler, aElementsToProcess, today);
  }
}
