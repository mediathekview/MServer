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
import mServer.tool.MServerDaten;
import mServer.tool.MServerFunktionen;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;

public class MServerMelden {

    public synchronized boolean melden(String urlFilmliste, String prio) {
        boolean ret = false;
        try {
            String pwd = MServerDaten.system[MServerKonstanten.SYSTEM_UPDATE_MELDEN_PWD_NR].trim();
            String url = MServerDaten.system[MServerKonstanten.SYSTEM_UPDATE_MELDEN_URL_NR].trim();
            if (!pwd.equals("") && !url.equals("") && !urlFilmliste.equals("")) {
                // nur dann gibts was zum Melden
                // die Zeitzone in der Liste ist "UTC"
                String zeit = MServerFunktionen.getTime();
                String datum = MServerFunktionen.getDate();
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("-----------------------------------");
                MServerLog.systemMeldung("URL: " + urlFilmliste);
                MServerLog.systemMeldung("melden an Server: " + url);
                MServerLog.systemMeldung("Datum: " + datum + "  Zeit: " + zeit);
                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
                String urlMelden = url
                        + "?pwd=" + MServerDaten.system[MServerKonstanten.SYSTEM_UPDATE_MELDEN_PWD_NR]
                        + "&zeit=" + zeit
                        + "&datum=" + datum
                        + (prio.trim().equals("") ? "" : "&prio=" + prio)
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
                this.wait(2000); // damit der Server nicht stolpert, max alle 2 Sekunden
                ret = true;
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(301256907, MServerMelden.class.getName(), "melden", ex);
        }
        return ret;
    }
}
