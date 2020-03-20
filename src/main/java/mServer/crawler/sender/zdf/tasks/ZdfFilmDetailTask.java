package mServer.crawler.sender.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.ZdfFilmDto;
import mServer.crawler.sender.zdf.ZdfVideoUrlOptimizer;
import mServer.crawler.sender.zdf.json.DownloadDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfFilmDetailTask extends ZdfTaskBase<DatenFilm, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_TYPE_TOKEN
          = new TypeToken<Optional<ZdfFilmDto>>() {
          }.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN
          = new TypeToken<Optional<DownloadDto>>() {
          }.getType();

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();
  private final String apiUrlBase;

  public ZdfFilmDetailTask(
          final MediathekReader aCrawler,
          final String aApiUrlBase,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
          final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);
    apiUrlBase = aApiUrlBase;

    registerJsonDeserializer(OPTIONAL_FILM_TYPE_TOKEN, new ZdfFilmDetailDeserializer(apiUrlBase));
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  private static Film clone(final Film aFilm, final String aLanguage) {
    final Film film
            = new Film(
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
  protected void processRestTarget(final CrawlerUrlDTO aDto, final WebTarget aTarget) {
    final Optional<ZdfFilmDto> film = deserializeOptional(aTarget, OPTIONAL_FILM_TYPE_TOKEN);
    if (film.isPresent()) {
      final Optional<DownloadDto> downloadDto
              = deserializeOptional(
                      createWebTarget(film.get().getUrl()), OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);

      if (downloadDto.isPresent()) {
        try {
          final DatenFilm result = film.get().getFilm();
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
  protected AbstractRecursivConverterTask<DatenFilm, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfFilmDetailTask(crawler, apiUrlBase, aElementsToProcess, authKey);
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
