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
import mServer.tool.MServerDatumZeit;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;

public class MServerDatenSuchen {

    public String[] arr = new String[MServerKonstanten.SUCHEN_MAX_ELEM];

    public MServerDatenSuchen() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public void meldungNaechsterStart() {
        MServerLog.systemMeldung("naechster Start: " + this.arr[MServerKonstanten.SUCHEN_WANN_NR]);
    }

    public void meldungStart() {
        MServerLog.systemMeldung("Starten: " + this.arr[MServerKonstanten.SUCHEN_WANN_NR]);
        MServerLog.systemMeldung("Suchen Sender wie:  " + this.arr[MServerKonstanten.SUCHEN_SENDER_WIE_NR]);
        MServerLog.systemMeldung("Suchen Liste wie:  " + (updateFilmliste() ? "nur ein Update" : "neue Filmliste"));
        if (!this.arr[MServerKonstanten.SUCHEN_SENDER_NR].equals("")) {
            MServerLog.systemMeldung("Sender:  " + this.arr[MServerKonstanten.SUCHEN_SENDER_NR]);
        }
    }

    public boolean jetzt() {
        return this.arr[MServerKonstanten.SUCHEN_WANN_NR].equals(MServerKonstanten.SUCHEN_WANN_SOFORT);
    }

    public boolean spaeter(MServerDatenSuchen d) {
        // Datum ist später als das von "d"
        if (jetzt()) {
            return false;
        } else if (this.getTimeHeute().compareTo(d.getTimeHeute()) > 0) {
            return true;
        }
        return false;
    }

    public String getExportFilmliste() {
        final String FILM_DATEI_SUFF = "bz2";
        final String FILMDATEI_NAME = "Filmliste";
        if (jetzt()) {
            return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
        } else {
            return FILMDATEI_NAME + "_" + arr[MServerKonstanten.SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF;
        }
    }

    public String getAktFilmliste() {
        final String FILM_DATEI_SUFF = "xml";
        final String FILMDATEI_NAME = "filme";
        return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
    }

    public Date getTimeHeute() {
        Date ret;
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("dd.MM.yyyy__HH:mm");
        try {
            return sdf_zeit.parse(MServerDatumZeit.getHeute() + "__" + this.arr[MServerKonstanten.SUCHEN_WANN_NR]);
        } catch (Exception ex) {
            ret = null;
            MServerLog.fehlerMeldung(825439079, MServerDatenSuchen.class.getName(), "getTime", ex);
        }
        return ret;
    }

    public boolean allesLaden() {
        return this.arr[MServerKonstanten.SUCHEN_SENDER_WIE_NR].equals(MServerKonstanten.SUCHEN_ALLES);
    }

    public boolean updateFilmliste() {
        // Ist nichts angegeben, dann ist der Standardwert: Update der Filmliste
        boolean ret = true;
        if (this.arr[MServerKonstanten.SUCHEN_LISTE_WIE_NR].equals(MServerKonstanten.SUCHEN_NEU)) {
            ret = false;
        }
        return ret;
    }

    public boolean starten() {
        if (jetzt()) {
            return true;
        }
        // true wenn gestartet werden soll: Auftrag liegt jetzt oder in der Vergangenheit
        Date now = new Date();
        if (this.getTimeHeute().compareTo(now) <= 0) {
            return true;
        }
        return false;
    }
}
