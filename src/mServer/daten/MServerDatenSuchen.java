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
import mServer.tool.MServerLog;

public class MServerDatenSuchen {

    // Konstanten Suchen
    public static final String SUCHEN_ALLES = "alles";
    public static final String SUCHEN_UPDATE = "update";
    public static final String SUCHEN_NEU = "neu";
    public static final String SUCHEN_WANN_SOFORT = "sofort";
    //
    public static final String SUCHEN_SENDER_WIE = "suchen-sender-wie"; // "alles" - dann alles suchen, sonst nur update
    public static final int SUCHEN_SENDER_WIE_NR = 0;
    public static final String SUCHEN_LISTE_WIE = "suchen-liste-wie"; // "neu" - dann neue Liste erstellen, sonst update
    public static final int SUCHEN_LISTE_WIE_NR = 1;
    public static final String SUCHEN_WANN = "suchen-wann";
    public static final int SUCHEN_WANN_NR = 2;
    public static final String SUCHEN_SENDER = "suchen-sender";
    public static final int SUCHEN_SENDER_NR = 3;
    public static final String SUCHEN_ORG_LISTE = "suchen-org-liste"; // dann wird eine Orgliste erstellt, darauf beziehen sich dann die diff-Files
    public static final int SUCHEN_ORG_LISTE_NR = 4;
    // Array
    public static final String SUCHEN = "suchen";
    public static final int SUCHEN_MAX_ELEM = 5;
    public static final String[] SUCHEN_COLUMN_NAMES = {SUCHEN_SENDER_WIE, SUCHEN_LISTE_WIE, SUCHEN_WANN, SUCHEN_SENDER, SUCHEN_ORG_LISTE};
    public String[] arr = new String[SUCHEN_MAX_ELEM];

    public MServerDatenSuchen() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public void meldungNaechsterStart() {
        MServerLog.systemMeldung("naechster Start: " + this.arr[SUCHEN_WANN_NR]);
    }

    public void meldungStart() {
        MServerLog.systemMeldung("Starten: " + this.arr[SUCHEN_WANN_NR]);
        MServerLog.systemMeldung("Suchen Sender wie:  " + this.arr[SUCHEN_SENDER_WIE_NR]);
        MServerLog.systemMeldung("Suchen Liste wie:  " + (updateFilmliste() ? "nur ein Update" : "neue Filmliste"));
        if (!this.arr[SUCHEN_SENDER_NR].equals("")) {
            MServerLog.systemMeldung("Sender:  " + this.arr[SUCHEN_SENDER_NR]);
        }
    }

    public boolean jetzt() {
        return this.arr[SUCHEN_WANN_NR].equals(SUCHEN_WANN_SOFORT);
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

    public String getExportFilmlisteXml() {
        final String FILM_DATEI_SUFF = "bz2";
        final String FILMDATEI_NAME = "Filmliste-xml";
        if (jetzt()) {
            return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
        } else {
            return FILMDATEI_NAME + "_" + arr[SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF;
        }
    }

    public String getExportFilmlisteJson() {
        
        //final String FILM_DATEI_SUFF = "bz2";
        final String FILM_DATEI_SUFF = "xz";
        final String FILMDATEI_NAME = "Filmliste-json";
        if (jetzt()) {
            return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
        } else {
            return FILMDATEI_NAME + "_" + arr[SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF;
        }
    }

//    public String getAktFilmliste() {
//        final String FILM_DATEI_SUFF = "json";
//        final String FILMDATEI_NAME = "filme";
//        return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
//    }

//    public String getOrgFilmliste() {
//        // ist die erste Filmliste am Tag gege die dann das diff erstellt wird
//        final String FILM_DATEI_SUFF = "json";
//        final String FILMDATEI_NAME = "filme-org";
//        return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
//    }

    public String getExportOrgFilmliste() {
        final String FILM_DATEI_SUFF = "xz";
        final String FILMDATEI_NAME = "Filmliste-org";
        return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
    }

//    public String getDiffFilmliste() {
//        // ist dann das diff das erstellt wird
//        final String FILM_DATEI_SUFF = "json";
//        final String FILMDATEI_NAME = "filme-diff";
//        return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
//    }

//    public String getExportDiffFilmliste() {
//        // ist dann das diff das erstellt wird
//        final String FILM_DATEI_SUFF = "xz";
//        final String FILMDATEI_NAME = "filme-diff";
//        return FILMDATEI_NAME + "." + FILM_DATEI_SUFF;
//    }

    public boolean orgListeAnlegen() {
        return Boolean.parseBoolean(arr[SUCHEN_ORG_LISTE_NR]);
    }

    public Date getTimeHeute() {
        Date ret;
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("dd.MM.yyyy__HH:mm");
        try {
            return sdf_zeit.parse(MServerDatumZeit.getHeute() + "__" + this.arr[SUCHEN_WANN_NR]);
        } catch (Exception ex) {
            ret = null;
            MServerLog.fehlerMeldung(825439079, MServerDatenSuchen.class.getName(), "getTime", ex);
        }
        return ret;
    }

    public boolean allesLaden() {
        return this.arr[SUCHEN_SENDER_WIE_NR].equals(SUCHEN_ALLES);
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
