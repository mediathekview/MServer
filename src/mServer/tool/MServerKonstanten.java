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

public class MServerKonstanten {

    public static final String VERSION = "1.0";
    public static final String PROGRAMMNAME = "MediathekServer";
    public static final String USER_AGENT_DEFAULT = PROGRAMMNAME + " " + VERSION;
    public static final int PROGRAMM_EXIT_CODE_UPDATE = 11;
    public static final String PROGRAMMDATEI_UPDATE = "MediathekServer_update.zip";
    // Dateien/Verzeichnisse
    public static final String VERZEICHNISS_EINSTELLUNGEN = ".mediathekServer";
    public static final String XML_DATEI = "mediathekServer.xml";
    public static final String LOG_FILE_NAME = "msLog";
    public static final String LOG_FILE_NAME_MV = "mvLog";
    public static final String LOG_FILE_PFAD = "log";
    public static final String PROGRAMM_UPDATE_URL_RSS = "http://sourceforge.net/api/file/index/project-id/222825/mtime/desc/limit/100/rss";
    //public static final String PROGRAMM_UPDATE_URL_RSS = "http://192.168.25.134/update/rss";
    //public static final String PROGRAMM_UPDATE_URL_RSS = "http://176.28.14.91/mediathek1/rss";
    public static final String XML_START = "MediathekServer";
    public static final String STR_TRUE = "1";
    public static final String STR_FALSE = "0";
    public static final String KODIERUNG_UTF = "UTF-8";
    // Filmliste die äleter sind werden aus der ListeFilmlisten gelöscht
    public static final int FILMLISTEN_MAX_ALTER = 3;
    // Wartezeiten
    public static final int MAX_WARTEN_FTP_UPLOAD = 1000 * 60 * 5; // 5 Minuten
    public static final int WARTEZEIT_ALLES_LADEN = 1000 * 60 * 110; // 110 Minuten
    public static final int WARTEZEIT_UPDATE_LADEN = 1000 * 60 * 50; // 50 Minuten
    //public  static final int WARTEZEIT_UPDATE_LADEN = 1000 * 10;
    //
    //
    // Konstanten System
    // zum Melden
    public static final String SYSTEM_UPDATE_MELDEN_PWD = "system-filmliste-melden-pwd";
    public static final int SYSTEM_UPDATE_MELDEN_PWD_NR = 0;
    public static final String SYSTEM_UPDATE_MELDEN_URL = "system-filmliste-melden-url";
    public static final int SYSTEM_UPDATE_MELDEN_URL_NR = 1;
    public static final String SYSTEM_USER_AGENT = "system-user-agent";
    public static final int SYSTEM_USER_AGENT_NR = 2;
    public static final String SYSTEM_UPDATE_SUCHEN = "system-programmupdate-suchen";
    public static final int SYSTEM_UPDATE_SUCHEN_NR = 3;
    public static final String SYSTEM_IMPORT_EXTEND_URL = "system-filmliste-import-url-extend";
    public static final int SYSTEM_IMPORT_URL_EXTEND_NR = 4;
    public static final String SYSTEM_IMPORT_URL_REPLACE = "system-filmliste-import-url-replace";
    public static final int SYSTEM_IMPORT_URL_REPLACE_NR = 5;
    public static final String SYSTEM_PFAD_LOGDATEI = "system-pfad-logdatei";
    public static final int SYSTEM_PFAD_LOGDATEI_NR = 6;
    public static final String SYSTEM_PROXY_URL = "system-proxy-url";
    public static final int SYSTEM_PROXY_URL_NR = 7;
    public static final String SYSTEM_PROXY_PORT = "system-proxy-port";
    public static final int SYSTEM_PROXY_PORT_NR = 8;
    public static final String SYSTEM_DEBUG = "system-debug";
    public static final int SYSTEM_DEBUG_NR = 9;
    // Array
    public static final String SYSTEM = "system";
    public static final int SYSTEM_MAX_ELEM = 10;
    public static final String[] SYSTEM_COLUMN_NAMES = {SYSTEM_UPDATE_MELDEN_PWD, SYSTEM_UPDATE_MELDEN_URL,
        SYSTEM_USER_AGENT, SYSTEM_UPDATE_SUCHEN, SYSTEM_IMPORT_EXTEND_URL, SYSTEM_IMPORT_URL_REPLACE,
        SYSTEM_PFAD_LOGDATEI, SYSTEM_PROXY_URL, SYSTEM_PROXY_PORT, SYSTEM_DEBUG};
    //
    //
    // Konstanten Suchen
    public static final String SUCHEN_ALLES = "alles";
    public static final String SUCHEN_UPDATE = "update";
    public static final String SUCHEN_NEU = "neu";
    public static final String SUCHEN_WANN_SOFORT = "sofort";
    //
    public static final String SUCHEN_SENDER_WIE = "suchen-sender-wie"; // Sender komplett - nur ein Update
    public static final int SUCHEN_SENDER_WIE_NR = 0;
    public static final String SUCHEN_LISTE_WIE = "suchen-liste-wie"; // neue Liste / Liste nur aktualisieren
    public static final int SUCHEN_LISTE_WIE_NR = 1;
    public static final String SUCHEN_WANN = "suchen-wann";
    public static final int SUCHEN_WANN_NR = 2;
    public static final String SUCHEN_SENDER = "suchen-sender";
    public static final int SUCHEN_SENDER_NR = 3;
    // Array
    public static final String SUCHEN = "suchen";
    public static final int SUCHEN_MAX_ELEM = 4;
    public static final String[] SUCHEN_COLUMN_NAMES = {SUCHEN_SENDER_WIE, SUCHEN_LISTE_WIE, SUCHEN_WANN, SUCHEN_SENDER};
    //
    //
    // Konstanten Upload
    public static final String UPLOAD_ART = "upload-art";
    public static final int UPLOAD_ART_NR = 0;
    public static final String UPLOAD_SERVER = "upload-server";
    public static final int UPLOAD_SERVER_NR = 1;
    public static final String UPLOAD_USER = "upload-user";
    public static final int UPLOAD_USER_NR = 2;
    public static final String UPLOAD_PWD = "upload-pwd";
    public static final int UPLOAD_PWD_NR = 3;
    public static final String UPLOAD_DEST_DIR = "upload-dest-dir";
    public static final int UPLOAD_DEST_DIR_NR = 4;
    public static final String UPLOAD_PORT = "upload-port";
    public static final int UPLOAD_PORT_NR = 5;
    public static final String UPLOAD_URL_FILMLISTE = "upload-url-filmliste";
    public static final int UPLOAD_URL_FILMLISTE_NR = 6;
    public static final String UPLOAD_PRIO_FILMLISTE = "upload-prio-filmliste";
    public static final int UPLOAD_PRIO_FILMLISTE_NR = 7;
    // Array
    public static final String UPLOAD = "upload";
    public static final int UPLOAD_MAX_ELEM = 8;
    public static final String[] UPLOAD_COLUMN_NAMES = {UPLOAD_ART, UPLOAD_SERVER, UPLOAD_USER, UPLOAD_PWD,
        UPLOAD_DEST_DIR, UPLOAD_PORT, UPLOAD_URL_FILMLISTE, UPLOAD_PRIO_FILMLISTE};
}
