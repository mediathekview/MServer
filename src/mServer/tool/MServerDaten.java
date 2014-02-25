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
import mServer.daten.MServerListeSuchen;
import mServer.daten.MServerListeUpload;
import msearch.tool.MSearchFunktionen;
import msearch.tool.MSearchGuiFunktionen;
import msearch.tool.MSearchConst;

public class MServerDaten {

    public static String[] system = new String[MServerKonstanten.SYSTEM_MAX_ELEM];
    public static MServerListeSuchen listeSuchen = new MServerListeSuchen();
    public static MServerListeUpload listeUpload = new MServerListeUpload();
    public static boolean debug = false;
    //
    private static String basisverzeichnis = "";

    public static void init() {
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
    }

    public static String getUserAgent() {
        if (system[MServerKonstanten.SYSTEM_USER_AGENT_NR].trim().equals("")) {
            return MServerKonstanten.PROGRAMMNAME + " " + MSearchConst.VERSION_FILMLISTE + " / " + MSearchFunktionen.getBuildNr();
        } else {
        }
        return system[MServerKonstanten.SYSTEM_USER_AGENT_NR].trim();
    }

    public static void setBasisVerzeichnis(String b) {
        if (b.equals("")) {
            basisverzeichnis = getBasisVerzeichnis(b, true);
        } else {
            basisverzeichnis = b;
        }
    }

    public static int getProxyPort() {
        if (system[MServerKonstanten.SYSTEM_PROXY_PORT_NR].isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(system[MServerKonstanten.SYSTEM_PROXY_PORT_NR]);
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(963487219, MServerDaten.class.getName(), new String[]{"Proxyport falsch: ", system[MServerKonstanten.SYSTEM_PROXY_PORT_NR]});
        }
        return -1;
    }

    public static String getBasisVerzeichnis() {
        return getBasisVerzeichnis(basisverzeichnis, false);
    }

    private static String getBasisVerzeichnis(String basis, boolean anlegen) {
        String ret;
        if (basis.equals("")) {
            ret = System.getProperty("user.home") + File.separator + MServerKonstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
        }
        if (anlegen) {
            File basisF = new File(ret);
            if (!basisF.exists()) {
                if (!basisF.mkdir()) {
                    MServerLog.fehlerMeldung(1023974998, MServerDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Daten nicht anlegen!", ret});
                }
            }
        }
        return ret;
    }

    public static String getKonfigDatei() {
        return MServerDaten.getBasisVerzeichnis() + MServerKonstanten.XML_DATEI;
    }

    public static boolean konfigExistiert() {
        String datei = MServerDaten.getBasisVerzeichnis() + MServerKonstanten.XML_DATEI;
        if (new File(datei).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getVerzeichnisFilme() {
        String ret = MSearchGuiFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MServerKonstanten.VERZEICHNISS_FILMLISTEN);
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdir()) {
                MServerLog.fehlerMeldung(739851049, MServerDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Filmliste nicht anlegen!", ret});
            }
        }
        return ret;


    }

    public static String getLogVerzeichnis() {
        String ret;
        if (system[MServerKonstanten.SYSTEM_PFAD_LOGDATEI_NR] == null) {
            ret = MSearchGuiFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MServerKonstanten.LOG_FILE_PFAD);
        } else if (MServerDaten.system[MServerKonstanten.SYSTEM_PFAD_LOGDATEI_NR].trim().equals("")) {
            ret = MSearchGuiFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MServerKonstanten.LOG_FILE_PFAD);
        } else {
            ret = MServerDaten.system[MServerKonstanten.SYSTEM_PFAD_LOGDATEI_NR].trim();
        }
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdir()) {
                MServerLog.fehlerMeldung(739851049, MServerDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Logfiles nicht anlegen!", ret});
            }
        }
        return ret;
    }

    public static String getLogDatei() {
        // beim Programmstart wird das Logfile ermittelt
        // und geleert!
        String logPfad, logFileName;
        try {
            logPfad = getLogVerzeichnis();
            // prüfen obs geht
            logFileName = MSearchGuiFunktionen.addsPfad(logPfad, MServerKonstanten.LOG_FILE_NAME + MServerDatumZeit.getJetztLogDatei());
            File logfile = new File(logFileName);
            if (!logfile.exists()) {
                boolean b = new File(logPfad).mkdirs();
                if (!logfile.createNewFile()) {
                    logFileName = "";
                }
            }
            return logFileName;
        } catch (Exception ex) {
            System.out.println("Logfile anlegen: " + ex.getMessage()); // hier muss direkt geschrieben werden
            return "";
        }
    }

    public static File getLogDatei_mediathekView() {
        File logfile;
        String logPfad, logFileName;
        try {
            logPfad = MServerDaten.getLogVerzeichnis();
            // prüfen obs geht
            logFileName = MSearchGuiFunktionen.addsPfad(logPfad, MServerKonstanten.LOG_FILE_NAME_MV + MServerDatumZeit.getJetztLogDatei());
            logfile = new File(logFileName);
            if (!logfile.exists()) {
                boolean b = new File(logPfad).mkdirs();
                if (!logfile.createNewFile()) {
                    logfile = null;
                }
            }
            return logfile;
        } catch (Exception ex) {
            System.out.println("Logfile MV anlegen: " + ex.getMessage()); // hier muss direkt geschrieben werden
            return null;
        }
    }
}
