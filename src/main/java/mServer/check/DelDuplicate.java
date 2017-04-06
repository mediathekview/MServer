/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.check;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.filmlisten.FilmlisteLesen;
import de.mediathekview.mlib.filmlisten.WriteFilmlistJson;
import java.util.HashSet;

public class DelDuplicate {

    int vorher;
    boolean simulate;
    String url;
    ListeFilme readList = new ListeFilme();
    ListeFilme writeList = new ListeFilme();
    HashSet<String> hash = new HashSet<>();

    public DelDuplicate(String url, boolean simulate) {
        this.simulate = simulate;
        this.url = url;
    }

    private void start() {
        new FilmlisteLesen().readFilmListe(url, readList, 0 /*all days*/);
        vorher = readList.size();

        System.out.println("");
        System.out.println("");
        System.out.println("---------------------");
        System.out.println("vorher:    " + vorher);
    }

    private void end() {
        hash.clear();
        // ==========================================
        System.out.println("danach:    " + writeList.size());
        System.out.println("Differenz: " + (vorher - writeList.size()));
        System.out.println("---------------------");

        if (!simulate) {
            new WriteFilmlistJson().filmlisteSchreibenJson(url, writeList);
        }

    }

    public void delSenderUrl() {
        start();
        readList.stream().filter(film -> !hash.contains(film.arr[DatenFilm.FILM_SENDER] + film.arr[DatenFilm.FILM_URL]))
                .forEach(film
                        -> {
                    hash.add(film.arr[DatenFilm.FILM_URL]);
                    writeList.add(film);
                });
        end();
    }

    public void delUrl() {
        start();
        readList.stream().filter(film -> !hash.contains(film.arr[DatenFilm.FILM_URL]))
                .forEach(film
                        -> {
                    hash.add(film.arr[DatenFilm.FILM_URL]);
                    writeList.add(film);
                });
        hash.clear();
        end();
    }
}
