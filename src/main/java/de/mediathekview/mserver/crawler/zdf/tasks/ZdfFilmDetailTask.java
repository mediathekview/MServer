package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDownloadDtoDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfFilmDetailDeserializer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import mServer.crawler.CrawlerTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfFilmDetailTask extends ZdfTaskBase<Film, ZdfEntryDto> {

  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<Film>>() {
  }.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN = new TypeToken<Optional<DownloadDto>>() {
  }.getType();

  private final ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();

  public ZdfFilmDetailTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<ZdfEntryDto> aUrlToCrawlDtos, Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ZdfFilmDetailDeserializer());
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  @Override
  protected void processRestTarget(ZdfEntryDto aDto, WebTarget aTarget) {
    final Optional<Film> film = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);
    final Optional<DownloadDto> downloadDto = deserializeOptional(createWebTarget(aDto.getVideoUrl()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

    if (film.isPresent() && downloadDto.isPresent()) {
      try {
        final Film result = film.get();
        addFilm(downloadDto.get(), result);
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      } catch (MalformedURLException e) {
        LOG.error("ZdfFilmDetailTask: url can't be parsed: ", e);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } else {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, ZdfEntryDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ZdfEntryDto> aElementsToProcess) {
    return new ZdfFilmDetailTask(crawler, aElementsToProcess, authKey);
  }

  private void addFilm(DownloadDto downloadDto, Film result) throws MalformedURLException {
    for (String language : downloadDto.getLanguages()) {

      final Film filmWithLanguage = clone(result);

      addUrlsToFilm(filmWithLanguage, downloadDto, language);

      taskResults.add(filmWithLanguage);
    }
  }

  private static Film clone(final Film aFilm) {
    Film film = new Film(UUID.randomUUID(),
        aFilm.getSender(),
        aFilm.getTitel(),
        aFilm.getThema(),
        aFilm.getTime(),
        aFilm.getDuration());

    film.setBeschreibung(aFilm.getBeschreibung());
    film.setWebsite(aFilm.getWebsite());
    return film;
  }

  private void addUrlsToFilm(final Film aFilm, final DownloadDto aDownloadDto, final String aLanguage) throws MalformedURLException {

    for (final Map.Entry<Resolution, String> qualitiesEntry : aDownloadDto.getDownloadUrls(aLanguage).entrySet()) {
      String url = qualitiesEntry.getValue();

      if (qualitiesEntry.getKey() == Resolution.NORMAL) {
        url = optimizer.getOptimizedUrlNormal(url);
      }

      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(url));
    }

    if (!aFilm.hasHD()) {
      Optional<String> hdUrl = optimizer.determineUrlHd(aFilm.getUrl(Resolution.NORMAL).toString());
      if (hdUrl.isPresent()) {
        aFilm.addUrl(Resolution.HD, CrawlerTool.stringToFilmUrl(hdUrl.get()));
      }
    }

    if (aDownloadDto.getSubTitleUrl().isPresent()) {
      aFilm.addSubtitle(new URL(aDownloadDto.getSubTitleUrl().get()));
    }

    if (aDownloadDto.getGeoLocation().isPresent()) {
      final Collection<GeoLocations> geo = new ArrayList<>();
      geo.add(aDownloadDto.getGeoLocation().get());
      aFilm.setGeoLocations(geo);
    }

    updateTitle(aLanguage, aFilm);
  }

  private static void updateTitle(final String aLanguage, final Film aFilm) {
    String title = aFilm.getTitel();
    switch (aLanguage) {
      case ZdfConstants.LANGUAGE_GERMAN:
        return;
      case ZdfConstants.LANGUAGE_ENGLISH:
        title += " (Englisch)";
        break;
      default:
        title += "(" + aLanguage + ")";
    }

    aFilm.setTitel(title);
  }
}
