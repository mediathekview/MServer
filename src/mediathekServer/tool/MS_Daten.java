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
package mediathekServer.tool;

import java.io.File;
import mediathek.tool.Funktionen;
import mediathek.tool.GuiFunktionen;
import mediathek.tool.Konstanten;
import mediathekServer.daten.MS_ListeSuchen;
import mediathekServer.daten.MS_ListeUpload;

public class MS_Daten {

    public static String[] system = new String[MS_Konstanten.SYSTEM_MAX_ELEM];
    public static MS_ListeSuchen listeSuchen = new MS_ListeSuchen();
    public static MS_ListeUpload listeUpload = new MS_ListeUpload();
    public static boolean debug = false;
    //
    private static String basisverzeichnis = "";

    public MS_Daten() {
        init();
    }

    private void init() {
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
    }

    public static String getUserAgent() {
        if (system[MS_Konstanten.SYSTEM_USER_AGENT_NR].trim().equals("")) {
            return MS_Konstanten.PROGRAMMNAME + " " + Konstanten.VERSION + " / " + Funktionen.getBuildNr();
        } else {
        }
        return system[MS_Konstanten.SYSTEM_USER_AGENT_NR].trim();
    }

    public static void setBasisVerzeichnis(String b) {
        if (b.equals("")) {
            basisverzeichnis = getBasisVerzeichnis(b, true);
        } else {
            basisverzeichnis = b;
        }
    }

    public static String getBasisVerzeichnis() {
        return getBasisVerzeichnis(basisverzeichnis, false);
    }

    private static String getBasisVerzeichnis(String basis, boolean anlegen) {
        String ret;
        if (basis.equals("")) {
            ret = System.getProperty("user.home") + File.separator + MS_Konstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
        }
        if (anlegen) {
            File basisF = new File(ret);
            if (!basisF.exists()) {
                if (!basisF.mkdir()) {
                    MS_Log.fehlerMeldung(1023974998, MS_Daten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Daten nicht anlegen!", ret});
                }
            }
        }
        return ret;
    }

    public static String getKonfigDatei() {
        return MS_Daten.getBasisVerzeichnis() + MS_Konstanten.XML_DATEI;
    }

    public static boolean konfigExistiert() {
        String datei = MS_Daten.getBasisVerzeichnis() + MS_Konstanten.XML_DATEI;
        if (new File(datei).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getVerzeichnisFilme() {
        String ret = GuiFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), "filmelisten");
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdir()) {
                MS_Log.fehlerMeldung(739851049, MS_Daten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Filmliste nicht anlegen!", ret});
            }
        }
        return ret;


    }

    public static String getLogVerzeichnis() {
        String ret;
        if (system[MS_Konstanten.SYSTEM_PFAD_LOGDATEI_NR] == null) {
            ret = GuiFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MS_Konstanten.LOG_FILE_PFAD);
        } else if (MS_Daten.system[MS_Konstanten.SYSTEM_PFAD_LOGDATEI_NR].trim().equals("")) {
            ret = GuiFunktionen.addsPfad(getBasisVerzeichnis(basisverzeichnis, false), MS_Konstanten.LOG_FILE_PFAD);
        } else {
            ret = MS_Daten.system[MS_Konstanten.SYSTEM_PFAD_LOGDATEI_NR].trim();
        }
        File basisF = new File(ret);
        if (!basisF.exists()) {
            if (!basisF.mkdir()) {
                MS_Log.fehlerMeldung(739851049, MS_Daten.class.getName(), new String[]{"Kann den Ordner zum Speichern der Logfiles nicht anlegen!", ret});
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
            logFileName = GuiFunktionen.addsPfad(logPfad, MS_Konstanten.LOG_FILE_NAME + MS_DatumZeit.getJetztLogDatei());
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
            logPfad = MS_Daten.getLogVerzeichnis();
            // prüfen obs geht
            logFileName = GuiFunktionen.addsPfad(logPfad, MS_Konstanten.LOG_FILE_NAME_MV + MS_DatumZeit.getJetztLogDatei());
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
