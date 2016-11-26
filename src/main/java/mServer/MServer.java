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

import mServer.daten.MserverSearchTask;
import mServer.search.MserverSearch;
import mServer.tool.MserverDaten;
import mServer.tool.MserverKonstanten;
import mServer.tool.MserverLog;
import mServer.tool.MserverTimer;
import mServer.tool.MserverXmlLesen;
import mServer.upload.MserverUpload;

public class MServer {

    private MserverSearchTask aktSearchTask = null;
    private boolean suchen = false;

    public boolean isSuchen() {
        return suchen;
    }

    private MserverSearch mvsSearch;

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
        MserverDaten.init();
        MserverDaten.setBasisVerzeichnis(pfad);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                MserverLog.fehlerMeldung(97986523, MServer.class.getName(), new String[] { "Da hat sich ein Thread verabschiedet: " + t.getName(), e.getMessage() });
                e.printStackTrace();
            }
        });
    }

    public boolean starten() {
        if (!MserverDaten.konfigExistiert()) {
            MserverLog.fehlerMeldung(858589654, MServer.class.getName(), new String[] { "Konfig-Datei existiert nicht", MserverDaten.getKonfigDatei() });
            System.exit(1); // und Tschüss
        } else {
            MserverXmlLesen.xmlDatenLesen();
            if (MserverDaten.system[MserverKonstanten.SYSTEM_DEBUG_NR].equals(MserverKonstanten.STR_TRUE)) {
                MserverDaten.debug = true;
            }
            if (MserverDaten.system[MserverKonstanten.SYSTEM_RESTART_AFTER_RUN_NR].equals(MserverKonstanten.STR_TRUE)) {
                MserverDaten.restart = true;
            }

            // Infos schreiben
            MserverLog.startMeldungen(this.getClass().getName());

            mvsSearch = new MserverSearch();
            MserverTimer timer = new MserverTimer(this);
            Thread thread = new Thread(timer);
            thread.start();
            try {
                // wait for the thread to finish
                thread.join();
                if (MserverDaten.restart) {
                    return true;
                }
            } catch (InterruptedException e) {
                MserverLog.fehlerMeldung(42, MServer.class.getName(), "Fehler bei thread.join()", e);
            }
        }
        return false;
    }

    public void laufen() {
        // =====================================
        // erst mal schauen ob was zu tun ist
        // -----------------------------------
        if (aktSearchTask == null) {
            aktSearchTask = MserverDaten.listeSuchen.erste();
            if (aktSearchTask == null) {
                // fertig für den Tag
                undTschuess();
                return;
            } else {
                aktSearchTask.meldungNaechsterStart();
            }
        }
        if (!suchen && aktSearchTask.starten()) {
            // dann gibts was zu tun
            suchen = true;
            aktSearchTask.meldungStart();

            // Filme suchen
            MserverLog.systemMeldung("");
            MserverLog.systemMeldung("======================================");
            MserverLog.systemMeldung("== Filme suchen ======================");
            MserverLog.systemMeldung("--------------------------------------");
            if (!mvsSearch.filmeSuchen(aktSearchTask)) {
                // das Suchen der Filme hat nicht geklappt
                MserverLog.systemMeldung("--------------------------------------");
                MserverLog.systemMeldung("== Fehler beim Filme Suchen ==========");
                MserverLog.systemMeldung("-------------------------------");
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("");

            } else {
                // Suchen war OK
                MserverLog.systemMeldung("== Filme Suchen beendet =======");
                MserverLog.systemMeldung("-------------------------------");
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("");

                // nur dann gibts was zum Hochladen
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("===============================");
                MserverLog.systemMeldung("== Upload =====================");

                MserverUpload.upload(aktSearchTask);
                MserverLog.systemMeldung("== Upload beendet =============");
                MserverLog.systemMeldung("-------------------------------");
                MserverLog.systemMeldung("---------------------------------------");
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("");
            }
            aktSearchTask = null;
            suchen = false;
        }
    }

    private void undTschuess() {
        MserverLog.printEndeMeldung();
        // we need to interrupt the current thread to know that we should stop for today
        Thread.currentThread().interrupt();
    }
}
