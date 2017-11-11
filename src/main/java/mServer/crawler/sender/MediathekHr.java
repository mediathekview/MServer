package mServer.crawler.sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.hr.HrSendungOverviewCallable;
import mServer.crawler.sender.hr.HrSendungenDto;
import mServer.crawler.sender.hr.HrSendungenListDeserializer;

public class MediathekHr extends MediathekReader {

  public final static Sender SENDER = Sender.HR;
  private static final String URL_SENDUNGEN = "http://www.hr-fernsehen.de/sendungen-a-z/index.html";

  private static final Logger LOG = LogManager.getLogger(MediathekHr.class);

  /**
   *
   * @param ssearch
   * @param startPrio
   */
  public MediathekHr(final FilmeSuchen ssearch, final int startPrio) {
    super(ssearch, SENDER.getName(), /* threads */ 2, /* urlWarten */ 200, startPrio);
  }

  /**
   *
   */
  @Override
  public void addToList() {
    meldungStart();

    List<HrSendungenDto> dtos = new ArrayList<>();

    try {
      final Document document = Jsoup.connect(URL_SENDUNGEN).get();
      final HrSendungenListDeserializer deserializer = new HrSendungenListDeserializer();

      dtos = deserializer.deserialize(document);
    } catch (final IOException ex) {
      Log.errorLog(894651554, ex);
    }

    meldungAddMax(dtos.size());

    final Collection<Future<ListeFilme>> futureFilme = new ArrayList<>();

    dtos.forEach(dto -> {

      final ExecutorService executor = Executors.newCachedThreadPool();
      futureFilme.add(executor.submit(new HrSendungOverviewCallable(dto)));
      meldungProgress(dto.getUrl());
    });

    futureFilme.forEach(e -> {
      try {
        final ListeFilme filmList = e.get();
        if (filmList != null) {
          filmList.forEach(film -> {
            if (film != null) {
              addFilm(film);
            }
          });
        }
      } catch (final Exception exception) {
        LOG.error("Es ist ein Fehler beim lesen der HR Filme aufgetreten.", exception);
      }
    });

    meldungThreadUndFertig();
  }
}
