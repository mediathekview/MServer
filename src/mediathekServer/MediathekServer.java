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
import mediathekServer.search.MS_FilmeSuchen;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;
import mediathekServer.tool.MS_Timer;
import mediathekServer.tool.MS_XmlLesen;
import mediathekServer.update.MS_Update;
import mediathekServer.upload.MS_Upload;

public class MediathekServer {

    private MS_Timer timer;
    private MS_Daten msDaten;
    private MS_DatenSuchen aktDatenSuchen = null;
    private boolean suchen = false;
    MS_FilmeSuchen msFilmeSuchen;
    MS_Upload msUpload;
    boolean nachUpdate = false;

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
                nachUpdate = true;
            }
        }
        msDaten = new MS_Daten();
        MS_Daten.setBasisVerzeichnis(pfad);
        msFilmeSuchen = new MS_FilmeSuchen();
        msUpload = new MS_Upload();
        if (nachUpdate) {
            MS_Log.systemMeldung("");
            MS_Log.systemMeldung("===========================");
            MS_Log.systemMeldung("== Nach einem Update ======");
            // tu was zu tun ist
            //
            MS_Log.systemMeldung("---------------------------");

        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                MS_Log.fehlerMeldung(97986523, MediathekServer.class.getName(), "Da hat sich ein Thread verabschiedet");
            }
        });
    }

    public void starten() {
        if (!MS_Daten.konfigExistiert()) {
            MS_Log.fehlerMeldung(858589654, MediathekServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MS_Daten.getKonfigDatei()});
            System.exit(0); // und Tschüss
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
            aktDatenSuchen.meldungStart();
            // ----------------------
            // Filme suchen
            MS_Log.systemMeldung("");
            MS_Log.systemMeldung("==============================");
            MS_Log.systemMeldung("== Filme suchen ==============");
            if (!msFilmeSuchen.filmeSuchen(aktDatenSuchen)) {
                // das Suchen der Filme hat nicht geklappt
                MS_Log.systemMeldung("== Fehler beim Filme Suchen ==");
                MS_Log.systemMeldung("==============================");
                MS_Log.systemMeldung("------------------------------");
                MS_Log.systemMeldung("");
            } else {
                // nur dann gibts was zum Hochladen
                MS_Log.systemMeldung("-----------------------------");
                MS_Log.systemMeldung("");
                // ----------------------
                // Filme hochladen
                MS_Log.systemMeldung("");
                MS_Log.systemMeldung("=============================");
                MS_Log.systemMeldung("== Upload ===================");
                msUpload.upload(aktDatenSuchen);
                MS_Log.systemMeldung("-----------------------------");
                MS_Log.systemMeldung("");
            }
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
