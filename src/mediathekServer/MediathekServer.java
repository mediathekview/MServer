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
import mediathekServer.cron.MS_Timer;
import mediathekServer.daten.MS_DatenSuchen;
import mediathekServer.search.MS_FilmeSuchen;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;
import mediathekServer.tool.MS_XmlLesen;
import mediathekServer.tool.MS_XmlSchreiben;
import mediathekServer.update.MS_Update;
import mediathekServer.upload.MS_Upload;

public class MediathekServer {

    private MS_Timer timer;
    private MS_Daten msDaten;
    private MS_DatenSuchen aktDatenSuchen = null;
    private boolean suchen = false;
    MS_FilmeSuchen msFilmeSuchen;
    MS_Upload msUpload;

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
                MS_Log.systemMeldung("");
                MS_Log.systemMeldung("===========================");
                MS_Log.systemMeldung("== Nach einem Update ======");
                // tu was zu tun ist
                //
                MS_Log.systemMeldung("---------------------------");
            }
        }
        msDaten = new MS_Daten();
        MS_Daten.setBasisVerzeichnis(pfad);
        msFilmeSuchen = new MS_FilmeSuchen();
        msUpload = new MS_Upload();
    }

    public void starten() {
        if (!MS_Daten.konfigExistiert()) {
            MS_Log.fehlerMeldung(858589654, MediathekServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MS_Daten.getKonfigDatei()});
            musterSchreiben(); // und Tschüss
        } else {
            MS_XmlLesen.xmlDatenLesen();
            // Infos schreiben
            MS_Log.startMeldungen(this.getClass().getName());
            updateSuchen(); // erst mal schauen was es neues gibt
            timer = new MS_Timer() {
                @Override
                public void ping() {
                    if (!suchen) {
                        // beschäftigt
                        laufen();
                    }
                }
            };
            new Thread(timer).start();
        }
    }

    public void musterSchreiben() {
        // Demo schreiben
        MS_XmlSchreiben.xmlMusterDatenSchreiben();
        // und Tschüss
        System.exit(0);
    }

    public void laufen() {
        // =====================================
        // erst mal schauen ob was zu tun ist
        // -----------------------------------
        if (aktDatenSuchen == null) {
            aktDatenSuchen = MS_Daten.listeSuchen.erste();
            if (aktDatenSuchen == null) {
                // fertig für den Tag
                undTschuess();
            } else {
                aktDatenSuchen.meldungNaechsterStart();
            }
        }
        if (!suchen && aktDatenSuchen.starten()) {
            suchen = true;
            aktDatenSuchen.MeldungStart();
            // ----------------------
            // Filme suchen
            MS_Log.systemMeldung("");
            MS_Log.systemMeldung("===========================");
            MS_Log.systemMeldung("== Filme suchen ===========");
            msFilmeSuchen.filmeSuchen(aktDatenSuchen);
            MS_Log.systemMeldung("---------------------------");
            MS_Log.systemMeldung("");
            // ----------------------
            // Filme hochladen
            MS_Log.systemMeldung("");
            MS_Log.systemMeldung("===========================");
            MS_Log.systemMeldung("== Upload =================");
            msUpload.upload(aktDatenSuchen);
            MS_Log.systemMeldung("---------------------------");
            MS_Log.systemMeldung("");
            aktDatenSuchen = null;
            suchen = false;
            // ----------------------
            // nach Programmupdate suchen
            updateSuchen();
        }
    }

    private void updateSuchen() {
        MS_Log.systemMeldung("");
        MS_Log.systemMeldung("================================");
        MS_Log.systemMeldung("== Programmupdate suchen =======");
        if (MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_SUCHEN_NR].equals(MS_Konstanten.STR_TRUE)) {
            if (MS_Update.updaten()) {
                System.exit(MS_Konstanten.PROGRAMM_EXIT_CODE_UPDATE);
            }
        }
        MS_Log.systemMeldung("---------------------------");
        MS_Log.systemMeldung("");
    }

    private void undTschuess() {
        MS_Log.printEndeMeldung();
        System.exit(0);
    }
}
