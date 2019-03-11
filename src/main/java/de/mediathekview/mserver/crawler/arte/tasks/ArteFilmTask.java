package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmDeserializer;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoDetailsDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import mServer.crawler.CrawlerTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArteFilmTask extends ArteTaskBase<Film, ArteFilmUrlDto> {

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<Film>>() {
  }.getType();
  private static final Type OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN = new TypeToken<Optional<ArteVideoDetailDTO>>() {
  }.getType();
  private static final Logger LOG = LogManager.getLogger(ArteFilmTask.class);

  private final Sender sender;
  private final LocalDateTime today;

  public ArteFilmTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<ArteFilmUrlDto> aUrlToCrawlDtos,
      Sender sender, LocalDateTime today) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(ArteConstants.AUTH_TOKEN));
    this.sender = sender;
    this.today = today;

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ArteFilmDeserializer(sender, today));
    registerJsonDeserializer(OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN, new ArteVideoDetailsDeserializer());
  }

  @Override
  protected void processRestTarget(ArteFilmUrlDto aDTO, WebTarget aTarget) {
    try {
      final Optional<Film> filmDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);

      if (filmDtoOptional.isPresent()) {

        Optional<ArteVideoDetailDTO> videoDetailDTO = deserializeVideoDetail(aDTO.getVideoDetailsUrl());
        if (videoDetailDTO.isPresent()) {

          Film result = filmDtoOptional.get();
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
    } catch (Exception e) {
      LOG.error("exception: " + aDTO.getUrl(), e);
    }
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      try {
        aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
      } catch (MalformedURLException ex) {
        LOG.error("InvalidUrl: " + qualitiesEntry.getValue(), ex);
      }
    }
  }

  private Optional<ArteVideoDetailDTO> deserializeVideoDetail(CrawlerUrlDTO videoUrl) {
    WebTarget webTarget = createWebTarget(videoUrl.getUrl());
    return deserializeOptional(webTarget, OPTIONAL_VIDEO_DETAILS_TYPE_TOKEN);
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, ArteFilmUrlDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ArteFilmUrlDto> aElementsToProcess) {
    return new ArteFilmTask(crawler, aElementsToProcess, sender, today);
  }
}
