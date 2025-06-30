package mServer.crawler.sender.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.zdf.*;
import mServer.crawler.sender.zdf.json.DownloadDto;
import mServer.crawler.sender.zdf.json.ZdfDownloadDtoDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfFilmTask extends ZdfTaskBase<DatenFilm, ZdfFilmDto> {

  private static final Logger LOG = LogManager.getLogger(ZdfFilmTask.class);

  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN =
          new TypeToken<Optional<DownloadDto>>() {}.getType();

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();

  public ZdfFilmTask(
          MediathekReader aCrawler, ConcurrentLinkedQueue<ZdfFilmDto> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, Optional.of(authKey));
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  private static DatenFilm createFilm(final ZdfFilmDto zdfFilmDto, final DownloadDto downloadDto, String aLanguage) {
    String title = updateTitle(aLanguage, zdfFilmDto.getTitle());

    LocalDateTime time = zdfFilmDto.getTime();

    String dateValue = time.format(DATE_FORMAT);
    String timeValue = time.format(TIME_FORMAT);

    Map<Qualities, String> downloadUrls = downloadDto.getDownloadUrls(aLanguage);

    Duration duration = downloadDto.getDuration().orElse(Duration.ZERO);

    DatenFilm film = new ZdfDatenFilm(zdfFilmDto.getSender(),
            zdfFilmDto.getTopic(),
            zdfFilmDto.getWebsite(),
            title, downloadUrls.get(Qualities.NORMAL), "", dateValue, timeValue, duration.getSeconds(), zdfFilmDto.getDescription());

    return film;
  }

  @Override
  protected void processRestTarget(ZdfFilmDto aDTO, WebTarget aTarget) {
    final Optional<DownloadDto> downloadDto = deserialize(aTarget, OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);
    if (downloadDto.isPresent()) {
      addFilm(aDTO, downloadDto.get());
    } else {
      LOG.error("ZdfFilmDetailTask: no video {} {} {} in {}",aDTO.getSender(), aDTO.getTitle(), aDTO.getTopic() , aDTO);
      Log.errorLog(453455467, aDTO.getUrl());
    }
  }

  @Override
  protected AbstractRestTask<DatenFilm, ZdfFilmDto> createNewOwnInstance(
      ConcurrentLinkedQueue<ZdfFilmDto> aElementsToProcess) {
    return new ZdfFilmTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }

  private void addFilm(final ZdfFilmDto zdfFilmDto, final DownloadDto downloadDto) {

    String previousLanguage = null;
    DatenFilm previousMainFilm = null;
    for (final String language : downloadDto.getLanguages().stream().sorted().toList()) {
// todo deu-ad bei audiodescription passt nicht im title
      // todo kann der auskommentierte code hier weg?
/*      if (previousLanguage != null && language.startsWith(previousLanguage)) {
        final DatenFilm currentFilm = previousMainFilm;

        if (language.endsWith(ZdfConstants.LANGUAGE_SUFFIX_AD)) {
          final Map<Qualities, String> urls =
                  getOptimizedUrls(downloadDto.getDownloadUrls(language));
          urls.forEach(currentFilm::addAudioDescription);
        } else if (language.endsWith(ZdfConstants.LANGUAGE_SUFFIX_DGS)) {
          final Map<Qualities, String> urls =
                  getOptimizedUrls(downloadDto.getDownloadUrls(language));
          urls.forEach(currentFilm::addSignLanguage);
        } else {
          LOG.debug("unknown language suffix: {}", language);
        }
      } else {*/
        final DatenFilm filmWithLanguage = createFilm(zdfFilmDto, downloadDto, language);
        setSubtitle(downloadDto, filmWithLanguage, language);
        setGeoLocation(downloadDto, filmWithLanguage);

        final Map<Qualities, String> urls =
                getOptimizedUrls(downloadDto.getDownloadUrls(language));
        if (urls.containsKey(Qualities.SMALL)) {
          CrawlerTool.addUrlKlein(filmWithLanguage, urls.get(Qualities.SMALL));
        }
        if (urls.containsKey(Qualities.HD)) {
          CrawlerTool.addUrlHd(filmWithLanguage, urls.get(Qualities.HD));
        }

        if (!taskResults.add(filmWithLanguage)) {
          LOG.error("Rejected duplicate {}", filmWithLanguage);
        }
        previousMainFilm = filmWithLanguage;
        previousLanguage = language;
      //}
    }
  }

  private Map<Qualities, String> getOptimizedUrls(Map<Qualities, String> urls) {
    Map<Qualities, String> result = new EnumMap<>(Qualities.class);

    for (final Map.Entry<Qualities, String> qualitiesEntry : urls.entrySet()) {
      String url = qualitiesEntry.getValue();

      if (qualitiesEntry.getKey() == Qualities.NORMAL) {
        url = optimizer.getOptimizedUrlNormal(url);
      } else if (qualitiesEntry.getKey() == Qualities.HD) {
        url = optimizer.getOptimizedUrlHd(url);
      }

      result.put(qualitiesEntry.getKey(), url);
    }

    if (!result.containsKey(Qualities.HD)) {
      final Optional<String> hdUrl = optimizer.determineUrlHd(result.get(Qualities.NORMAL));
      if (hdUrl.isPresent()) {
        result.put(
                Qualities.HD, hdUrl.get());
      }
    }
    return result;
  }

  private static void setSubtitle(DownloadDto downloadDto, DatenFilm filmWithLanguage, String language) {
    final Optional<String> subtitleUrl = downloadDto.getSubTitleUrl(language);
    if (subtitleUrl.isPresent()) {
      CrawlerTool.addUrlSubtitle(filmWithLanguage, subtitleUrl.get());
    }
  }

  private static void setGeoLocation(DownloadDto downloadDto, DatenFilm filmWithLanguage) {
    final Optional<GeoLocations> geoLocation = downloadDto.getGeoLocation();
    geoLocation.ifPresent(geoLocations -> filmWithLanguage.arr[DatenFilm.FILM_GEO] = geoLocations.getDescription());
  }

  private static String updateTitle(final String aLanguage, final String aTitle) {
    String title = aTitle;
    switch (aLanguage) {
      case ZdfConstants.LANGUAGE_GERMAN:
        break;
      case ZdfConstants.LANGUAGE_ENGLISH:
        title += " (Englisch)";
        break;
      case ZdfConstants.LANGUAGE_FRENCH:
        title += " (Französisch)";
        break;
      default:
        title += "(" + aLanguage + ")";
    }

    return title;
  }
}
