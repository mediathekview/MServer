package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.google.common.base.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;



public class CompareFilmlistsTest {
  private static final Logger LOG = LogManager.getLogger(CompareFilmlistsTest.class);
  private int fullmatch = 0;
  private int missingLeft = 0;
  private int missingRight = 0;
  private int diff = 0;
  

  @Test
  void readFilmlistOldFormatIncludingBrokenRecords()
      throws IOException {
    //
    LOG.info("Start");
    if (false) {
      return;
    }
    
    String aList = "C:/tmp/filme-old.json";
    String bList = "C:/tmp/filme-new.json";
    ClassLoader classLoader = getClass().getClassLoader();
    final Path aListPath = new File(aList).toPath();
    Optional<Filmlist> aFilmlist = new FilmlistOldFormatReader().read(new FileInputStream(aListPath.toString()));
    assertTrue(aFilmlist.isPresent());
    final Path bListPath = new File(bList).toPath();
    Optional<Filmlist> bFilmlist = new FilmlistOldFormatReader().read(new FileInputStream(bListPath.toString()));
    assertTrue(bFilmlist.isPresent());
    //
    if (true) {
    ArrayList<Film> bFilms = new ArrayList<>(bFilmlist.get().getFilms().values());
    aFilmlist.get().getFilms().values().forEach( f -> {
      Film target = index(bFilms, f);
      if (target != null) {
        compare(f, target);
      }
    });
    aFilmlist.get().getFilms().values().forEach( f -> {
      if (index(bFilms, f) == null) {
        LOG.info("Missing Film in "+bList+" list");
        LOG.info(f.toString());
        missingLeft++;
      }
    });
    ArrayList<Film> aFilms = new ArrayList<>(aFilmlist.get().getFilms().values());
    bFilms.forEach( f -> {
      Film target = index(aFilms, f);
      if (target == null) {
        LOG.info("Missing Film in "+aList+" list");
        LOG.info(f.toString());
        missingRight++;
      }
    });}
    new FilmlistOldFormatWriter().write(aFilmlist.get(), Path.of("c:/tmp/aFilmlist.json"));
    new FilmlistOldFormatWriter().write(bFilmlist.get(), Path.of("c:/tmp/bFilmlist.json"));
    //
    LOG.info("Matching: {} MissingLeft({}): {} MissingRight({}): {} Diff: {}", fullmatch, bList, missingLeft, aList, missingRight, diff);
  }
  
  private void compare(Film aFilm, Film bFilm) {
    String error = "";
    //if (!aFilm.getSenderName().equalsIgnoreCase(bFilm.getSenderName())) {
    //  error += "Incorrect Sender";
    //} 
    if (!aFilm.getTitel().equalsIgnoreCase(bFilm.getTitel())){
      error += "Incorrect Title '" + aFilm.getTitel() + "' vs '" + bFilm.getTitel() + "'";
    } 
    if (!aFilm.getThema().equalsIgnoreCase(bFilm.getThema())){
      error += "Incorrect Topic '" + aFilm.getThema() + "' vs '" + bFilm.getThema() + "'";
    } 
    if (!aFilm.getDuration().equals(bFilm.getDuration())){
      error += "Incorrect Duration" + aFilm.getDuration() + "' vs '" + bFilm.getDuration() + "'";
    }
    if (false && !aFilm.getBeschreibung().equalsIgnoreCase(bFilm.getBeschreibung())) {
      // new parser cuts out last char
      if (aFilm.getBeschreibung().length() > 70 && bFilm.getBeschreibung().length() > 70 && 
          !aFilm.getBeschreibung().substring(1,aFilm.getBeschreibung().length()-19).equalsIgnoreCase(bFilm.getBeschreibung().substring(1,bFilm.getBeschreibung().length()-18))){
        error += "Incorrect Description";
      } else if (aFilm.getBeschreibung().length() > 20 && bFilm.getBeschreibung().length() > 20 &&
          !aFilm.getBeschreibung().substring(1,20).equalsIgnoreCase(bFilm.getBeschreibung().substring(1,20))){
          error += "Incorrect Description";
      }
    }
    if (!aFilm.getWebsite().toString().equalsIgnoreCase(bFilm.getWebsite().toString())){
      error += "Incorrect website" + aFilm.getWebsite().toString() + "' vs '" + bFilm.getWebsite().toString() + "'";
    } 
    if (!aFilm.getTime().equals(bFilm.getTime())){
      error += "Incorrect Time " + aFilm.getTime() + "' vs '" + bFilm.getTime() + "'";
    } 
    //if (!aFilm.getSubtitles().equals(bFilm.getSubtitles())){
    //  error += "Incorrect subtitle";
    //}
    if (!compareFilmUrl(aFilm.getUrl(Resolution.SMALL), bFilm.getUrl(Resolution.SMALL))) {
      error += "URL SMALL " + aFilm.getUrl(Resolution.SMALL) + "' vs '" + bFilm.getUrl(Resolution.SMALL) + "'";
    }
    if (!compareFilmUrl(aFilm.getUrl(Resolution.NORMAL), bFilm.getUrl(Resolution.NORMAL))) {
      error += "URL NORMAL " + aFilm.getUrl(Resolution.NORMAL) + "' vs '" + bFilm.getUrl(Resolution.NORMAL) + "'";
    }
    if (!compareFilmUrl(aFilm.getUrl(Resolution.HD), bFilm.getUrl(Resolution.HD))) {
      error += "URL HD "+ aFilm.getUrl(Resolution.HD) + "' vs '" + bFilm.getUrl(Resolution.HD) + "'";
    }
    //
    if (error != "") {
      LOG.info(error);
      LOG.info(aFilm.toString());
      LOG.info(bFilm.toString());
      diff++;
    } else {
      fullmatch++;
    }
  }
  
  private static boolean compareFilmUrl(FilmUrl a, FilmUrl b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    return (a.getUrl().toString().equalsIgnoreCase(b.getUrl().toString()));
  }
  
  private static Film index(ArrayList<Film> list, Film aFilm) {
    for (Film e : list) {
        if (e.getSenderName().equalsIgnoreCase(aFilm.getSenderName()) &&
            e.getTitel().equalsIgnoreCase(aFilm.getTitel()) &&
            e.getThema().equalsIgnoreCase(aFilm.getThema()) &&
            e.getTime().equals(aFilm.getTime())) {
            return e;
        }
    }
    return null;
}

}
