package de.mediathekview.mserver.crawler.ndr.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.GeoLocationGuesser;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.ndr.NdrConstants;
import de.mediathekview.mserver.crawler.ndr.parser.NdrFilmDeserializer;
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

public class NdrSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(NdrSendungsfolgedetailsTask.class);
  private static final long serialVersionUID = 1614807484305273437L;

  private final NdrFilmDeserializer filmDetailDeserializer;
  private final Gson gson;

  public NdrSendungsfolgedetailsTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    filmDetailDeserializer = new NdrFilmDeserializer();
    gson =
        new GsonBuilder()
            .registerTypeAdapter(ArdVideoInfoDto.class, new ArdVideoInfoJsonDeserializer(crawler))
            .create();
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new NdrSendungsfolgedetailsTask(crawler, aUrlsToCrawl);
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
        if (videoInfo.getSubtitleUrl() != null) {
          videoInfo.setSubtitleUrl(
              UrlUtils.addDomainIfMissing(videoInfo.getSubtitleUrl(), NdrConstants.URL_BASE));
        }

        if (!videoInfo.getVideoUrls().isEmpty()) {
          final Film film = createFilm(filmInfoDto, videoInfo);
          taskResults.add(film);
          crawler.incrementAndGetActualCount();
          crawler.updateProgress();
        } else {
          LOG.error("NdrSendungsfolgedetailsTask: film url list is empty " + aUrlDto.getUrl());
          crawler.incrementAndGetErrorCount();
          crawler.updateProgress();
        }

      } catch (final IOException e) {
        LOG.error(
            "NdrSendungsfolgedetailsTask: error reading video infos " + filmInfoDto.getUrl(), e);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }

    } else {
      LOG.info("NdrSendungsfolgedetailsTask: no film found for url " + aUrlDto.getUrl());
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  private Film createFilm(final FilmInfoDto aFilmInfoDto, final ArdVideoInfoDto aVideoInfoDto)
      throws MalformedURLException {
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.NDR,
            aFilmInfoDto.getTitle(),
            aFilmInfoDto.getTopic(),
            aFilmInfoDto.getTime(),
            aFilmInfoDto.getDuration());

    film.setBeschreibung(aFilmInfoDto.getDescription());
    film.setGeoLocations(
        GeoLocationGuesser.getGeoLocations(Sender.NDR, aVideoInfoDto.getDefaultVideoUrl()));
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
      aFilm.addUrl(qualitiesEntry.getKey(), new FilmUrl(qualitiesEntry.getValue(), serialVersionUID));
    }
  }
}
