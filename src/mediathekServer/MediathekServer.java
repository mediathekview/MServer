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
package mediathekServer;

import java.io.File;
import mediathekServer.daten.MS_DatenSuchen;
import mediathekServer.cron.MS_Timer;
import mediathekServer.search.MS_FilmeSuchen;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;
import mediathekServer.tool.MS_Test;
import mediathekServer.tool.MS_XmlLesen;
import mediathekServer.tool.MS_XmlSchreiben;
import mediathekServer.update.MS_Update;

public class MediathekServer {

    private String output = "filme.bz2";
    private String imprtUrl = "";
    private MS_DatenSuchen aktDatenSuchen = null;
    private MS_Timer timer;
    private MS_Daten msDaten;

    public MediathekServer() {
    }

    public MediathekServer(String[] ar) {
        String pfad = "";
        if (ar != null) {
            if (ar.length > 0) {
                if (!ar[0].startsWith("-")) {
                    if (!ar[0].endsWith(File.separator)) {
                        ar[0] += File.separator;
                    }
                    pfad = ar[0];
                }
            }
        }
        for (String s : ar) {
            if (s.equalsIgnoreCase("-update")) {
                // tu was zu tun ist
                MS_Log.systemMeldung("tu was zu tun ist");
            }
        }
        msDaten = new MS_Daten();
        MS_Daten.setBasisVerzeichnis(pfad);
        // Infos schreiben
        MS_Log.startMeldungen(this.getClass().getName());
        MS_Log.systemMeldung("");
        MS_Log.systemMeldung("");
    }

    public void starten() {
        // los gehts
        if (!MS_Daten.konfigExistiert()) {
            MS_Log.fehlerMeldung(858589654, MediathekServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MS_Daten.getKonfigDatei()});
            musterSchreiben(); // und Tschüss
        } else {
            MS_XmlLesen.xmlDatenLesen();
            MS_XmlLesen.xmlLogLesen();
            aktDatenSuchen = MS_Daten.listeSuchen.erste();
            timer = new MS_Timer() {
                @Override
                public void ping() {
/////////                    laufen();
                }
            };
            new Thread(timer).start();
            laufen();
        }
    }

    public void musterSchreiben() {
        MS_Log.systemMeldung("Muster Konfig anlegen");
        // Demo schreiben
        MS_XmlSchreiben.xmlMusterDatenSchreiben();
        // und Tschüss
        System.exit(0);
    }

    public void laufen() {
////////        if (aktDatenSuchen == null) {
////////            // erst mal schauen obs was zum tun gibt
////////            aktDatenSuchen = MS_Daten.listeSuchen.naechstes();
////////        }
////////        if (aktDatenSuchen == null) {
////////            // fertig für den Tag
////////            undTschuess();
////////        }
////////        if (aktDatenSuchen.starten()) {
        // wieder ein Durchlauf fällig
        // ---------------------------
        // Update suchen
        MS_Log.systemMeldung("===========================");
        MS_Log.systemMeldung("Update");
        if (MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_SUCHEN_NR].equals(MS_Konstanten.STR_TRUE)) {
            if (MS_Update.updaten()) {
                System.exit(MS_Konstanten.PROGRAMM_EXIT_CODE_UPDATE);
            }
        }
        // ---------------------------
        // Filme suchen
        if (!MS_FilmeSuchen.filmeSuchen(aktDatenSuchen.allesLaden(), output, imprtUrl, MS_Daten.getUserAgent())) {
            // war wohl nix
            MS_Log.fehlerMeldung(812370895, MediathekServer.class.getName(), "FilmeSuchen mit Fehler beendet");
            return;
        }
        MS_Test.schreiben(output); /////////////
        // ---------------------------
        // Filmliste hochladen
////////////        MS_Upload.upload(output);
////        }
        undTschuess();

    }

    private void undTschuess() {
        MS_Log.printEndeMeldung();
        MS_XmlSchreiben.xmlLogSchreiben();
        System.exit(0);
    }
}
