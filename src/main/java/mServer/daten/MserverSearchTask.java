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
package mServer.daten;

import java.text.SimpleDateFormat;
import java.util.Date;

import mServer.crawler.CrawlerConfig;
import mServer.tool.MserverDatumZeit;
import mServer.tool.MserverKonstanten;
import mServer.tool.MserverLog;

public class MserverSearchTask {

    // Konstanten Suchen
    public static final String SUCHEN_UPDATE = "kurz";
    public static final String SUCHEN_LONG = "lang";
    public static final String SUCHEN_MAX = "max";

    public static final String SUCHEN_NEU = "neu";
    public static final String SUCHEN_WANN_SOFORT = "sofort";
    //
    public static final String SUCHEN_SENDER_WIE = "suchen-sender-wie"; // "short", "long", "max" - wenn leer dann nur "short"
    public static final int SUCHEN_SENDER_WIE_NR = 0;
    public static final String SUCHEN_LISTE_WIE = "suchen-liste-wie"; // "neu" - dann neue Liste erstellen, sonst update
    public static final int SUCHEN_LISTE_WIE_NR = 1;
    public static final String SUCHEN_WANN = "suchen-wann";
    public static final int SUCHEN_WANN_NR = 2;
    public static final String SUCHEN_SENDER = "suchen-sender";
    public static final int SUCHEN_SENDER_NR = 3;
    public static final String SUCHEN_ORG_LISTE = "suchen-org-liste"; // dann wird eine Orgliste erstellt, darauf beziehen sich dann die diff-Files
    public static final int SUCHEN_ORG_LISTE_NR = 4;
    public static final String SUCHEN_MAX_WAIT = "suchen-max-warten"; // der Suchlauf darf max. so lange dauern bis er abgebrochen wird
    public static final int SUCHEN_MAX_WAIT_NR = 5;
    // Array
    public static final String SUCHEN = "suchen";
    public static final int SUCHEN_MAX_ELEM = 6;
    public static final String[] SUCHEN_COLUMN_NAMES = {SUCHEN_SENDER_WIE, SUCHEN_LISTE_WIE, SUCHEN_WANN, SUCHEN_SENDER, SUCHEN_ORG_LISTE, SUCHEN_MAX_WAIT};
    public String[] arr = new String[SUCHEN_MAX_ELEM];

    public MserverSearchTask() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public void meldungNaechsterStart() {
        MserverLog.systemMeldung("naechster Start: " + this.arr[SUCHEN_WANN_NR]);
    }

    public void meldungStart() {
        MserverLog.systemMeldung("Starten: " + this.arr[SUCHEN_WANN_NR]);
        MserverLog.systemMeldung("Suchen Sender wie:  " + this.arr[SUCHEN_SENDER_WIE_NR]);
        MserverLog.systemMeldung("Suchen Liste wie:  " + (updateFilmliste() ? "nur ein Update" : "neue Filmliste"));
        if (!this.arr[SUCHEN_SENDER_NR].equals("")) {
            MserverLog.systemMeldung("Sender:  " + this.arr[SUCHEN_SENDER_NR]);
        }
    }

    public int getWaitTime() {
        int waitTime = MserverKonstanten.WAIT_TIME_LOAD_UPDATE;
        if (arr[MserverSearchTask.SUCHEN_MAX_WAIT_NR].isEmpty()) {
            switch (loadHow()) {
                case CrawlerConfig.LOAD_SHORT:
                    waitTime = MserverKonstanten.WAIT_TIME_LOAD_UPDATE;
                    break;
                case CrawlerConfig.LOAD_LONG:
                    waitTime = MserverKonstanten.WAIT_TIME_LOAD_BIG;
                    break;
                case CrawlerConfig.LOAD_MAX:
                    waitTime = MserverKonstanten.WAIT_TIME_LOAD_MAX;
                    break;
            }
        } else {
            try {
                waitTime = Integer.parseInt(arr[MserverSearchTask.SUCHEN_MAX_WAIT_NR]);
            } catch (Exception ignore) {
                waitTime = MserverKonstanten.WAIT_TIME_LOAD_UPDATE;
            }
        }
        return waitTime;
    }

    public boolean sofortSuchen() {
        return this.arr[SUCHEN_WANN_NR].equals(SUCHEN_WANN_SOFORT);
    }

    public boolean spaeter(MserverSearchTask d) {
        // Datum ist später als das von "d"
        if (sofortSuchen()) {
            return false;
        } else if (this.getTimeHeute().compareTo(d.getTimeHeute()) > 0) {
            return true;
        }
        return false;
    }

    public boolean orgListeAnlegen() {
        return Boolean.parseBoolean(arr[SUCHEN_ORG_LISTE_NR]);
    }

    public Date getTimeHeute() {
        Date ret;
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("dd.MM.yyyy__HH:mm");
        try {
            return sdf_zeit.parse(MserverDatumZeit.getHeute() + "__" + this.arr[SUCHEN_WANN_NR]);
        } catch (Exception ex) {
            ret = null;
            MserverLog.fehlerMeldung(825439079, MserverSearchTask.class.getName(), "getTime", ex);
        }
        return ret;
    }

    public int loadHow() {
        int ret;
        switch (this.arr[SUCHEN_SENDER_WIE_NR]) {
            case SUCHEN_LONG:
                ret = CrawlerConfig.LOAD_LONG;
                break;
            case SUCHEN_MAX:
                ret = CrawlerConfig.LOAD_MAX;
                break;
            default:
                ret = CrawlerConfig.LOAD_SHORT;
        }
        return ret;
    }

    public boolean updateFilmliste() {
        // Ist nichts angegeben, dann ist der Standardwert: Update der Filmliste
        boolean ret = true;
        if (this.arr[SUCHEN_LISTE_WIE_NR].equals(SUCHEN_NEU)) {
            ret = false;
        }
        return ret;
    }

    public boolean starten() {
        if (sofortSuchen()) {
            return true;
        }
        // true wenn gestartet werden soll: Auftrag liegt jetzt oder in der Vergangenheit
        Date now = new Date();
        return this.getTimeHeute().compareTo(now) <= 0;
    }
}
