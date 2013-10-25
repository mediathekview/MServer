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

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import mServer.daten.MServerDatenUpload;
import mServer.tool.MServerDaten;
import mServer.tool.MServerFunktionen;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import mServer.tool.MServerWarten;
import msearch.filmeLaden.DatenUrlFilmliste;
import msearch.filmeLaden.ListeDownloadUrlsFilmlisten;
import msearch.filmeLaden.MSearchFilmlistenSuchen;

public class MServerMelden {

    public static synchronized boolean melden(String filmlisteDateiName, MServerDatenUpload mServerDatenUpload) {
        boolean ret = false;
        try {
            new MServerWarten().sekundenWarten(2);// damit der Server nicht stolpert, max alle 2 Sekunden
            String urlFilmliste = mServerDatenUpload.getUrlFilmliste(filmlisteDateiName);
            String pwd = mServerDatenUpload.getMeldenPwd();
            String urlServerMelden = mServerDatenUpload.getMeldenUrl();
            if (!pwd.equals("") && !urlServerMelden.equals("") && !urlFilmliste.equals("")) {
                // nur dann gibts was zum Melden
                // die Zeitzone in der Liste ist "UTC"
                String zeit = MServerFunktionen.getTime();
                String datum = MServerFunktionen.getDate();
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("-----------------------------------");
                MServerLog.systemMeldung("URL: " + urlFilmliste);
                MServerLog.systemMeldung("melden an Server: " + urlServerMelden);
                MServerLog.systemMeldung("Datum: " + datum + "  Zeit: " + zeit);
                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
                String urlMelden = urlServerMelden
                        + "?pwd=" + pwd
                        + "&zeit=" + zeit
                        + "&datum=" + datum
                        + (mServerDatenUpload.getPrio().equals("") ? "" : "&prio=" + mServerDatenUpload.getPrio())
                        + "&server=" + urlFilmliste;
                int timeout = 20000;
                URLConnection conn = new URL(urlMelden).openConnection();
                conn.setRequestProperty("User-Agent", MServerDaten.getUserAgent());
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MServerKonstanten.KODIERUNG_UTF);
                inReader.read();
                inReader.close();
                MServerLog.systemMeldung("Ok");
                ret = true;
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(301256907, MServerMelden.class.getName(), "Filmliste melden", ex);
        }
        return ret;
    }

    public static synchronized boolean updateServerLoeschen(MServerDatenUpload mServerDatenUpload) {
        boolean ret = false;
        String delUrl = "";
        String pwdServerMelden = mServerDatenUpload.getMeldenPwd();
        String urlServerMelden = mServerDatenUpload.getMeldenUrl();
        // dann den aktuellsten Eintrag des Servers in der Liste löschen
        ListeDownloadUrlsFilmlisten listeDownloadUrlsFilmlisten = new ListeDownloadUrlsFilmlisten();
        MSearchFilmlistenSuchen.getDownloadUrlsFilmlisten(mServerDatenUpload.getUrlFilmlistenServer(), listeDownloadUrlsFilmlisten, MServerDaten.getUserAgent());
        listeDownloadUrlsFilmlisten.sort();
        Iterator<DatenUrlFilmliste> it = listeDownloadUrlsFilmlisten.iterator();
        int count = 0;
        while (count < 5 && it.hasNext()) {
            // nur in der ersten 5 Einträgen suchen
            ++count;
            DatenUrlFilmliste d = it.next();
            if (d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR].startsWith(mServerDatenUpload.arr[MServerDatenUpload.UPLOAD_URL_FILMLISTE_NR])) {
                delUrl = d.arr[MSearchFilmlistenSuchen.FILM_UPDATE_SERVER_URL_NR];
                break;
            }
        }
        try {
            if (!pwdServerMelden.isEmpty() && !urlServerMelden.isEmpty() && !delUrl.isEmpty()) {
                new MServerWarten().sekundenWarten(2);// damit der Server nicht stolpert, max alle 2 Sekunden
                String zeit = MServerFunktionen.getTime();
                String datum = MServerFunktionen.getDate();
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("--------------------------------------------------------------------------------");
                MServerLog.systemMeldung("Server löschen, URL: " + delUrl);
                MServerLog.systemMeldung("                     " + datum + " Zeit: " + zeit);
                String delCommand = urlServerMelden + "?pwd=" + pwdServerMelden + "&server=" + delUrl;
                int timeout = 20000;
                URLConnection conn = new URL(delCommand).openConnection();
                conn.setRequestProperty("User-Agent", MServerDaten.getUserAgent());
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MServerKonstanten.KODIERUNG_UTF);
                inReader.read();
                inReader.close();
                MServerLog.systemMeldung("Ok");
                MServerLog.systemMeldung("--------------------------------------------------------------------------------");
                ret = true;
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(649701354, MServerMelden.class.getName(), "Filmliste löschen", ex);
        }
        return ret;
    }
}
