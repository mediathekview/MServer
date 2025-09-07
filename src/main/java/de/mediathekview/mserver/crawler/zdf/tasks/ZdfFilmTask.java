package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDownloadDtoDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfFilmTask extends ZdfTaskBase<Film, ZdfFilmDto> {

  private static final Logger LOG = LogManager.getLogger(ZdfFilmTask.class);

  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN =
      new TypeToken<Optional<DownloadDto>>() {}.getType();

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer(crawler);

  public ZdfFilmTask(AbstractCrawler aCrawler, Queue<ZdfFilmDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  private static Film createFilm(final ZdfFilmDto aFilm, final DownloadDto downloadDto)
      throws MalformedURLException {
    final Film film =
        new Film(
            UUID.randomUUID(),
            aFilm.getSender(),
            aFilm.getTitle(),
            aFilm.getTopic(),
            aFilm.getTime(),
            downloadDto.getDuration().orElse(Duration.ZERO));

    film.setBeschreibung(aFilm.getDescription());
    film.setWebsite(URI.create(aFilm.getWebsite()).toURL());

    return film;
  }

  private static void setSubtitle(DownloadDto downloadDto, Film filmWithLanguage, String language)
      throws MalformedURLException {
    final Optional<String> subtitleUrl = downloadDto.getSubTitleUrl(language);
    if (subtitleUrl.isPresent()) {
      filmWithLanguage.addSubtitle(URI.create(subtitleUrl.get()).toURL());
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
  protected void processRestTarget(ZdfFilmDto aDTO, WebTarget aTarget) {
    final Optional<DownloadDto> downloadDto =
        deserialize(aTarget, OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);
    if (downloadDto.isPresent()) {
      try {
        addFilm(downloadDto.get(), createFilm(aDTO, downloadDto.get()), aDTO.getVideoType());
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      } catch (final MalformedURLException e) {
        LOG.error("ZdfFilmTask: url can't be parsed: ", e);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } else {
      LOG.error(
          "ZdfFilmDetailTask: no video {} {} {} in {}",
          aDTO.getSender(),
          aDTO.getTitle(),
          aDTO.getTopic(),
          aDTO);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, ZdfFilmDto> createNewOwnInstance(
      Queue<ZdfFilmDto> aElementsToProcess) {
    return new ZdfFilmTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }

  private void addFilm(final DownloadDto downloadDto, final Film result, String videoType)
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
        } else if (language.endsWith(ZdfConstants.LANGUAGE_SUFFIX_DGS)
            || "DGS".equalsIgnoreCase(videoType)) {
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
        if ("DGS".equalsIgnoreCase(videoType)) {
          urls.forEach(filmWithLanguage::addSignLanguage);
        } else {
          urls.forEach(filmWithLanguage::addUrl);
        }

        if (!taskResults.add(filmWithLanguage)) {
          LOG.error("Rejected duplicate {}", filmWithLanguage);
        }
        previousMainFilm = filmWithLanguage;
        previousLanguage = language;
      }
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
    return result;
  }
}
