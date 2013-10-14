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
import mServer.daten.MServerDatenSuchen;
import mServer.search.MServerSearch;
import mServer.tool.MServerDaten;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import mServer.tool.MServerTimer;
import mServer.tool.MServerXmlLesen;
import mServer.update.MServerUpdate;
import mServer.upload.MServerMelden;
import mServer.upload.MServerUpload;
import msearch.Search;

public class MServer {

    private MServerTimer timer;
    private MServerDatenSuchen aktDatenSuchen = null;
    private boolean suchen = false;
    MServerSearch mServerSearch;
    MServerUpload mServerUpload;
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
        MServerDaten.init();
        MServerDaten.setBasisVerzeichnis(pfad);
        mServerSearch = new MServerSearch();
        mServerUpload = new MServerUpload();
        if (nachUpdate) {
            MServerLog.systemMeldung("");
            MServerLog.systemMeldung("===========================");
            MServerLog.systemMeldung("== Nach einem Update ======");
            // tu was zu tun ist
            //
            MServerLog.systemMeldung("---------------------------");

        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                MServerLog.fehlerMeldung(97986523, MServer.class.getName(), new String[]{"Da hat sich ein Thread verabschiedet: " + t.getName(), e.getMessage()});
                e.printStackTrace();
            }
        });
    }

    public void starten() {
        if (!MServerDaten.konfigExistiert()) {
            MServerLog.fehlerMeldung(858589654, MServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MServerDaten.getKonfigDatei()});
            System.exit(0); // und Tschüss
        } else {
            MServerXmlLesen.xmlDatenLesen();
            if (MServerDaten.system[MServerKonstanten.SYSTEM_DEBUG_NR].equals(MServerKonstanten.STR_TRUE)) {
                MServerDaten.debug = true;
                MServerLog.systemMeldung("== Debug on ======");
            }
            // Infos schreiben
            MServerLog.startMeldungen(this.getClass().getName());
            updateSuchen(); // erst mal schauen was es neues gibt
            timer = new MServerTimer() {
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

    public void urlLoeschen(String url) {
        if (!MServerDaten.konfigExistiert()) {
            MServerLog.fehlerMeldung(858589654, MServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MServerDaten.getKonfigDatei()});
            System.exit(0); // und Tschüss
        } else {
            MServerXmlLesen.xmlDatenLesen();
            if (MServerDaten.system[MServerKonstanten.SYSTEM_DEBUG_NR].equals(MServerKonstanten.STR_TRUE)) {
                MServerDaten.debug = true;
                MServerLog.systemMeldung("== Debug on ======");
            }
            // Infos schreiben
            MServerLog.startMeldungen(this.getClass().getName());
            MServerLog.systemMeldung("== FilmUrl löschen ======");
            MServerLog.systemMeldung("Url: " + url);
            MServerMelden.updateServerLoeschen(url,"");
        }
    }

    public void senderLoeschen(String sender, String filmdatei) {
        try {
            if (sender.isEmpty()) {
                MServerLog.fehlerMeldung(936251478, MServer.class.getName(), "Kein Sender zum löschen angegeben!");
                System.exit(0); // und Tschüss
            } else if (filmdatei.isEmpty()) {
                MServerLog.fehlerMeldung(732657910, MServer.class.getName(), "Keine Filmlistendatei angegeben!");
                System.exit(0); // und Tschüss
            } else {
                Search.senderLoeschenUndExit(sender, filmdatei);
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(915263470, MServer.class.getName(), "Search.senderLoeschen()", ex);
        }
    }

    public void laufen() {
        // =====================================
        // erst mal schauen ob was zu tun ist
        // -----------------------------------
        if (aktDatenSuchen == null) {
            aktDatenSuchen = MServerDaten.listeSuchen.erste();
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
            MServerLog.systemMeldung("");
            MServerLog.systemMeldung("======================================");
            MServerLog.systemMeldung("== Filme suchen ======================");
            MServerLog.systemMeldung("--------------------------------------");
            if (!mServerSearch.filmeSuchen(aktDatenSuchen)) {
                // das Suchen der Filme hat nicht geklappt
                MServerLog.systemMeldung("--------------------------------------");
                MServerLog.systemMeldung("== Fehler beim Filme Suchen ==========");
                MServerLog.systemMeldung("-------------------------------");
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("");
            } else {
                // Suchen war OK
                MServerLog.systemMeldung("== Filme Suchen beendet =======");
                MServerLog.systemMeldung("-------------------------------");
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("");
                // nur dann gibts was zum Hochladen
                // Filme jetzt hochladen
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("===============================");
                MServerLog.systemMeldung("== Upload =====================");
                mServerUpload.upload(aktDatenSuchen);
                MServerLog.systemMeldung("== Upload beendet =============");
                MServerLog.systemMeldung("-------------------------------");
                MServerLog.systemMeldung("---------------------------------------");
                MServerLog.systemMeldung("");
                MServerLog.systemMeldung("");
            }
            aktDatenSuchen = null;
            suchen = false;
            // ----------------------
            // nach Programmupdate suchen
            updateSuchen();
        }

    }

    private void updateSuchen() {
        MServerLog.systemMeldung("");
        MServerLog.systemMeldung("=======================================");
        MServerLog.systemMeldung("== Programmupdate suchen ==============");
        if (MServerDaten.system[MServerKonstanten.SYSTEM_UPDATE_SUCHEN_NR].equals(MServerKonstanten.STR_TRUE)) {
            if (MServerUpdate.updaten()) {
                System.exit(MServerKonstanten.PROGRAMM_EXIT_CODE_UPDATE);
            }
        }
        MServerLog.systemMeldung("== Programmupdate suchen beendet ======");
        MServerLog.systemMeldung("---------------------------------------");
        MServerLog.systemMeldung("");
        MServerLog.systemMeldung("");
    }

    private void undTschuess() {
        MServerLog.printEndeMeldung();
        System.exit(0);
    }
}
