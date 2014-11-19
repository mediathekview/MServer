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
package mvServer.tool;

import java.io.File;
import mvServer.daten.MvSListeSuchen;
import mvServer.daten.MvSListeUpload;
import msearch.tool.MSConst;
import msearch.tool.MSFunktionen;

public class MvSDaten {

    public static String[] system = new String[MvSKonstanten.SYSTEM_MAX_ELEM];
    public static MvSListeSuchen listeSuchen = new MvSListeSuchen();
    public static MvSListeUpload listeUpload = new MvSListeUpload();
    public static boolean debug = false;
    //
    private static String basisverzeichnis = "";

    public static void init() {
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
    }

    public static String getUserAgent() {
        if (system[MvSKonstanten.SYSTEM_USER_AGENT_NR].trim().equals("")) {
            return MvSKonstanten.PROGRAMMNAME + " " + MSConst.VERSION_FILMLISTE + " / " + MSFunktionen.getBuildNr();
        } else {
        }
        return system[MvSKonstanten.SYSTEM_USER_AGENT_NR].trim();
    }

    public static void setBasisVerzeichnis(String b) {
        if (b.equals("")) {
            basisverzeichnis = getBasisVerzeichnis(b, true);
        } else {
            basisverzeichnis = b;
        }
    }

    public static int getProxyPort() {
        if (system[MvSKonstanten.SYSTEM_PROXY_PORT_NR].isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(system[MvSKonstanten.SYSTEM_PROXY_PORT_NR]);
        } catch (Exception ex) {
            MvSLog.fehlerMeldung(963487219, MvSDaten.class.getName(), new String[]{"Proxyport falsch: ", system[MvSKonstanten.SYSTEM_PROXY_PORT_NR]});
        }
        return -1;
    }

    public static String getBasisVerzeichnis() {
        return getBasisVerzeichnis(basisverzeichnis, false);
    }

    private static String getBasisVerzeichnis(String basis, boolean anlegen) {
        String ret;
        if (basis.equals("")) {
            ret = System.getProperty("user.home") + File.separator + MvSKonstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
        }
        if (anlegen) {
            File basisF = new File(ret);
            if (!basisF.exists()) {
                if (!basisF.mkdirs()) {
                    MvSLog.fehlerMeldung(1023974998, MvSDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Daten nicht anlegen!", ret});
                }
            }
        }
        return ret;
    }

    public static String getKonfigDatei() {
        return MvSDaten.getBasisVerzeichnis() + MvSKonstanten.XML_DATEI;
    }

    public static String getUploadDatei() {
        return MvSDaten.getBasisVerzeichnis() + MvSKonstanten.XML_DATEI_UPLOAD;
    }

    public static boolean konfigExistiert() {
        String datei = MvSDaten.getBasisVerzeichnis() + MvSKonstanten.XML_DATEI;
        return new File(datei).exists();
    }

    public static String getVerzeichnisFilme() {
        String ret = MSFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MvSKonstanten.VERZEICHNISS_FILMLISTEN);
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdirs()) {
                MvSLog.fehlerMeldung(739851049, MvSDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Filmliste nicht anlegen!", ret});
            }
        }
        return ret;

    }

    public static String getLogVerzeichnis() {
        String ret;
        if (system[MvSKonstanten.SYSTEM_PFAD_LOGDATEI_NR] == null) {
            ret = MSFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MvSKonstanten.LOG_FILE_PFAD);
        } else if (MvSDaten.system[MvSKonstanten.SYSTEM_PFAD_LOGDATEI_NR].trim().equals("")) {
            ret = MSFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MvSKonstanten.LOG_FILE_PFAD);
        } else {
            ret = MvSDaten.system[MvSKonstanten.SYSTEM_PFAD_LOGDATEI_NR].trim();
        }
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdirs()) {
                MvSLog.fehlerMeldung(958474120, MvSDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Logfiles nicht anlegen!", ret});
            }
        }
        return ret;
    }

    public static String getLogDatei() {
        // beim Programmstart wird das Logfile ermittelt
        String logPfad, logFileName;
        try {
            logPfad = getLogVerzeichnis();
            // prüfen obs geht
            logFileName = MSFunktionen.addsPfad(logPfad, MvSKonstanten.LOG_FILE_NAME + "__" + MvSDatumZeit.getHeute_yyyy_MM_dd() + ".log");
            File logfile = new File(logFileName);
            if (!logfile.exists()) {
                new File(logPfad).mkdirs();
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

    public static String getLogDatei_msearch() {
        String logPfad, logFileName;
        try {
            logPfad = MvSDaten.getLogVerzeichnis();
            // prüfen obs geht
            logFileName = MSFunktionen.addsPfad(logPfad, MvSKonstanten.LOG_FILE_NAME_MSEARCH + "__" + MvSDatumZeit.getHeute_yyyy_MM_dd() + ".log");
            File logfile = new File(logFileName);
            if (!logfile.exists()) {
                new File(logPfad).mkdirs();
                if (!logfile.createNewFile()) {
                    logFileName = "";
                }
            }
            return logFileName;
        } catch (Exception ex) {
            System.out.println("Logfile MV anlegen: " + ex.getMessage()); // hier muss direkt geschrieben werden
            return "";
        }
    }
}
