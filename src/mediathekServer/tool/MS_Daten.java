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

public class MS_Daten {

    public static String[] system = new String[MS_Konstanten.SYSTEM_MAX_ELEM];
    public static String[] update = new String[MS_Konstanten.UPDATE_MAX_ELEM];
    public static String[] suchen = new String[MS_Konstanten.SUCHEN_MAX_ELEM];
    public static String[] upload = new String[MS_Konstanten.UPLOAD_MAX_ELEM];
    public static boolean debug = false;
    public static MS_LogFile logFile;
    //
    private static String basisverzeichnis = "";

    public MS_Daten() {
        init();
        logFile = new MS_LogFile();
    }

    private void init() {
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
        for (int i = 0; i < update.length; ++i) {
            update[i] = "";
        }
        for (int i = 0; i < suchen.length; ++i) {
            suchen[i] = "";
        }
        for (int i = 0; i < upload.length; ++i) {
            upload[i] = "";
        }
    }

    public static void setUserAgent(String ua) {
        if (ua.equals("")) {
            system[MS_Konstanten.SYSTEM_USER_AGENT_NR] = MS_Konstanten.PROGRAMMNAME + "-" + MS_Funktionen.getVersion();
        } else {
            system[MS_Konstanten.SYSTEM_USER_AGENT_NR] = ua;
        }
    }

    public static String getUserAgent() {
        return system[MS_Konstanten.SYSTEM_USER_AGENT_NR];
    }

    public static void setBasisVerzeichnis(String b) {
        if (b.equals("")) {
            basisverzeichnis = getBasisVerzeichnis_(b);
        } else {
            basisverzeichnis = b;
        }
    }

    public static String getBasisVerzeichnis() {
        return basisverzeichnis;
    }

    private static String getBasisVerzeichnis_(String basis) {
        String ret;
        if (basis.equals("")) {
            ret = System.getProperty("user.home") + File.separator + MS_Konstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
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

    public static String getLogDatei() {
        return MS_Daten.getBasisVerzeichnis() + MS_Konstanten.XML_LOG_FILE;
    }

    public static boolean logExistiert() {
        String datei = MS_Daten.getBasisVerzeichnis() + MS_Konstanten.XML_LOG_FILE;
        if (new File(datei).exists()) {
            return true;
        } else {
            return false;
        }
    }
}
