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
package mediathekServer.search;

import java.util.ArrayList;
import mediathek.MediathekNoGui;
import mediathek.tool.GuiFunktionen;
import mediathekServer.daten.MS_DatenSuchen;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_FilmeSuchen {

    public boolean filmeSuchen(MS_DatenSuchen aktDatenSuchen) {
        boolean ret = true;
        String filmDateiName = aktDatenSuchen.getZielDateiName();
        String filmDateiPfad = MS_Daten.getVerzeichnisFilme();
        String sender[] = arrLesen(aktDatenSuchen.arr[MS_Konstanten.SUCHEN_SENDER_NR].trim());
        String importUrlExtend = MS_Daten.system[MS_Konstanten.SYSTEM_IMPORT_URL_EXTEND_NR].toString();
        String importUrlReplace = MS_Daten.system[MS_Konstanten.SYSTEM_IMPORT_URL_REPLACE_NR].toString();
        try {
            // ===========================================
            // den nächsten Suchlauf starten
            MS_Log.systemMeldung("");
            MS_Log.systemMeldung("-----------------------------------");
            MS_Log.systemMeldung("Filmsuche starten");
            MediathekNoGui mediathekNoGui = new MediathekNoGui(MS_Daten.getBasisVerzeichnis(),
                    aktDatenSuchen.allesLaden(),
                    aktDatenSuchen.updateFilmliste(),
                    GuiFunktionen.addsPfad(filmDateiPfad, filmDateiName),
                    importUrlExtend,
                    importUrlReplace,
                    MS_Daten.getUserAgent(),
                    MS_Daten.getLogDatei_mediathekView(),
                    MS_Daten.debug);
            mediathekNoGui.init(sender);
            Thread t = new Thread(mediathekNoGui);
            t.start();
            MS_Log.systemMeldung("Filme suchen gestartet");
            // ===========================================
            // warten auf das Ende
            int warten = aktDatenSuchen.allesLaden() == true ? MS_Konstanten.WARTEZEIT_ALLES_LADEN : MS_Konstanten.WARTEZEIT_UPDATE_LADEN;
            t.join(warten);
            // ===========================================
            // erst mal schauen ob noch was läuft
            if (t != null) {
                if (t.isAlive()) {
                    MS_Log.fehlerMeldung(915147623, MS_FilmeSuchen.class.getName(), "Der letzte Suchlauf läuft noch");
                    if (mediathekNoGui != null) {
                        MS_Log.systemMeldung("und wird jetzt gestoppt");
                        mediathekNoGui.stoppen();
                    }
                    t.join(10 * 1000); // 10 Sekunden wegen der 5 Sekunden in MediathekNoGui.run
                    if (t.isAlive()) {
                        MS_Log.systemMeldung("und noch gekillt");
                        ret = false;
                    }
                    // nach 3 Sekunden ist Schicht im Schacht
                    t.stop();
                }
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(636987308, MS_FilmeSuchen.class.getName(), "filmeSuchen", ex);
        }
        MS_Log.systemMeldung("filmeSuchen beendet");
        return ret;
    }

    private String[] arrLesen(String s) {
        ArrayList<String> arr = new ArrayList<String>();
        String tmp = "";
        s = s.trim();
        if (s.equals("")) {
            return null;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == ',') {
                if (!tmp.equals("")) {
                    arr.add(tmp);
                }
                tmp = "";
            } else {
                tmp += s.charAt(i);
            }
        }
        if (!tmp.equals("")) {
            arr.add(tmp);
        }
        return arr.toArray(new String[]{});
    }
}
