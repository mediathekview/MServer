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

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_Melden {

    private static SimpleDateFormat sdf_zeit = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");

    public static void melden(String urlFilmliste, String prio) {
        try {
            String pwd = MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_MELDEN_PWD_NR].trim();
            String url = MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_MELDEN_URL_NR].trim();
            if (!pwd.equals("") && !url.equals("") && !urlFilmliste.equals("")) {
                // nur dann gibts was zum Melden
                String zeit = sdf_zeit.format(new Date());
                String datum = sdf_datum.format(new Date());
                MS_Log.systemMeldung("URL: " + urlFilmliste);
                MS_Log.systemMeldung("melden an Server: " + url);
                MS_Log.systemMeldung("Datum: " + datum + "  Zeit: " + zeit);
                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
                String urlMelden = url
                        + "?pwd=" + MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_MELDEN_PWD_NR]
                        + "&zeit=" + zeit
                        + "&datum=" + datum
                        + (prio.trim().equals("") ? "" : "&prio=" + prio)
                        + "&server=" + urlFilmliste;
                int timeout = 20000;
                URLConnection conn = new URL(urlMelden).openConnection();
                conn.setRequestProperty("User-Agent", MS_Daten.getUserAgent());
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MS_Konstanten.KODIERUNG_UTF);
                inReader.read();
                inReader.close();
                MS_Log.systemMeldung("Ok");
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(301256907, MS_Melden.class.getName(), "melden", ex);
        }
    }
}
