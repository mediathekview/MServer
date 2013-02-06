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
package mediathekServer.upload;

import java.io.File;
import java.util.Iterator;
import mediathek.controller.filmeLaden.importieren.DatenFilmlistenServer;
import mediathek.controller.filmeLaden.importieren.DatenUrlFilmliste;
import mediathek.controller.filmeLaden.importieren.FilmlistenSuchen;
import mediathek.controller.filmeLaden.importieren.ListeDownloadUrlsFilmlisten;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_ListeFilmlisten {

    public static File filmlisteEintragen(String urlDatei, DatenUrlFilmliste input) {
        ListeDownloadUrlsFilmlisten listeFilmUpdateServer = new ListeDownloadUrlsFilmlisten();
        // erst mal die Liste holen
        try {
            FilmlistenSuchen.getDownloadUrlsFilmlisten(urlDatei, listeFilmUpdateServer, MS_Daten.getUserAgent());
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(347895642, MS_ListeFilmlisten.class.getName(), urlDatei, ex);
        }
        // Einträge mit der URL löschen und dann "input" eintragen
        // gibt immer nur einen Eintrag mit einer URL
        // und zu alte Einträge löschen
        Iterator<DatenUrlFilmliste> it = listeFilmUpdateServer.iterator();
        while (it.hasNext()) {
            DatenUrlFilmliste d = it.next();
            if (d.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR].equals(input.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR])) {
                it.remove();
            } else if (d.aelterAls(MS_Konstanten.FILMLISTEN_MAX_ALTER)) {
                MS_Log.systemMeldung("Filmliste ist zu alt: " + d.arr[DatenFilmlistenServer.FILM_LISTEN_SERVER_URL_NR]);
                MS_Log.systemMeldung("Erstellt: " + d.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_DATUM_NR] + ", " + d.arr[FilmlistenSuchen.FILM_UPDATE_SERVER_ZEIT_NR]);
                it.remove();
            }
        }
        listeFilmUpdateServer.add(input);
        // Liste in Datei schreiben
        File f = null;
        try {
            f = FilmlistenSuchen.ListeFilmlistenSchreiben(listeFilmUpdateServer);
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(347895642, MS_ListeFilmlisten.class.getName(), urlDatei, ex);
        }
        return f;
    }
}
