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
package mServer.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * für jeden MediathekReader der "Sucht" gibts einen,
 * es werden Infos über das Suchen gesammelt:
 * Anzahl abgesuchter Websiten, Dauer, Traffic, ..
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
    public final ConcurrentHashMap<Count, AtomicLong> counter = new ConcurrentHashMap<>();

    public RunSender(String sender, int max, int progress) {
        this.sender = sender;
        this.max = max;
        this.progress = progress;

        //initialize the counter
        for (Count item : EnumSet.allOf(Count.class)) {
            counter.put(item, new AtomicLong(0));
        }
    }

    public static String getStringZaehler(final long z) {
        return z == 0 ? "0" : ((z / 1000_000) == 0 ? "<1" : String.valueOf(z / 1000_000));
    }

    public String getLaufzeitMinuten() {
        String ret = "";
        int sekunden;
        if (startZeit != null) {
            sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
            String min = String.valueOf(sekunden / 60);
            String sek = String.valueOf(sekunden % 60);
            if (sek.length() == 1) {
                sek = '0' + sek;
            }
            ret = min + ':' + sek;
        }

        return ret;
    }

    public int getLaufzeitSekunden() {
        int sekunden = 0;
        if (startZeit != null && endZeit != null) {
            sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
        }

        return sekunden;
    }

    public enum Count {

        ANZAHL("Seiten"), FILME("Filme"), FEHLER("Fehler"), FEHLVERSUCHE("FVers"), WARTEZEIT_FEHLVERSUCHE("ZeitFV[s]"),
        /**
         * Seite über Proxy laden.
         */
        PROXY("Proxy"),
        /**
         * Datenmenge, entpackt
         */
        SUM_DATA_BYTE("sumData"),
        /**
         * Datenmenge, die übertragen wird
         */
        SUM_TRAFFIC_BYTE("sumTraffic"),
        SUM_TRAFFIC_LOADART_NIX("trNix"),
        GET_SIZE_SUM("getSize"), GET_SIZE_PROXY("gS-proxy");

        private final String name;

        Count(String name) {
            this.name = name;
        }

        public static String[] getNames() {
            ArrayList<String> enumNames = new ArrayList<>();
            for (Count name : EnumSet.allOf(Count.class)) {
                enumNames.add(name.toString());
            }
            return enumNames.toArray(new String[enumNames.size()]);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
