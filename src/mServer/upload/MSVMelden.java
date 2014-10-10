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
import mServer.daten.MSVDatenUpload;
import mServer.tool.MSVDaten;
import mServer.tool.MSVFunktionen;
import mServer.tool.MSVKonstanten;
import mServer.tool.MSVLog;
import mServer.tool.MSVWarten;

public class MSVMelden {

    public static synchronized boolean melden(String filmlisteDateiName, MSVDatenUpload mServerDatenUpload) {
        boolean ret = false;
        switch (mServerDatenUpload.arr[MSVDatenUpload.UPLOAD_LISTE_NR]) {
            case (MSVUpload.LISTE_DIFF):
                MSVLog.systemMeldung("Liste Diff: nicht melden");
                return true; // da wird nicht gemeldet
            case (MSVUpload.LISTE_AKT):
                MSVLog.systemMeldung("Liste Akt: nicht melden");
                return true; // da wird nicht gemeldet
            case (MSVUpload.LISTE_XML):
            case (MSVUpload.LISTE_JSON):
            default:
        }
        try {
            String urlFilmliste = mServerDatenUpload.getUrlFilmliste(filmlisteDateiName);
            String pwdServerMelden = mServerDatenUpload.getMeldenPwd();
            String urlServerMelden = mServerDatenUpload.getMeldenUrl();
            if (pwdServerMelden.isEmpty() || urlServerMelden.isEmpty() || urlFilmliste.isEmpty()) {
                // dann soll nicht gemeldet werden
                MSVLog.systemMeldung("Melden: keine URL, PWD");
                ret = true;
            } else {
                // nur dann gibts was zum Melden
                // die Zeitzone in der Liste ist "UTC"
                new MSVWarten().sekundenWarten(2);// damit der Server nicht stolpert, max alle 2 Sekunden
                String zeit = MSVFunktionen.getTime();
                String datum = MSVFunktionen.getDate();
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("-----------------------------------");
                MSVLog.systemMeldung("URL: " + urlFilmliste);
                MSVLog.systemMeldung("melden an Server: " + urlServerMelden);
                MSVLog.systemMeldung("Datum: " + datum + "  Zeit: " + zeit);
                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
                String urlMelden = urlServerMelden
                        + "?pwd=" + pwdServerMelden
                        + "&zeit=" + zeit
                        + "&datum=" + datum
                        + (mServerDatenUpload.getPrio().equals("") ? "" : "&prio=" + mServerDatenUpload.getPrio())
                        + "&server=" + urlFilmliste;
                int timeout = 20000;
                URLConnection conn = new URL(urlMelden).openConnection();
                conn.setRequestProperty("User-Agent", MSVDaten.getUserAgent());
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MSVKonstanten.KODIERUNG_UTF);
                inReader.read();
                inReader.close();
                MSVLog.systemMeldung("Melden: Ok");
                ret = true;
            }
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(301256907, MSVMelden.class.getName(), "Filmliste melden", ex);
        }
        return ret;
    }

//    public static synchronized void updateServerLoeschen() {
//        // wenn die eigene Server-URL aus der Downloadliste vor dem Suchen gelöscht werden soll
//        if (MSVDaten.system[MSVKonstanten.SYSTEM_URL_VORHER_LOESCHEN_NR].isEmpty()) {
//            return;
//        }
//        String pwdServerMelden = MSVDaten.system[MSVKonstanten.SYSTEM_MELDEN_PWD_JSON_NR].trim(); // nur für die aktuellen interessant
//        String urlServerMelden = MSVDaten.system[MSVKonstanten.SYSTEM_MELDEN_URL_JSON_NR].trim(); // nur für die aktuellen interessant
//        if (pwdServerMelden.isEmpty() || urlServerMelden.isEmpty()) {
//            // dann soll nicht gemeldet werden
//            MSVLog.systemMeldung("DownloadURL vorher löschen: keine URL, PWD");
//        } else {
//            // dann den aktuellsten Eintrag des Servers in der Liste löschen
//            try {
//                String delUrl = "";
//                ListeFilmlistenUrls listeFilmlistenUrls = new ListeFilmlistenUrls();
//                MSFilmlistenSuchen.getDownloadUrlsFilmlisten(MSConst.ADRESSE_FILMLISTEN_SERVER_JSON, listeFilmlistenUrls, MSVDaten.getUserAgent(), DatenFilmlisteUrl.SERVER_ART_OLD);
//                listeFilmlistenUrls.sort();
//                Iterator<DatenFilmlisteUrl> it = listeFilmlistenUrls.iterator();
//                int count = 0;
//                while (count < 45 && it.hasNext()) {
//                    // nur in den ersten 15 Einträgen suchen
//                    ++count;
//                    DatenFilmlisteUrl d = it.next();
//                    if (d.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR].startsWith(MSVDaten.system[MSVKonstanten.SYSTEM_URL_VORHER_LOESCHEN_NR].trim())) {
//                        delUrl = d.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR];
//                        break;
//                    }
//                }
//                if (!pwdServerMelden.isEmpty() && !urlServerMelden.isEmpty() && !delUrl.isEmpty()) {
//                    new MSVWarten().sekundenWarten(2);// damit der Server nicht stolpert, max alle 2 Sekunden
//                    String zeit = MSVFunktionen.getTime();
//                    String datum = MSVFunktionen.getDate();
//                    MSVLog.systemMeldung("");
//                    MSVLog.systemMeldung("--------------------------------------------------------------------------------");
//                    MSVLog.systemMeldung("Server löschen, URL: " + delUrl);
//                    MSVLog.systemMeldung("                     " + datum + " Zeit: " + zeit);
//                    String delCommand = urlServerMelden + "?pwd=" + pwdServerMelden + "&server=" + delUrl;
//                    int timeout = 20000;
//                    URLConnection conn = new URL(delCommand).openConnection();
//                    conn.setRequestProperty("User-Agent", MSVDaten.getUserAgent());
//                    conn.setReadTimeout(timeout);
//                    conn.setConnectTimeout(timeout);
//                    InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MSVKonstanten.KODIERUNG_UTF);
//                    inReader.read();
//                    inReader.close();
//                    MSVLog.systemMeldung("Ok");
//                    MSVLog.systemMeldung("--------------------------------------------------------------------------------");
//                }
//            } catch (Exception ex) {
//                MSVLog.fehlerMeldung(649701354, MSVMelden.class.getName(), "Filmliste löschen", ex);
//            }
//        }
//    }
}
