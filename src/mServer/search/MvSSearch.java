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
package mServer.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import mSearch.Config;
import mSearch.MSearch;
import mSearch.tool.Log;
import mServer.daten.MvSSearchTask;
import mServer.tool.MvSDaten;
import mServer.tool.MvSKonstanten;
import mServer.tool.MvSLog;

public class MvSSearch {

    MSearch mSearch;

    public MvSSearch() {
        this.mSearch = null;
        Config.dirFilme = MvSDaten.getVerzeichnisFilme();
    }

    public boolean filmeSuchen(MvSSearchTask aktSearchTask) {
        boolean ret = true;
        try {
            // ===========================================
            // den nächsten Suchlauf starten
            MvSLog.systemMeldung("");
            MvSLog.systemMeldung("-----------------------------------");
            MvSLog.systemMeldung("Filmsuche starten");
            mSearch = new MSearch();

            // was und wie
            Config.senderLoadHow = aktSearchTask.loadHow();
            Config.updateFilmliste = aktSearchTask.updateFilmliste();
            Config.nurSenderLaden = arrLesen(aktSearchTask.arr[MvSSearchTask.SUCHEN_SENDER_NR].trim());
            Config.orgFilmlisteErstellen = aktSearchTask.orgListeAnlegen();
            Config.orgFilmliste = MvSDaten.system[MvSKonstanten.SYSTEM_FILMLISTE_ORG_NR];

            // live-steams
            Config.importLive = MvSDaten.system[MvSKonstanten.SYSTEM_IMPORT_LIVE_NR];

            // und noch evtl. ein paar Imports von Filmlisten anderer Server
            Config.importUrl_1__anhaengen = MvSDaten.system[MvSKonstanten.SYSTEM_IMPORT_URL_1_NR];
            Config.importUrl_2__anhaengen = MvSDaten.system[MvSKonstanten.SYSTEM_IMPORT_URL_2_NR];

            // für die alte Filmliste
            Config.importOld = MvSDaten.system[MvSKonstanten.SYSTEM_IMPORT_OLD_NR];

            // Rest
            Config.setUserAgent(MvSDaten.getUserAgent());
            Config.proxyUrl = MvSDaten.system[MvSKonstanten.SYSTEM_PROXY_URL_NR];
            Config.proxyPort = MvSDaten.getProxyPort();
            Config.debug = MvSDaten.debug;

            Log.setLogfile(MvSDaten.getLogDatei(MvSKonstanten.LOG_FILE_NAME_MSEARCH));

            Thread t = new Thread(mSearch);
            t.start();
            MvSLog.systemMeldung("Filme suchen gestartet");
            // ===========================================
            // warten auf das Ende
            //int warten = aktSearchTask.allesLaden() == true ? MvSKonstanten.WARTEZEIT_ALLES_LADEN : MvSKonstanten.WARTEZEIT_UPDATE_LADEN;
            int warten = aktSearchTask.getWaitTime()/*Minuten*/;
            MvSLog.systemMeldung("Max Laufzeit[Min]: " + warten);
            MvSLog.systemMeldung("-----------------------------------");

            warten = 1000 * 60 * warten;
            t.join(warten);

            // ===========================================
            // erst mal schauen ob noch was läuft
            if (t != null) {
                if (t.isAlive()) {
                    MvSLog.fehlerMeldung(915147623, MvSSearch.class.getName(), "Der letzte Suchlauf läuft noch");
                    if (mSearch != null) {
                        MvSLog.systemMeldung("");
                        MvSLog.systemMeldung("");
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("und wird jetzt gestoppt");
                        MvSLog.systemMeldung("Zeit: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("");
                        //und jetzt STOPPEN!!!!!!!!
                        mSearch.stop();
                    }

                    t.join(15 * 60 * 1000); // 15 Minuten warten, das Erstellen/Komprimieren der Liste dauert
                    if (t.isAlive()) {
                        MvSLog.systemMeldung("");
                        MvSLog.systemMeldung("");
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("und noch gekillt");
                        MvSLog.systemMeldung("Zeit: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("================================");
                        MvSLog.systemMeldung("");
                        ret = false;
                    }
                    //jetzt ist Schicht im Schacht
                    t.stop();
                }
            }
        } catch (Exception ex) {
            MvSLog.fehlerMeldung(636987308, MvSSearch.class.getName(), "filmeSuchen", ex);
        }
        int l = mSearch.getListeFilme().size();
        MvSLog.systemMeldung("");
        MvSLog.systemMeldung("");
        MvSLog.systemMeldung("================================");
        MvSLog.systemMeldung("Filmliste Anzahl Filme: " + l);
        if (l < 10_000) {
            //dann hat was nicht gepasst
            MvSLog.systemMeldung("   Fehler!!");
            MvSLog.systemMeldung("================================");
            ret = false;
        } else {
            MvSLog.systemMeldung("   dann ist alles OK");
            MvSLog.systemMeldung("================================");

        }
        MvSLog.systemMeldung("filmeSuchen beendet");
        mSearch = null;
        return ret;
    }

    private String[] arrLesen(String s) {
        ArrayList<String> arr = new ArrayList<>();
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
