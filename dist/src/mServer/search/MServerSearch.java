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

import java.util.ArrayList;
import java.util.Iterator;
import mServer.daten.MServerDatenSuchen;
import mServer.daten.MServerDatenUpload;
import mServer.tool.MServerDaten;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import mServer.tool.MServerWarten;
import mServer.upload.MServerMelden;
import msearch.Search;
import msearch.daten.MSearchConfig;
import msearch.tool.GuiFunktionen;

public class MServerSearch {

    Search mSearch = null;
    String sender[] = null;

    public boolean filmeSuchen(MServerDatenSuchen aktDatenSuchen) {
        boolean ret = true;
        String exportFilmliste = GuiFunktionen.addsPfad(MServerDaten.getVerzeichnisFilme(), aktDatenSuchen.getExportFilmliste());
        String aktFilmliste = GuiFunktionen.addsPfad(MServerDaten.getVerzeichnisFilme(), aktDatenSuchen.getAktFilmliste());
        sender = arrLesen(aktDatenSuchen.arr[MServerDatenSuchen.SUCHEN_SENDER_NR].trim());
        String importUrlExtend = MServerDaten.system[MServerKonstanten.SYSTEM_IMPORT_URL_EXTEND_NR].toString();
        String importUrlReplace = MServerDaten.system[MServerKonstanten.SYSTEM_IMPORT_URL_REPLACE_NR].toString();
        vorherLoeschen();
        try {
            // ===========================================
            // den nächsten Suchlauf starten
            MServerLog.systemMeldung("");
            MServerLog.systemMeldung("-----------------------------------");
            MServerLog.systemMeldung("Filmsuche starten");
            mSearch = new Search(new String[]{});
            MSearchConfig.senderAllesLaden = aktDatenSuchen.allesLaden();
            MSearchConfig.updateFilmliste = aktDatenSuchen.updateFilmliste();
            MSearchConfig.dateiFilmliste = aktFilmliste;
            MSearchConfig.exportFilmliste = exportFilmliste;
            MSearchConfig.nurSenderLaden = sender;
            MSearchConfig.importUrl__anhaengen = importUrlExtend;
            MSearchConfig.importUrl__ersetzen = importUrlReplace;
            MSearchConfig.setUserAgent(MServerDaten.getUserAgent());
            MSearchConfig.proxyUrl = MServerDaten.system[MServerKonstanten.SYSTEM_PROXY_URL_NR];
            MSearchConfig.proxyPort = MServerDaten.getProxyPort();
            MSearchConfig.debug = MServerDaten.debug;

            Thread t = new Thread(mSearch);
            t.start();
            MServerLog.systemMeldung("Filme suchen gestartet");
            // ===========================================
            // warten auf das Ende
            int warten = aktDatenSuchen.allesLaden() == true ? MServerKonstanten.WARTEZEIT_ALLES_LADEN : MServerKonstanten.WARTEZEIT_UPDATE_LADEN;
            t.join(warten);
            // ===========================================
            // erst mal schauen ob noch was läuft
            if (t != null) {
                if (t.isAlive()) {
                    MServerLog.fehlerMeldung(915147623, MServerSearch.class.getName(), "Der letzte Suchlauf läuft noch");
                    if (mSearch != null) {
                        MServerLog.systemMeldung("und wird jetzt gestoppt");
                        MSearchConfig.setStop();
                    }
                    t.join(10 * 1000); // 10 Sekunden wegen der 5 Sekunden in MediathekNoGui.run
                    if (t.isAlive()) {
                        MServerLog.systemMeldung("und noch gekillt");
                        ret = false;
                    }
                    // nach 3 Sekunden ist Schicht im Schacht
                    t.stop();
                }
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(636987308, MServerSearch.class.getName(), "filmeSuchen", ex);
        }
        MServerLog.systemMeldung("filmeSuchen beendet");
        return ret;
    }

    private void vorherLoeschen() {
        // so braucht der Buildserver während des Suchens von keine Downloads anbieten
        try {
            Iterator<MServerDatenUpload> it = MServerDaten.listeUpload.iterator();
            while (it.hasNext()) {
                MServerDatenUpload datenUpload = it.next();
                if (datenUpload.vorherLoeschen()) {
                    new MServerWarten().sekundenWarten(2);// damit der Server nicht stolpert, max alle 2 Sekunden
                    MServerMelden.updateServerLoeschen("", datenUpload.arr[MServerDatenUpload.UPLOAD_URL_FILMLISTE_NR]);
                }
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(915152369, MServerSearch.class.getName(), "vorherLoeschen", ex);
        }
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
