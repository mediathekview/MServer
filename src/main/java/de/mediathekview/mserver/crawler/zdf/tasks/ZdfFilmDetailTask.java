package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.zdf.DownloadDtoFilmConverter;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDownloadDtoDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfFilmDetailDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfFilmDetailTask extends ZdfTaskBase<Film, ZdfEntryDto> {
  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<Film>>() {}.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN =
      new TypeToken<Optional<DownloadDto>>() {}.getType();

  private final ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();

  public ZdfFilmDetailTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<ZdfEntryDto> aUrlToCrawlDtos,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ZdfFilmDetailDeserializer());
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

  @Override
  protected void processRestTarget(final ZdfEntryDto aDto, final WebTarget aTarget) {
    final Optional<Film> film = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);
    final Optional<DownloadDto> downloadDto =
        deserializeOptional(createWebTarget(aDto.getVideoUrl()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

    if (film.isPresent() && downloadDto.isPresent()) {
      try {
        final Film result = film.get();
        addFilm(downloadDto.get(), result);

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

  @Override
  protected AbstractRecrusivConverterTask<Film, ZdfEntryDto> createNewOwnInstance(
      final ConcurrentLinkedQueue<ZdfEntryDto> aElementsToProcess) {
    return new ZdfFilmDetailTask(crawler, aElementsToProcess, authKey);
  }

  private void addFilm(final DownloadDto downloadDto, final Film result)
      throws MalformedURLException {
    for (final String language : downloadDto.getLanguages()) {

      final Film filmWithLanguage = clone(result, language);

      DownloadDtoFilmConverter.addUrlsToFilm(
          filmWithLanguage, downloadDto, Optional.of(optimizer), language);

      taskResults.add(filmWithLanguage);
    }
  }

  private static void updateTitle(final String aLanguage, final Film aFilm) {
    String title = aFilm.getTitel();
    switch (aLanguage) {
      case ZdfConstants.LANGUAGE_GERMAN:
        return;
      case ZdfConstants.LANGUAGE_GERMAN_AD:
        title += " (Audiodeskription)";
        break;
      case ZdfConstants.LANGUAGE_ENGLISH:
        title += " (Englisch)";
        break;
      default:
        title += "(" + aLanguage + ")";
    }

    aFilm.setTitel(title);
  }
}
