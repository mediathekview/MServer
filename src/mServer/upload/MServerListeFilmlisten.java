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
package mServer.upload;

import java.io.File;
import java.util.Iterator;
import mServer.tool.MServerDaten;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import msearch.filmeLaden.DatenFilmlistenServer;
import msearch.filmeLaden.DatenUrlFilmliste;
import msearch.filmeLaden.MSearchFilmlistenSuchen;
import msearch.filmeLaden.ListeDownloadUrlsFilmlisten;

public class MServerListeFilmlisten {

    public static File filmlisteEintragen(String urlDatei, DatenUrlFilmliste input) {
        ListeDownloadUrlsFilmlisten listeFilmUpdateServer = new ListeDownloadUrlsFilmlisten();
        // erst mal die Liste holen
        try {
            MSearchFilmlistenSuchen.getDownloadUrlsFilmlisten(urlDatei, listeFilmUpdateServer, MServerDaten.getUserAgent());
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(347895642, MServerListeFilmlisten.class.getName(), urlDatei, ex);
        }
        // Einträge mit der URL löschen und dann "input" eintragen
        // gibt immer nur einen Eintrag mit einer URL
        // und zu alte Einträge löschen
        Iterator<DatenUrlFilmliste> it = listeFilmUpdateServer.iterator();
        while (it.hasNext()) {
            DatenUrlFilmliste d = it.next();
            if (d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR].equals(input.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR])) {
                it.remove();
            } else if (d.aelterAls(MServerKonstanten.FILMLISTEN_MAX_ALTER)) {
                MServerLog.systemMeldung("Filmliste ist zu alt: " + d.arr[DatenFilmlistenServer.FILM_LISTEN_SERVER_URL_NR]);
                MServerLog.systemMeldung("Erstellt: " + d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_DATUM_NR] + ", " + d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_ZEIT_NR]);
                it.remove();
            }
        }
        listeFilmUpdateServer.add(input);
        // Liste in Datei schreiben
        File f = null;
        try {
            f = MSearchFilmlistenSuchen.ListeFilmlistenSchreiben(listeFilmUpdateServer);
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(347895642, MServerListeFilmlisten.class.getName(), urlDatei, ex);
        }
        return f;
    }
}
