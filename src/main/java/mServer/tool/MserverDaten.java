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
package mServer.tool;

import java.io.File;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Functions;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import mServer.daten.MserverListeSuchen;
import mServer.daten.MserverListeUpload;

public class MserverDaten {
    private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
    public static String[] system = new String[MserverKonstanten.SYSTEM_MAX_ELEM];
    public static MserverListeSuchen listeSuchen = new MserverListeSuchen();
    public static MserverListeUpload listeUpload = new MserverListeUpload();
    public static boolean debug = false;
    public static boolean restart = false;
    //
    private static String basisverzeichnis = "";

    public static void init() {
        EtmPoint performancePoint = etmMonitor.createPoint("MserverDaten:init");

        listeSuchen = new MserverListeSuchen();
        listeUpload = new MserverListeUpload();
        debug = false;
        restart = false;
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
        performancePoint.collect();
    }

    public static String getUserAgent() {
        if (system[MserverKonstanten.SYSTEM_USER_AGENT_NR].trim().equals("")) {
            return MserverKonstanten.PROGRAMMNAME + " " + Const.VERSION_FILMLISTE + " / " + Functions.getProgVersion().toString();
        } else {
        }
        return system[MserverKonstanten.SYSTEM_USER_AGENT_NR].trim();
    }

    public static void setBasisVerzeichnis(String b) {
        if (b.isEmpty()) {
            basisverzeichnis = getBasisVerzeichnis(b, true);
        } else {
            basisverzeichnis = b;
        }
    }

    public static int getProxyPort() {
        if (system[MserverKonstanten.SYSTEM_PROXY_PORT_NR].isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(system[MserverKonstanten.SYSTEM_PROXY_PORT_NR]);
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(963487219, MserverDaten.class.getName(), new String[] { "Proxyport falsch: ", system[MserverKonstanten.SYSTEM_PROXY_PORT_NR] });
        }
        return -1;
    }

    public static String getBasisVerzeichnis() {
        return getBasisVerzeichnis(basisverzeichnis, false);
    }

    private static String getBasisVerzeichnis(String basis, boolean anlegen) {
        String ret;
        if (basis.isEmpty()) {
            ret = System.getProperty("user.home") + File.separator + MserverKonstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
        }
        if (anlegen) {
            File basisF = new File(ret);
            if (!basisF.exists()) {
                if (!basisF.mkdirs()) {
                    MserverLog.fehlerMeldung(1023974998, MserverDaten.class.getName(), new String[] { "Kann den Ordner zum Speichern der Daten nicht anlegen!", ret });
                }
            }
        }
        return ret;
    }

    public static String getKonfigDatei() {
        return MserverDaten.getBasisVerzeichnis() + MserverKonstanten.XML_DATEI;
    }

    public static String getUploadDatei() {
        return MserverDaten.getBasisVerzeichnis() + MserverKonstanten.XML_DATEI_UPLOAD;
    }

    public static boolean konfigExistiert() {
        String datei = MserverDaten.getBasisVerzeichnis() + MserverKonstanten.XML_DATEI;
        return new File(datei).exists();
    }

    public static String getVerzeichnisFilme() {
        String ret = Functions.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MserverKonstanten.VERZEICHNISS_FILMLISTEN);
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdirs()) {
                MserverLog.fehlerMeldung(739851049, MserverDaten.class.getName(), new String[] { "Kann den Ordner zum Speichern der Filmliste nicht anlegen!", ret });
            }
        }
        return ret;

    }

    public static String getLogDatei(String name) {
        String logPfad = "", logFileName;

        try {
            logPfad = Functions.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MserverKonstanten.LOG_FILE_PFAD);

            // prüfen obs geht
            logFileName = Functions.addsPfad(logPfad, name + "__" + MserverDatumZeit.getHeute_yyyy_MM_dd() + ".log");
            File logfile = new File(logFileName);
            if (!logfile.exists()) {
                new File(logPfad).mkdirs();
                if (!logfile.createNewFile()) {
                    logFileName = "";
                }
            }
        } catch (Exception ex) {
            System.out.println("Logfile anlegen: " + name + '\n' + ex.getMessage()); // hier muss direkt geschrieben werden
            logFileName = "";
        }

        if (logFileName.isEmpty()) {
            MserverLog.fehlerMeldung(912304578, MserverDaten.class.getName(), new String[] { "Kann das Logfile nicht anlegen!", logPfad, logFileName });
        }
        return logFileName;
    }
}
