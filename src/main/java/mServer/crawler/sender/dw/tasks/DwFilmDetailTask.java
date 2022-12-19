package mServer.crawler.sender.dw.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import jakarta.ws.rs.client.WebTarget;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.dw.DWTaskBase;
import mServer.crawler.sender.dw.parser.DwFilmDetailDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class DwFilmDetailTask extends DWTaskBase<DatenFilm, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(DwFilmDetailTask.class);
  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<DatenFilm>>() {}.getType();

  public DwFilmDetailTask(
          final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.empty());

    registerJsonDeserializer(
        OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new DwFilmDetailDeserializer(this.crawler));
  }

  @Override
  protected DwFilmDetailTask createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new DwFilmDetailTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    Optional<DatenFilm> filmDetailDtoOptional = Optional.empty();
    try {
      filmDetailDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    } catch (Exception e) {
      LOG.error("error processing {} ", aDTO.getUrl(), e);
    }
    if (!filmDetailDtoOptional.isPresent()) {
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLVERSUCHE);
      return;
    }
    this.taskResults.add(filmDetailDtoOptional.get());
  }
}
