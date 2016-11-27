/*
 * MediathekView
 * Copyright (C) 2011 W. Xaver
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
package mServer.mSearch.filmeSuchen;

import java.util.Date;
import mSearch.tool.Log;

/**
 * für jeden MediathekReader der "Sucht" gibts einen,
 * es werden Infos über das Suchen gesammelt:
 * Anzahl abgesuchter Websiten, Dauer, Traffic, ..
 *
 */
public class RunSender {

    public String sender = "";
    public int max = 0; // max laden
    public int progress = 0;
    public int maxThreads = 0;
    public int waitOnLoad = 0;
    public Date startZeit = new Date();
    public Date endZeit = new Date(); // zur Sicherheit

    public boolean fertig = false;
    long[] counter = new long[Count.values().length];

    public enum Count {

        ANZAHL("Seiten"), FILME("Filme"), FEHLER("Fehler"), FEHLVERSUCHE("FVers"), WARTEZEIT_FEHLVERSUCHE("ZeitFV[s]"),
        PROXY("Proxy" /*Seite über Proxy laden*/), NO_BUFFER("NoBuffer"),
        SUM_DATA_BYTE("sumData" /*Datenmenge, entpackt*/), SUM_TRAFFIC_BYTE("sumTraffic" /*Datenmenge die übertragen wird*/),
        SUM_TRAFFIC_LOADART_NIX("trNix"), SUM_TRAFFIC_LOADART_DEFLATE("trDeflate"), SUM_TRAFFIC_LOADART_GZIP("trGzip"),
        GET_SIZE_SUM("getSize"), GET_SIZE_SUM403("gS-403"), GET_SIZE_PROXY("gS-proxy");

        final String name;

        Count(String name) {
            this.name = name;
        }

        public static String[] getNames() {
            String[] ret = new String[values().length];

            for (int i = 0; i < values().length; ++i) {
                ret[i] = values()[i].name;
            }
            return ret;
        }
    }

    public RunSender(String sender, int max, int progress) {
        this.sender = sender;
        this.max = max;
        this.progress = progress;
    }

    public String getLaufzeitMinuten() {
        String ret = "";
        int sekunden;
        try {
            if (startZeit != null) {
                sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
                String min = String.valueOf(sekunden / 60);
                String sek = String.valueOf(sekunden % 60);
                if (sek.length() == 1) {
                    sek = "0" + sek;
                }
                ret = min + ":" + sek;
            }
        } catch (Exception ex) {
            Log.errorLog(976431583, ex, sender);
        }
        return ret;
    }

    public int getLaufzeitSekunden() {
        int sekunden = 0;
        try {
            if (startZeit != null && endZeit != null) {
                sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
            }
        } catch (Exception ex) {
            Log.errorLog(976431583, ex, sender);
        }
        return sekunden;
    }

    public static synchronized String getStringZaehler(long z) {
        return z == 0 ? "0" : ((z / 1000 / 1000) == 0 ? "<1" : String.valueOf(z / 1000 / 1000));
    }
}
