package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDownloadDtoDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfFilmDetailDeserializer;
import jakarta.ws.rs.client.WebTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ZdfFilmDetailTask extends ZdfTaskBase<Film, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN =
      new TypeToken<Optional<ZdfFilmDto>>() {}.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN =
      new TypeToken<Optional<DownloadDto>>() {}.getType();

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer(crawler);
  private final String apiUrlBase;
  private final Map<String, Sender> partner2Sender;

  public ZdfFilmDetailTask(
      final AbstractCrawler aCrawler,
      final String aApiUrlBase,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String authKey,
      final Map<String, Sender> partner2Sender) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    apiUrlBase = aApiUrlBase;
    this.partner2Sender = partner2Sender;
    registerJsonDeserializer(
        OPTIONAL_FILM_TYPE_TOKEN, new ZdfFilmDetailDeserializer(apiUrlBase, partner2Sender));
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  private static Film clone(final Film aFilm, final String aLanguage) {
    final Film film =
        new Film(
            UUID.randomUUID(),
            aFilm.getSender(),
            aFilm.getTitel(),
            aFilm.getThema(),
            aFilm.getTime(),
            aFilm.getDuration());

    film.setBeschreibung(aFilm.getBeschreibung());
    film.setWebsite(aFilm.getWebsite().orElse(null));

    updateTitle(aLanguage, film);

    return film;
  }

  private static void updateTitle(final String aLanguage, final Film aFilm) {
    String title = aFilm.getTitel();
    switch (aLanguage) {
      case ZdfConstants.LANGUAGE_GERMAN:
        return;
      case ZdfConstants.LANGUAGE_ENGLISH:
        title += " (Englisch)";
        break;
      case ZdfConstants.LANGUAGE_FRENCH:
        title += " (Französisch)";
        break;
      default:
        title += "(" + aLanguage + ")";
    }

    aFilm.setTitel(title);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDto, final WebTarget aTarget) {
    final Optional<ZdfFilmDto> film = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);
    if (film.isPresent()) {
      final Optional<DownloadDto> downloadDtoOptional =
          deserializeOptional(
              createWebTarget(film.get().getUrl()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

      if (downloadDtoOptional.isPresent()) {
        final DownloadDto downloadDto = downloadDtoOptional.get();
        appendSignLanguage(downloadDto, film.get().getUrlSignLanguage());

        try {
          final Film result = film.get().getFilm();
          if (result.getDuration().isZero() && downloadDto.getDuration().isPresent()) {
            result.setDuration(downloadDto.getDuration().get());
          }
          addFilm(downloadDto, result);

          crawler.incrementAndGetActualCount();
          crawler.updateProgress();
        } catch (final MalformedURLException e) {
          LOG.error("ZdfFilmDetailTask: url can't be parsed: ", e);
          crawler.incrementAndGetErrorCount();
          crawler.updateProgress();
        }
      } else {
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    }
  }

  private void appendSignLanguage(DownloadDto downloadDto, Optional<String> urlSignLanguage) {
    if (urlSignLanguage.isPresent()) {
      final Optional<DownloadDto> downloadSignLanguage =
          deserializeOptional(
              createWebTarget(urlSignLanguage.get()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

      if (downloadSignLanguage.isPresent()) {
        downloadSignLanguage
            .get()
            .getDownloadUrls(ZdfConstants.LANGUAGE_GERMAN)
            .forEach(
                (resolution, url) ->
                    downloadDto.addUrl(ZdfConstants.LANGUAGE_GERMAN_DGS, resolution, url));
      }
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfFilmDetailTask(
        crawler, apiUrlBase, aElementsToProcess, getAuthKey().orElse(null), partner2Sender);
  }

  private void addFilm(final DownloadDto downloadDto, final Film result)
      throws MalformedURLException {

    String previousLanguage = null;
    Film previousMainFilm = null;
    for (final String language : downloadDto.getLanguages().stream().sorted().toList()) {

      if (previousLanguage != null && language.startsWith(previousLanguage)) {
        final Film currentFilm = previousMainFilm;

        if (language.endsWith(ZdfConstants.LANGUAGE_SUFFIX_AD)) {
          final Map<Resolution, FilmUrl> urls =
              getOptimizedUrls(downloadDto.getDownloadUrls(language));
          urls.forEach(currentFilm::addAudioDescription);
        } else if (language.endsWith(ZdfConstants.LANGUAGE_SUFFIX_DGS)) {
          final Map<Resolution, FilmUrl> urls =
              getOptimizedUrls(downloadDto.getDownloadUrls(language));
          urls.forEach(currentFilm::addSignLanguage);
        } else {
          LOG.debug("unknown language suffix: {}", language);
        }
      } else {
        final Film filmWithLanguage = clone(result, language);
        setSubtitle(downloadDto, filmWithLanguage, language);
        setGeoLocation(downloadDto, filmWithLanguage);

        final Map<Resolution, FilmUrl> urls =
            getOptimizedUrls(downloadDto.getDownloadUrls(language));
        urls.forEach(filmWithLanguage::addUrl);

        if (!taskResults.add(filmWithLanguage)) {
          LOG.error("Rejected duplicate {}", filmWithLanguage);
        }
        previousMainFilm = filmWithLanguage;
        previousLanguage = language;
      }
    }
  }

  private static void setSubtitle(DownloadDto downloadDto, Film filmWithLanguage, String language) throws MalformedURLException {
    final Optional<String> subtitleUrl = downloadDto.getSubTitleUrl(language);
    if (subtitleUrl.isPresent()) {
      filmWithLanguage.addSubtitle(new URL(subtitleUrl.get()));
    }
  }

  private static void setGeoLocation(DownloadDto downloadDto, Film filmWithLanguage) {
    final Optional<GeoLocations> geoLocation = downloadDto.getGeoLocation();
    if (geoLocation.isPresent()) {
      final Collection<GeoLocations> geo = new ArrayList<>();
      geo.add(geoLocation.get());
      filmWithLanguage.setGeoLocations(geo);
    }
  }

  private Map<Resolution, FilmUrl> getOptimizedUrls(Map<Resolution, String> urls)
      throws MalformedURLException {
    Map<Resolution, FilmUrl> result = new EnumMap<>(Resolution.class);

    for (final Map.Entry<Resolution, String> qualitiesEntry : urls.entrySet()) {
      String url = qualitiesEntry.getValue();

      if (qualitiesEntry.getKey() == Resolution.NORMAL) {
        url = optimizer.getOptimizedUrlNormal(url);
      } else if (qualitiesEntry.getKey() == Resolution.HD) {
        url = optimizer.getOptimizedUrlHd(url);
      }

      result.put(qualitiesEntry.getKey(), new FilmUrl(url, crawler.determineFileSizeInKB(url)));
    }

    if (!result.containsKey(Resolution.HD)) {
      final Optional<String> hdUrl = optimizer.determineUrlHd(result.get(Resolution.NORMAL));
      if (hdUrl.isPresent()) {
        result.put(
            Resolution.HD, new FilmUrl(hdUrl.get(), crawler.determineFileSizeInKB(hdUrl.get())));
      }
    }
    
    // FIXME
    // old filmlist needs normal url - remove after decom
    if (!result.containsKey(Resolution.NORMAL)) {
      if (result.containsKey(Resolution.SMALL)) {
        result.put(Resolution.NORMAL, result.get(Resolution.SMALL));
        result.remove(Resolution.SMALL);
      } else if (result.containsKey(Resolution.HD)) {
        result.put(Resolution.NORMAL, result.get(Resolution.HD));
        result.remove(Resolution.HD);
      }
    }
    return result;
  }
}
