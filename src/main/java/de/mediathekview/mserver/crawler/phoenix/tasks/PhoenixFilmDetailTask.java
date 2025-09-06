package de.mediathekview.mserver.crawler.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.phoenix.PhoenixConstants;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmDetailDeserializer;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmDetailDto;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfFilmDetailTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;
import jakarta.ws.rs.client.WebTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.annotation.Nullable;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhoenixFilmDetailTask extends ZdfTaskBase<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(PhoenixFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<PhoenixFilmDetailDto>>() {}.getType();

  private final String filmDetailHost;

  public PhoenixFilmDetailTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      @Nullable final String authKey,
      final String filmDetailHost) {
    super(aCrawler, aUrlToCrawlDTOs, authKey);
    this.filmDetailHost = filmDetailHost;

    registerJsonDeserializer(
        OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new PhoenixFilmDetailDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixFilmDetailTask(
        crawler, aElementsToProcess, getAuthKey().orElse(null), filmDetailHost);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<PhoenixFilmDetailDto> filmDetailDtoOptional =
        deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    if (filmDetailDtoOptional.isEmpty()) {
      // tritt auf, wenn kein Film vorhanden
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    final PhoenixFilmDetailDto filmDetailDto = filmDetailDtoOptional.get();

    Queue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();
    shows.add(
        new CrawlerUrlDTO(
            String.format(
                PhoenixConstants.URL_VIDEO_DETAILS,
                this.filmDetailHost,
                filmDetailDto.getBaseName())));
    final ZdfFilmDetailTask zdfFilmDetailTask =
        new ZdfFilmDetailTask(this.crawler, "", shows, null, PhoenixConstants.PARTNER_TO_SENDER);
    final Set<Film> films = zdfFilmDetailTask.invoke();
    films.forEach(
        film -> {
          film.setThema(filmDetailDto.getTopic());
          film.setTitel(filmDetailDto.getTitle());
          if (filmDetailDto.getWebsite().isPresent()) {
            try {
              film.setWebsite(new URI(filmDetailDto.getWebsite().get()).toURL());
            } catch (MalformedURLException | URISyntaxException e) {
              LOG.error("invalid url: ", e);
            }
          }
          if (!this.taskResults.add(film)) {
            LOG.error("Rejected duplicate {}", film);
          }
        });
  }
}
