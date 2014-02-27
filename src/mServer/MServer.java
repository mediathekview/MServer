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
package mServer;

import java.io.File;
import mServer.daten.MSVSearchTask;
import mServer.search.MSVSearch;
import mServer.tool.MSVDaten;
import mServer.tool.MSVKonstanten;
import mServer.tool.MSVLog;
import mServer.tool.MSVTimer;
import mServer.tool.MSVXmlLesen;
import mServer.update.MSVUpdate;
import mServer.upload.MSVUpload;

public class MServer {

    private MSVTimer timer;
    private MSVSearchTask aktDatenSuchen = null;
    private boolean suchen = false;
    MSVSearch mServerSearch;
    boolean nachUpdate = false;
    boolean nurUpload = false;

    public MServer(String[] ar) {
        String pfad = "";
        if (ar != null) {
            if (ar.length > 0) {
                if (!ar[0].startsWith("-")) {
                    pfad = ar[0];
                    if (!pfad.endsWith(File.separator)) {
                        pfad += File.separator;
                    }
                }
            }
        }
        for (String s : ar) {
            if (s.equalsIgnoreCase("-update")) {
                nachUpdate = true;
            }
            if (s.equalsIgnoreCase("-upload")) {
                nurUpload = true;
            }
        }
        MSVDaten.init();
        MSVDaten.setBasisVerzeichnis(pfad);

        if (nachUpdate) {
            MSVLog.systemMeldung("");
            MSVLog.systemMeldung("===========================");
            MSVLog.systemMeldung("== Nach einem Update ======");
            // tu was zu tun ist
            //
            MSVLog.systemMeldung("---------------------------");

        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                MSVLog.fehlerMeldung(97986523, MServer.class.getName(), new String[]{"Da hat sich ein Thread verabschiedet: " + t.getName(), e.getMessage()});
                e.printStackTrace();
            }
        });
    }

    public void starten() {
        if (!MSVDaten.konfigExistiert()) {
            MSVLog.fehlerMeldung(858589654, MServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MSVDaten.getKonfigDatei()});
            System.exit(0); // und Tschüss
        } else {
            MSVXmlLesen.xmlDatenLesen();
            if (MSVDaten.system[MSVKonstanten.SYSTEM_DEBUG_NR].equals(MSVKonstanten.STR_TRUE)) {
                MSVDaten.debug = true;
                MSVLog.systemMeldung("== Debug on ======");
            }
            // Infos schreiben
            MSVLog.startMeldungen(this.getClass().getName());
            updateSuchen(); // erst mal schauen was es neues gibt
            mServerSearch = new MSVSearch();
            timer = new MSVTimer() {
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

////    public void urlLoeschen(String url) {
////        if (!MServerDaten.konfigExistiert()) {
////            MServerLog.fehlerMeldung(858589654, MServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MServerDaten.getKonfigDatei()});
////            System.exit(0); // und Tschüss
////        } else {
////            MServerXmlLesen.xmlDatenLesen();
////            if (MServerDaten.system[MServerKonstanten.SYSTEM_DEBUG_NR].equals(MServerKonstanten.STR_TRUE)) {
////                MServerDaten.debug = true;
////                MServerLog.systemMeldung("== Debug on ======");
////            }
////            // Infos schreiben
////            MServerLog.startMeldungen(this.getClass().getName());
////            MServerLog.systemMeldung("== FilmUrl löschen ======");
////            MServerLog.systemMeldung("Url: " + url);
////            MServerMelden.updateServerLoeschen(url,"");
////        }
////    }
////
////    public void senderLoeschen(String sender, String filmdatei) {
////        try {
////            if (sender.isEmpty()) {
////                MServerLog.fehlerMeldung(936251478, MServer.class.getName(), "Kein Sender zum löschen angegeben!");
////                System.exit(0); // und Tschüss
////            } else if (filmdatei.isEmpty()) {
////                MServerLog.fehlerMeldung(732657910, MServer.class.getName(), "Keine Filmlistendatei angegeben!");
////                System.exit(0); // und Tschüss
////            } else {
////                Search.senderLoeschenUndExit(sender, filmdatei);
////            }
////        } catch (Exception ex) {
////            MServerLog.fehlerMeldung(915263470, MServer.class.getName(), "Search.senderLoeschen()", ex);
////        }
////    }
    public void laufen() {
        // =====================================
        // erst mal schauen ob was zu tun ist
        // -----------------------------------
        if (aktDatenSuchen == null) {
            aktDatenSuchen = MSVDaten.listeSuchen.erste();
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
            MSVLog.systemMeldung("");
            MSVLog.systemMeldung("======================================");
            MSVLog.systemMeldung("== Filme suchen ======================");
            MSVLog.systemMeldung("--------------------------------------");
            if (!mServerSearch.filmeSuchen(aktDatenSuchen)) {
                // das Suchen der Filme hat nicht geklappt
                MSVLog.systemMeldung("--------------------------------------");
                MSVLog.systemMeldung("== Fehler beim Filme Suchen ==========");
                MSVLog.systemMeldung("-------------------------------");
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("");
            } else {
                // Suchen war OK
                MSVLog.systemMeldung("== Filme Suchen beendet =======");
                MSVLog.systemMeldung("-------------------------------");
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("");
                // nur dann gibts was zum Hochladen
                // Filme jetzt hochladen
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("===============================");
                MSVLog.systemMeldung("== Upload =====================");
                MSVUpload.upload(aktDatenSuchen);
                MSVLog.systemMeldung("== Upload beendet =============");
                MSVLog.systemMeldung("-------------------------------");
                MSVLog.systemMeldung("---------------------------------------");
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("");
            }
            aktDatenSuchen = null;
            suchen = false;
            // ----------------------
            // nach Programmupdate suchen
            updateSuchen();
        }

    }

    private void updateSuchen() {
        MSVLog.systemMeldung("");
        MSVLog.systemMeldung("=======================================");
        MSVLog.systemMeldung("== Programmupdate suchen ==============");
        if (MSVDaten.system[MSVKonstanten.SYSTEM_UPDATE_SUCHEN_NR].equals(MSVKonstanten.STR_TRUE)) {
            if (MSVUpdate.updaten()) {
                System.exit(MSVKonstanten.PROGRAMM_EXIT_CODE_UPDATE);
            }
        }
        MSVLog.systemMeldung("== Programmupdate suchen beendet ======");
        MSVLog.systemMeldung("---------------------------------------");
        MSVLog.systemMeldung("");
        MSVLog.systemMeldung("");
    }

    private void undTschuess() {
        MSVLog.printEndeMeldung();
        System.exit(0);
    }
}
