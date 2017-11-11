package mServer.crawler.sender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.wdr.WdrDayPageCallable;
import mServer.crawler.sender.wdr.WdrLetterPageCallable;

public class MediathekWdr extends MediathekReader {
  private static final Logger LOG = LogManager.getLogger(MediathekWdr.class);

  public final static Sender SENDER = Sender.WDR;

  private final LinkedList<String> dayUrls = new LinkedList<>();
  private final LinkedList<String> letterPageUrls = new LinkedList<>();

  private MSStringBuilder seite_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

  Collection<Future<ListeFilme>> futureFilme = new ArrayList<>();

  public MediathekWdr(final FilmeSuchen ssearch, final int startPrio) {
    super(ssearch, SENDER.getName(), /* threads */ 3, /* urlWarten */ 100, startPrio);
  }

  // ===================================
  // public
  // ===================================
  @Override
  public synchronized void addToList() {
    clearLists();
    meldungStart();
    fillLists();

    if (Config.getStop()) {
      meldungThreadUndFertig();
    } else if (letterPageUrls.isEmpty() && dayUrls.isEmpty()) {
      meldungThreadUndFertig();
    } else {
      meldungAddMax(letterPageUrls.size() + dayUrls.size());

      startLetterPages();
      startDayPages();

      addFilms();

      meldungThreadUndFertig();
    }
  }

  private void addDayPages() {
    // Sendung verpasst, da sind einige die nicht in einer "Sendung" enthalten sind
    // URLs nach dem Muster bauen:
    // http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-27022016.html
    final SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
    String tag;
    for (int i = 1; i < 14; ++i) {
      final String URL =
          "http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-";
      tag = formatter.format(new Date().getTime() - 1000 * 60 * 60 * 24 * i);
      final String urlString = URL + tag + ".html";
      dayUrls.add(urlString);
    }
  }

  private void addFilms() {
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
        LOG.error("Es ist ein Fehler beim lesen der WDR Filme aufgetreten.", exception);
      }
    });
  }

  private void addLetterPages() {
    // http://www1.wdr.de/mediathek/video/sendungen/abisz-b100.html
    // Theman suchen
    final String URL = "http://www1.wdr.de/mediathek/video/sendungen-a-z/index.html";
    final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen-a-z/";
    final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
    seite_1 = getUrlIo.getUri_Iso(SENDER.getName(), URL, seite_1, "");
    int pos1;
    int pos2;
    String url;
    letterPageUrls.add(URL); // ist die erste Seite: "a"
    pos1 = seite_1.indexOf("<strong>A</strong>");
    while (!Config.getStop() && (pos1 = seite_1.indexOf(MUSTER_URL, pos1)) != -1) {
      pos1 += MUSTER_URL.length();
      if ((pos2 = seite_1.indexOf("\"", pos1)) != -1) {
        url = seite_1.substring(pos1, pos2);
        if (url.equals("index.html")) {
          continue;
        }
        if (url.isEmpty()) {
          Log.errorLog(995122047, "keine URL");
        } else {
          url = "http://www1.wdr.de/mediathek/video/sendungen-a-z/" + url;
          letterPageUrls.add(url);
        }
      }
    }
  }

  private void clearLists() {
    letterPageUrls.clear();
    dayUrls.clear();
  }

  private void fillLists() {
    addLetterPages();
    addDayPages();
  }

  private void startDayPages() {

    dayUrls.forEach(url -> {
      final ExecutorService executor = Executors.newCachedThreadPool();
      futureFilme.add(executor.submit(new WdrDayPageCallable(url)));
      meldungProgress(url);
    });
  }

  private void startLetterPages() {

    letterPageUrls.forEach(url -> {
      final ExecutorService executor = Executors.newCachedThreadPool();
      futureFilme.add(executor.submit(new WdrLetterPageCallable(url)));
      meldungProgress(url);
    });
  }
}
