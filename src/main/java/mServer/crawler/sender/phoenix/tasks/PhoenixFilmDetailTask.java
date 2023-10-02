package mServer.crawler.sender.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.phoenix.PhoenixConstants;
import mServer.crawler.sender.phoenix.parser.PhoenixFilmDetailDeserializer;
import mServer.crawler.sender.phoenix.parser.PhoenixFilmDetailDto;
import mServer.crawler.sender.zdf.tasks.ZdfFilmDetailTask;
import mServer.crawler.sender.zdf.tasks.ZdfTaskBase;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhoenixFilmDetailTask extends ZdfTaskBase<DatenFilm, CrawlerUrlDTO> {

  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN = new TypeToken<Optional<PhoenixFilmDetailDto>>() {
  }.getType();

  private final String filmDetailHost;

  public PhoenixFilmDetailTask(MediathekReader aCrawler,
                               ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, Optional<String> aAuthKey,
                               String filmDetailHost) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
    this.filmDetailHost = filmDetailHost;

    registerJsonDeserializer(OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new PhoenixFilmDetailDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixFilmDetailTask(this.crawler, aElementsToProcess, this.authKey, this.filmDetailHost);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Optional<PhoenixFilmDetailDto> filmDetailDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    if (filmDetailDtoOptional.isEmpty()) {
      // tritt auf, wenn kein Film vorhanden
      return;
    }

    PhoenixFilmDetailDto filmDetailDto = filmDetailDtoOptional.get();
    ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();
    shows.add(
            new CrawlerUrlDTO(
                    (
                            PhoenixConstants.URL_VIDEO_DETAILS).formatted(
                            this.filmDetailHost,
                            filmDetailDto.getBaseName())));
    final ZdfFilmDetailTask zdfFilmDetailTask =
            new ZdfFilmDetailTask(this.crawler, "", shows, Optional.empty());
    final Set<DatenFilm> films = zdfFilmDetailTask.invoke();
    films.forEach(
            film -> {
              film.arr[DatenFilm.FILM_THEMA] = filmDetailDto.getTopic();
              film.arr[DatenFilm.FILM_TITEL] = filmDetailDto.getTitle();
              if (filmDetailDto.getWebsite().isPresent()) {
                film.arr[DatenFilm.FILM_WEBSEITE] = filmDetailDto.getWebsite().get();
              }
              this.taskResults.add(film);
            });
  }
}
