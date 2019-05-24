package de.mediathekview.mserver.crawler.rbb.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.GeoLocationGuesser;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.rbb.parser.RbbFilmDetailDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RbbFilmTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(RbbFilmTask.class);

  private final RbbFilmDetailDeserializer filmDetailDeserializer;
  private final Gson gson;
  private final String baseUrl;

  public RbbFilmTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDtos);

    baseUrl = aBaseUrl;
    filmDetailDeserializer = new RbbFilmDetailDeserializer(baseUrl);
    gson =
        new GsonBuilder()
            .registerTypeAdapter(ArdVideoInfoDto.class, new ArdVideoInfoJsonDeserializer(crawler))
            .create();
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    final Optional<FilmInfoDto> filmInfo = filmDetailDeserializer.deserialize(aUrlDto, aDocument);
    if (filmInfo.isPresent()) {
      final FilmInfoDto filmInfoDto = filmInfo.get();
      try {
        final ArdVideoInfoDto videoInfo =
            gson.fromJson(
                new InputStreamReader(
                    new URL(filmInfoDto.getUrl()).openStream(), StandardCharsets.UTF_8),
                ArdVideoInfoDto.class);

        final Film film = createFilm(filmInfoDto, videoInfo);
        taskResults.add(film);
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();

      } catch (final IOException e) {
        LOG.error("RbbFilmTask: error reading video infos " + filmInfoDto.getUrl(), e);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }

    } else {
      LOG.error("RbbFilmTask: no film found for url " + aUrlDto.getUrl());
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new RbbFilmTask(crawler, aElementsToProcess, baseUrl);
  }

  private Film createFilm(final FilmInfoDto aFilmInfoDto, final ArdVideoInfoDto aVideoInfoDto)
      throws MalformedURLException {
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.RBB,
            aFilmInfoDto.getTitle(),
            aFilmInfoDto.getTopic(),
            aFilmInfoDto.getTime(),
            aFilmInfoDto.getDuration());

    film.setBeschreibung(aFilmInfoDto.getDescription());
    try {
      film.setGeoLocations(
          GeoLocationGuesser.getGeoLocations(Sender.RBB, aVideoInfoDto.getDefaultVideoUrl()));
    } catch (final NullPointerException e) {
      e.printStackTrace();
    }
    film.setWebsite(new URL(aFilmInfoDto.getWebsite()));
    if (StringUtils.isNotBlank(aVideoInfoDto.getSubtitleUrl())) {
      film.addSubtitle(new URL(aVideoInfoDto.getSubtitleUrl()));
    }
    addUrls(film, aVideoInfoDto.getVideoUrls());
    return film;
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls)
      throws MalformedURLException {
    for (final Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), new FilmUrl(qualitiesEntry.getValue()));
    }
  }
}
