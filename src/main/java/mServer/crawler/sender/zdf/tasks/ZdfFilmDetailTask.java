package mServer.crawler.sender.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.zdf.*;
import mServer.crawler.sender.zdf.json.DownloadDto;
import mServer.crawler.sender.zdf.json.ZdfDownloadDtoDeserializer;
import mServer.crawler.sender.zdf.json.ZdfFilmDetailDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfFilmDetailTask extends ZdfTaskBase<DatenFilm, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN
    = new TypeToken<Optional<ZdfFilmDtoOld>>() {
  }.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN
    = new TypeToken<Optional<DownloadDto>>() {
  }.getType();

  private static final DateTimeFormatter DATE_FORMAT
    = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
    = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();
  private final String apiUrlBase;
  private final Map<String, String> partnerToSender;

  public ZdfFilmDetailTask(
    final MediathekReader aCrawler,
    final String aApiUrlBase,
    final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
    final Optional<String> aAuthKey,
    final Map<String, String> partnerToSender) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);
    apiUrlBase = aApiUrlBase;
    this.partnerToSender = partnerToSender;

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ZdfFilmDetailDeserializer(apiUrlBase, partnerToSender));
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDto, final WebTarget aTarget) {
    if (Config.getStop()) {
      return;
    }

    try {
      final Optional<ZdfFilmDtoOld> film = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);
      if (film.isPresent()) {
        final Optional<DownloadDto> downloadDtoOptional
          = deserializeOptional(
          createWebTarget(film.get().getUrl()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

        if (downloadDtoOptional.isPresent()) {
          final DownloadDto downloadDto = downloadDtoOptional.get();
          appendSignLanguage(downloadDto, film.get().getUrlSignLanguage());

          final ZdfFilmDtoOld result = film.get();
          addFilm(downloadDto, result);
        }
      }
    } catch (Exception e) {
      LOG.error("exception: {}", aDto.getUrl(), e);
      Log.errorLog(453455465, e, aDto.getUrl());
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
  protected AbstractRecursivConverterTask<DatenFilm, CrawlerUrlDTO> createNewOwnInstance(
    final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfFilmDetailTask(crawler, apiUrlBase, aElementsToProcess, authKey, partnerToSender);
  }

  private void addFilm(final DownloadDto downloadDto, final ZdfFilmDtoOld result) {
    for (final String language : downloadDto.getLanguages()) {

      if (downloadDto.getUrl(language, Qualities.NORMAL).isPresent()) {
        DownloadDtoFilmConverter.getOptimizedUrls(
          downloadDto.getDownloadUrls(language), Optional.of(optimizer));

        final DatenFilm filmWithLanguage = createFilm(result, downloadDto, language);
        taskResults.add(filmWithLanguage);
      }
    }
  }

  private static String updateTitle(final String aLanguage, final String aTitle) {
    String title = aTitle;
    switch (aLanguage) {
      case ZdfConstants.LANGUAGE_GERMAN:
        return title;
      case ZdfConstants.LANGUAGE_GERMAN_AD:
        title += " (Audiodeskription)";
        break;
      case ZdfConstants.LANGUAGE_GERMAN_DGS:
        title += " (Gebärdensprache)";
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

  private DatenFilm createFilm(final ZdfFilmDtoOld zdfFilmDto, final DownloadDto downloadDto, final String aLanguage) {

    final String title = updateTitle(aLanguage, zdfFilmDto.getTitle());

    LocalDateTime time = zdfFilmDto.getTime().orElse(LocalDateTime.now());

    String dateValue = time.format(DATE_FORMAT);
    String timeValue = time.format(TIME_FORMAT);

    Map<Qualities, String> downloadUrls = downloadDto.getDownloadUrls(aLanguage);

    Duration duration = zdfFilmDto.getDuration().orElse(downloadDto.getDuration().orElse(Duration.ZERO));

    DatenFilm film = new ZdfDatenFilm(zdfFilmDto.getSender(),
      zdfFilmDto.getTopic().orElse(title),
      zdfFilmDto.getWebsite().orElse(""),
      title, downloadUrls.get(Qualities.NORMAL), "", dateValue, timeValue, duration.getSeconds(), zdfFilmDto.getDescription().orElse(""));
    if (downloadUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, downloadUrls.get(Qualities.SMALL));
    }
    if (downloadUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, downloadUrls.get(Qualities.HD));
    }
    final Optional<String> subTitleUrl = downloadDto.getSubTitleUrl(aLanguage);
    if (subTitleUrl.isPresent()) {
      CrawlerTool.addUrlSubtitle(film, subTitleUrl.get());
    }

    final Optional<GeoLocations> geoLocation = downloadDto.getGeoLocation();
    geoLocation.ifPresent(geoLocations -> film.arr[DatenFilm.FILM_GEO] = geoLocations.getDescription());
    return film;
  }
}
