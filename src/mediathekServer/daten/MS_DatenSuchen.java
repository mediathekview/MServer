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
package mediathekServer.daten;

import java.text.SimpleDateFormat;
import java.util.Date;
import mediathekServer.tool.MS_DatumZeit;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_DatenSuchen {

    public String[] arr = new String[MS_Konstanten.SUCHEN_MAX_ELEM];

    public MS_DatenSuchen() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public void meldungNaechsterStart() {
        MS_Log.systemMeldung("naechster Start: " + this.arr[MS_Konstanten.SUCHEN_WANN_NR]);
    }

    public void meldungStart() {
        MS_Log.systemMeldung("Starten: " + this.arr[MS_Konstanten.SUCHEN_WANN_NR]);
        MS_Log.systemMeldung("Suchen Sender wie:  " + this.arr[MS_Konstanten.SUCHEN_SENDER_WIE_NR]);
        MS_Log.systemMeldung("Suchen Liste wie:  " + this.arr[MS_Konstanten.SUCHEN_LISTE_WIE_NR]);
        if (!this.arr[MS_Konstanten.SUCHEN_SENDER_NR].equals("")) {
            MS_Log.systemMeldung("Sender:  " + this.arr[MS_Konstanten.SUCHEN_SENDER_NR]);
        }
    }

    public boolean jetzt() {
        return this.arr[MS_Konstanten.SUCHEN_WANN_NR].equals(MS_Konstanten.SUCHEN_WANN_SOFORT);
    }

    public boolean spaeter(MS_DatenSuchen d) {
        // Datum ist später als das von "d"
        if (jetzt()) {
            return false;
        } else if (this.getTimeHeute().compareTo(d.getTimeHeute()) > 0) {
            return true;
        }
        return false;
    }

    public String getZielDateiName() {
        final String FILM_DATEI_SUFF = "bz2";
        final String FILMDATEI_NAME = "Filmliste";
        if (jetzt()) {
            return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
        } else {
            return FILMDATEI_NAME + "_" + arr[MS_Konstanten.SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF;
        }
    }

    public Date getTimeHeute() {
        Date ret;
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("dd.MM.yyyy__HH:mm");
        try {
            return sdf_zeit.parse(MS_DatumZeit.getHeute() + "__" + this.arr[MS_Konstanten.SUCHEN_WANN_NR]);
        } catch (Exception ex) {
            ret = null;
            MS_Log.fehlerMeldung(825439079, MS_DatenSuchen.class.getName(), "getTime", ex);
        }
        return ret;
    }

    public boolean allesLaden() {
        return this.arr[MS_Konstanten.SUCHEN_SENDER_WIE_NR].equals(MS_Konstanten.SUCHEN_ALLES);
    }

    public boolean updateFilmliste() {
        return this.arr[MS_Konstanten.SUCHEN_LISTE_WIE_NR].equals(MS_Konstanten.SUCHEN_UPDATE);
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
