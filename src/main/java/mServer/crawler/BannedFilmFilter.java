package mServer.crawler;

import de.mediathekview.mlib.daten.DatenFilm;

public class BannedFilmFilter {

  public static boolean isBanned(final DatenFilm film) {
    if (film.arr[DatenFilm.FILM_TITEL].equalsIgnoreCase("Geschichte einer Liebe - Freya")) {
      return true;
    }
    if (film.arr[DatenFilm.FILM_TITEL].equalsIgnoreCase("Wir haben genug - Wirtschaft ohne Wachstum")) {
      return true;
    }

    return false;
  }
}
