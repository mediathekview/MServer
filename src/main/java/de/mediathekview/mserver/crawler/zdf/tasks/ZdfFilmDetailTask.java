package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.DownloadDtoFilmConverter;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
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
import java.util.Queue;
import java.util.UUID;

public class ZdfFilmDetailTask extends ZdfTaskBase<Film, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN =
      new TypeToken<Optional<ZdfFilmDto>>() {}.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN =
      new TypeToken<Optional<DownloadDto>>() {}.getType();

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer(crawler);
  private final String apiUrlBase;

  public ZdfFilmDetailTask(
      final AbstractCrawler aCrawler,
      final String aApiUrlBase,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    apiUrlBase = aApiUrlBase;

    registerJsonDeserializer(
        OPTIONAL_FILM_TYPE_TOKEN, new ZdfFilmDetailDeserializer(apiUrlBase, aCrawler.getSender()));
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
      case ZdfConstants.LANGUAGE_GERMAN_AD:
        title += " (Audiodeskription)";
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

    aFilm.setTitel(title);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDto, final WebTarget aTarget) {
    final Optional<ZdfFilmDto> film = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);
    if (film.isPresent()) {
      final Optional<DownloadDto> downloadDto =
          deserializeOptional(
              createWebTarget(film.get().getUrl()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

      if (downloadDto.isPresent()) {
        try {
          final Film result = film.get().getFilm();
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
    } else {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfFilmDetailTask(
        crawler, apiUrlBase, aElementsToProcess, getAuthKey().orElse(null));
  }

  private void addFilm(final DownloadDto downloadDto, final Film result)
      throws MalformedURLException {
    for (final String language : downloadDto.getLanguages()) {

      final Film filmWithLanguage = clone(result, language);

      DownloadDtoFilmConverter.addUrlsToFilm(
          crawler, filmWithLanguage, downloadDto, Optional.of(optimizer), language);

      taskResults.add(filmWithLanguage);
    }
  }
}
