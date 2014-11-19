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
package mvServer;

import java.io.File;
import mvServer.daten.MvSSearchTask;
import mvServer.search.MvSSearch;
import mvServer.tool.MvSDaten;
import mvServer.tool.MvSKonstanten;
import mvServer.tool.MvSLog;
import mvServer.tool.MvSTimer;
import mvServer.tool.MvSXmlLesen;
import mvServer.upload.MvSUpload;

public class MvServer {

    private MvSTimer timer;
    private MvSSearchTask aktSearchTask = null;
    private boolean suchen = false;
    private MvSSearch mvsSearch;
    boolean nachUpdate = false;
    boolean nurUpload = false;

    public MvServer(String[] ar) {
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
        MvSDaten.init();
        MvSDaten.setBasisVerzeichnis(pfad);

        if (nachUpdate) {
            MvSLog.systemMeldung("");
            MvSLog.systemMeldung("===========================");
            MvSLog.systemMeldung("== Nach einem Update ======");
            // tu was zu tun ist
            //
            MvSLog.systemMeldung("---------------------------");

        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                MvSLog.fehlerMeldung(97986523, MvServer.class.getName(), new String[]{"Da hat sich ein Thread verabschiedet: " + t.getName(), e.getMessage()});
                e.printStackTrace();
            }
        });
    }

    public void starten() {
        if (!MvSDaten.konfigExistiert()) {
            MvSLog.fehlerMeldung(858589654, MvServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MvSDaten.getKonfigDatei()});
            System.exit(0); // und Tschüss
        } else {
            MvSXmlLesen.xmlDatenLesen();
            if (MvSDaten.system[MvSKonstanten.SYSTEM_DEBUG_NR].equals(MvSKonstanten.STR_TRUE)) {
                MvSDaten.debug = true;
            }
            // Infos schreiben
            MvSLog.startMeldungen(this.getClass().getName());
            if (MvSDaten.debug) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("== Debug on ======");
                MvSLog.systemMeldung("");
            }
            mvsSearch = new MvSSearch();
            timer = new MvSTimer() {
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
        if (aktSearchTask == null) {
            aktSearchTask = MvSDaten.listeSuchen.erste();
            if (aktSearchTask == null) {
                // fertig für den Tag
                undTschuess();
            } else {
                aktSearchTask.meldungNaechsterStart();
            }
        }
        if (!suchen && aktSearchTask.starten()) {
            suchen = true;
            aktSearchTask.meldungStart();
            // ----------------------
            // Filme suchen
            MvSLog.systemMeldung("");
            MvSLog.systemMeldung("======================================");
            MvSLog.systemMeldung("== Filme suchen ======================");
            MvSLog.systemMeldung("--------------------------------------");
            if (!mvsSearch.filmeSuchen(aktSearchTask)) {
                // das Suchen der Filme hat nicht geklappt
                MvSLog.systemMeldung("--------------------------------------");
                MvSLog.systemMeldung("== Fehler beim Filme Suchen ==========");
                MvSLog.systemMeldung("-------------------------------");
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("");
            } else {
                // Suchen war OK
                MvSLog.systemMeldung("== Filme Suchen beendet =======");
                MvSLog.systemMeldung("-------------------------------");
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("");
                // nur dann gibts was zum Hochladen
                // Filme jetzt hochladen
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("===============================");
                MvSLog.systemMeldung("== Upload =====================");
                MvSUpload.upload(aktSearchTask);
                MvSLog.systemMeldung("== Upload beendet =============");
                MvSLog.systemMeldung("-------------------------------");
                MvSLog.systemMeldung("---------------------------------------");
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("");
            }
            aktSearchTask = null;
            suchen = false;
        }
    }

    private void undTschuess() {
        MvSLog.printEndeMeldung();
        System.exit(0);
    }
}
