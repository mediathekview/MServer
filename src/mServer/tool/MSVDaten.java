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
import mServer.daten.MSVListeSuchen;
import mServer.daten.MSVListeUpload;
import msearch.tool.MSConst;
import msearch.tool.MSFunktionen;

public class MSVDaten {

    public static String[] system = new String[MSVKonstanten.SYSTEM_MAX_ELEM];
    public static MSVListeSuchen listeSuchen = new MSVListeSuchen();
    public static MSVListeUpload listeUpload = new MSVListeUpload();
    public static boolean debug = false;
    //
    private static String basisverzeichnis = "";

    public static void init() {
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
    }

    public static String getUserAgent() {
        if (system[MSVKonstanten.SYSTEM_USER_AGENT_NR].trim().equals("")) {
            return MSVKonstanten.PROGRAMMNAME + " " + MSConst.VERSION_FILMLISTE + " / " + MSFunktionen.getBuildNr();
        } else {
        }
        return system[MSVKonstanten.SYSTEM_USER_AGENT_NR].trim();
    }

    public static void setBasisVerzeichnis(String b) {
        if (b.equals("")) {
            basisverzeichnis = getBasisVerzeichnis(b, true);
        } else {
            basisverzeichnis = b;
        }
    }

    public static int getProxyPort() {
        if (system[MSVKonstanten.SYSTEM_PROXY_PORT_NR].isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(system[MSVKonstanten.SYSTEM_PROXY_PORT_NR]);
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(963487219, MSVDaten.class.getName(), new String[]{"Proxyport falsch: ", system[MSVKonstanten.SYSTEM_PROXY_PORT_NR]});
        }
        return -1;
    }

    public static String getBasisVerzeichnis() {
        return getBasisVerzeichnis(basisverzeichnis, false);
    }

    private static String getBasisVerzeichnis(String basis, boolean anlegen) {
        String ret;
        if (basis.equals("")) {
            ret = System.getProperty("user.home") + File.separator + MSVKonstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
        }
        if (anlegen) {
            File basisF = new File(ret);
            if (!basisF.exists()) {
                if (!basisF.mkdir()) {
                    MSVLog.fehlerMeldung(1023974998, MSVDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Daten nicht anlegen!", ret});
                }
            }
        }
        return ret;
    }

    public static String getKonfigDatei() {
        return MSVDaten.getBasisVerzeichnis() + MSVKonstanten.XML_DATEI;
    }

    public static String getUploadDatei() {
        return MSVDaten.getBasisVerzeichnis() + MSVKonstanten.XML_DATEI_UPLOAD;
    }

    public static boolean konfigExistiert() {
        String datei = MSVDaten.getBasisVerzeichnis() + MSVKonstanten.XML_DATEI;
        return new File(datei).exists();
    }

    public static String getVerzeichnisFilme() {
        String ret = MSFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MSVKonstanten.VERZEICHNISS_FILMLISTEN);
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdirs()) {
                MSVLog.fehlerMeldung(739851049, MSVDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Filmliste nicht anlegen!", ret});
            }
        }
        return ret;

    }

    public static String getLogVerzeichnis() {
        String ret;
        if (system[MSVKonstanten.SYSTEM_PFAD_LOGDATEI_NR] == null) {
            ret = MSFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MSVKonstanten.LOG_FILE_PFAD);
        } else if (MSVDaten.system[MSVKonstanten.SYSTEM_PFAD_LOGDATEI_NR].trim().equals("")) {
            ret = MSFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MSVKonstanten.LOG_FILE_PFAD);
        } else {
            ret = MSVDaten.system[MSVKonstanten.SYSTEM_PFAD_LOGDATEI_NR].trim();
        }
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdirs()) {
                MSVLog.fehlerMeldung(958474120, MSVDaten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Logfiles nicht anlegen!", ret});
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
            logFileName = MSFunktionen.addsPfad(logPfad, MSVKonstanten.LOG_FILE_NAME + MSVDatumZeit.getJetztLogDatei());
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
            logPfad = MSVDaten.getLogVerzeichnis();
            // prüfen obs geht
            logFileName = MSFunktionen.addsPfad(logPfad, MSVKonstanten.LOG_FILE_NAME_MV + MSVDatumZeit.getJetztLogDatei());
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
