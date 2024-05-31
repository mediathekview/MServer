package de.mediathekview.mserver.base.utils;

import java.util.ArrayList;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.kika.KikaApiCrawler;

public class FilmlistDebugHelper {
  private static final Logger LOG = LogManager.getLogger(FilmlistDebugHelper.class);
  
  
  
  public static Film getFilmFromSet(Set<Film> base, Film searchFilm) {
    for (Film e : base) {
        if (e.equals(searchFilm)) {
            return e;
        }
    }
    return null;
  }
  
  public static void printFilmlistForSender(Filmlist list, String sender, boolean printFullDetails) {
    list.getFilms().values().stream().forEach( e -> {
      if (e.getSenderName().equalsIgnoreCase(sender)) {
        if (printFullDetails) {
          LOG.debug("{} {} {}", e.getTitel(), e.getThema(), e);
        } else {
          LOG.debug("{} {}", e.getTitel(), e.getThema());
        }
      }
    });
  }
  
  public static void printFilmlistForThema(Filmlist list, String thema, boolean printFullDetails) {
    list.getFilms().values().stream().forEach( e -> {
      if (e.getThema().equalsIgnoreCase(thema)) {
        if (printFullDetails) {
          LOG.debug("{} {} {} {}", e.getSenderName(), e.getTitel(), e.getThema(), e);
        } else {
          LOG.debug("{} {} {}", e.getSenderName(), e.getTitel(), e.getThema());
        }
      }
    });
  }
  
  
  
  
  public static void compareFilmlist(Filmlist aFilmlist, Filmlist bFilmlist) {
    ArrayList<Film> bFilms = new ArrayList<>(bFilmlist.getFilms().values());
    aFilmlist.getFilms().values().forEach( f -> {
      if (bFilms.indexOf(f) == -1) {
        LOG.info("Missing Film in source list");
        LOG.info(f.toString());
      } else {
        Film expectedFilm = bFilms.get(bFilms.indexOf(f));
        compare(f, expectedFilm);
      }
    });
    ArrayList<Film> aFilms = new ArrayList<>(aFilmlist.getFilms().values());
    bFilms.forEach( f -> {
      if (aFilms.indexOf(f) == -1) {
        LOG.info("Missing Film in target list");
        LOG.info(f.toString());
      }
    });
  }
  
  private static void compare(Film aFilm, Film bFilm) {
    String error = "";
    if (!aFilm.getSenderName().equalsIgnoreCase(bFilm.getSenderName())) {
      error = "Incorrect Sender";
    } else if (!aFilm.getTitel().equalsIgnoreCase(bFilm.getTitel())){
      error = "Incorrect Title";
    } else if (!aFilm.getThema().equalsIgnoreCase(bFilm.getThema())){
      error = "Incorrect Topic";
    } else if (!aFilm.getDuration().equals(bFilm.getDuration())){
      error = "Incorrect Duration";
    } else if (!aFilm.getBeschreibung().equalsIgnoreCase(bFilm.getBeschreibung())){
      error = "Incorrect Description";
    } else if (!aFilm.getWebsite().toString().equalsIgnoreCase(bFilm.getWebsite().toString())){
      error = "Incorrect website";
    } else if (!aFilm.getTime().equals(bFilm.getTime())){
      error = "Incorrect Time";
    } else if (!aFilm.getSubtitles().equals(bFilm.getSubtitles())){
      error = "Incorrect subtitle";
    } else if (bFilm.getUrl(Resolution.SMALL) != null && 
              !aFilm.getUrl(Resolution.SMALL).toString().equalsIgnoreCase(bFilm.getUrl(Resolution.SMALL).toString())) {
      error = "URL SMALL";
    } else if (bFilm.getUrl(Resolution.NORMAL) != null &&
              !aFilm.getUrl(Resolution.NORMAL).toString().equalsIgnoreCase(bFilm.getUrl(Resolution.NORMAL).toString())) {
      error = "URL NORMAL";
    } else if (bFilm.getUrl(Resolution.HD) != null &&
                !aFilm.getUrl(Resolution.HD).toString().equalsIgnoreCase(bFilm.getUrl(Resolution.HD).toString())) {
      error = "URL HD";
    }
    //
    if (error != "") {
      LOG.info(error);
      LOG.info(aFilm.toString());
      LOG.info(bFilm.toString());
    }
  }
  
}
