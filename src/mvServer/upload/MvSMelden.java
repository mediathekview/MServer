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
package mvServer.upload;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import mvServer.daten.MvSDatenUpload;
import mvServer.tool.MvSDaten;
import mvServer.tool.MvSFunktionen;
import mvServer.tool.MvSKonstanten;
import mvServer.tool.MvSLog;
import mvServer.tool.MvSWarten;

public class MvSMelden {

    public static synchronized boolean melden(String filmlisteDateiName, MvSDatenUpload mServerDatenUpload) {
        // bruchts nicht mehr
        return true; // da wird nicht gemeldet

////        boolean ret = false;
////        switch (mServerDatenUpload.arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
////            case (MvSUpload.LISTE_DIFF):
////                MvSLog.systemMeldung("Liste Diff: nicht melden");
////                return true; // da wird nicht gemeldet
////            case (MvSUpload.LISTE_AKT):
////                MvSLog.systemMeldung("Liste Akt: nicht melden");
////                return true; // da wird nicht gemeldet
////            case (MvSUpload.LISTE_XML):
////            case (MvSUpload.LISTE_JSON):
////            default:
////        }
////        try {
////            String urlFilmliste = mServerDatenUpload.getUrlFilmliste(filmlisteDateiName);
////            String pwdServerMelden = mServerDatenUpload.getMeldenPwd();
////            String urlServerMelden = mServerDatenUpload.getMeldenUrl();
////            if (pwdServerMelden.isEmpty() || urlServerMelden.isEmpty() || urlFilmliste.isEmpty()) {
////                // dann soll nicht gemeldet werden
////                MvSLog.systemMeldung("Melden: keine URL, PWD");
////                ret = true;
////            } else {
////                // nur dann gibts was zum Melden
////                // die Zeitzone in der Liste ist "UTC"
////                new MvSWarten().sekundenWarten(2);// damit der Server nicht stolpert, max alle 2 Sekunden
////                String zeit = MvSFunktionen.getTime();
////                String datum = MvSFunktionen.getDate();
////                MvSLog.systemMeldung("");
////                MvSLog.systemMeldung("-----------------------------------");
////                MvSLog.systemMeldung("URL: " + urlFilmliste);
////                MvSLog.systemMeldung("melden an Server: " + urlServerMelden);
////                MvSLog.systemMeldung("Datum: " + datum + "  Zeit: " + zeit);
////                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
////                String urlMelden = urlServerMelden
////                        + "?pwd=" + pwdServerMelden
////                        + "&zeit=" + zeit
////                        + "&datum=" + datum
////                        + (mServerDatenUpload.getPrio().equals("") ? "" : "&prio=" + mServerDatenUpload.getPrio())
////                        + "&server=" + urlFilmliste;
////                int timeout = 20000;
////                URLConnection conn = new URL(urlMelden).openConnection();
////                conn.setRequestProperty("User-Agent", MvSDaten.getUserAgent());
////                conn.setReadTimeout(timeout);
////                conn.setConnectTimeout(timeout);
////                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MvSKonstanten.KODIERUNG_UTF);
////                inReader.read();
////                inReader.close();
////                MvSLog.systemMeldung("Melden: Ok");
////                ret = true;
////            }
////        } catch (Exception ex) {
////            MvSLog.fehlerMeldung(301256907, MvSMelden.class.getName(), "Filmliste melden", ex);
////        }
////        return ret;
    }

}
