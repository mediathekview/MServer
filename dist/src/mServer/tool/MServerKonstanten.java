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
    public static final String PROGRAMMNAME = "MServer";
    public static final String USER_AGENT_DEFAULT = PROGRAMMNAME + " " + VERSION;
    public static final int PROGRAMM_EXIT_CODE_UPDATE = 11;
    public static final String PROGRAMMDATEI_UPDATE = "MServer_update.zip";
    // Dateien/Verzeichnisse
    public static final String VERZEICHNISS_EINSTELLUNGEN = ".mserver";
    public static final String VERZEICHNISS_FILMLISTEN = "filmlisten";
    public static final String XML_DATEI = "mserver.xml";
    public static final String LOG_FILE_NAME = "msLog";
    public static final String LOG_FILE_NAME_MV = "mvLog";
    public static final String LOG_FILE_PFAD = "log";
    public static final String PROGRAMM_UPDATE_URL_RSS = "http://sourceforge.net/api/file/index/project-id/222825/mtime/desc/limit/100/rss";
    //public static final String PROGRAMM_UPDATE_URL_RSS = "http://192.168.25.134/update/rss";
    //public static final String PROGRAMM_UPDATE_URL_RSS = "http://176.28.14.91/mediathek1/rss";
    public static final String XML_START = "MServer";
    public static final String STR_TRUE = "1";
    public static final String STR_FALSE = "0";
    public static final String KODIERUNG_UTF = "UTF-8";
    // Filmliste die äleter sind werden aus der ListeFilmlisten gelöscht
    public static final int FILMLISTEN_MAX_ALTER = 3;
    // Wartezeiten
    public static final int MAX_WARTEN_FTP_UPLOAD = 1000 * 60 * 5; // 5 Minuten
    public static final int WARTEZEIT_ALLES_LADEN = 1000 * 60 * 120; // 110 Minuten
    public static final int WARTEZEIT_UPDATE_LADEN = 1000 * 60 * 60; // 50 Minuten
    //public  static final int WARTEZEIT_UPDATE_LADEN = 1000 * 10;
    //
    //
    // Konstanten System
    public static final String SYSTEM_MELDEN_PWD_XML = "system-melden-pwd-xml";
    public static final int SYSTEM_MELDEN_PWD_XML_NR = 0;
    public static final String SYSTEM_MELDEN_URL_XML = "system-melden-url-xml";
    public static final int SYSTEM_MELDEN_URL_XML_NR = 1;
    public static final String SYSTEM_MELDEN_PWD_JSON = "system-melden-pwd-json";
    public static final int SYSTEM_MELDEN_PWD_JSON_NR = 2;
    public static final String SYSTEM_MELDEN_URL_JSON = "system-melden-url-json";
    public static final int SYSTEM_MELDEN_URL_JSON_NR = 3;
    public static final String SYSTEM_USER_AGENT = "system-user-agent";
    public static final int SYSTEM_USER_AGENT_NR = 4;
    public static final String SYSTEM_UPDATE_SUCHEN = "system-programmupdate-suchen";
    public static final int SYSTEM_UPDATE_SUCHEN_NR = 5;
    public static final String SYSTEM_IMPORT_EXTEND_URL = "system-filmliste-import-url-extend";
    public static final int SYSTEM_IMPORT_URL_EXTEND_NR = 6;
    public static final String SYSTEM_IMPORT_URL_REPLACE = "system-filmliste-import-url-replace";
    public static final int SYSTEM_IMPORT_URL_REPLACE_NR = 7;
    public static final String SYSTEM_EXPORT_FILE_FILMLISTE = "system-export-datei-filmliste"; // die Filmliste (json.xz) kann lokal noch mit einem festen Namen kopiert werden
    public static final int SYSTEM_EXPORT_FILE_FILMLISTE_NR = 8;
    public static final String SYSTEM_EXPORT_FILE_FILMLISTE_ORG = "system-export-datei-filmliste-org"; // die Filmliste für diff (json.xz) kann lokal noch mit einem festen Namen kopiert werden
    public static final int SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR = 9;
    public static final String SYSTEM_PFAD_LOGDATEI = "system-pfad-logdatei";
    public static final int SYSTEM_PFAD_LOGDATEI_NR = 10;
    public static final String SYSTEM_PROXY_URL = "system-proxy-url";
    public static final int SYSTEM_PROXY_URL_NR = 11;
    public static final String SYSTEM_PROXY_PORT = "system-proxy-port";
    public static final int SYSTEM_PROXY_PORT_NR = 12;
    public static final String SYSTEM_DEBUG = "system-debug";
    public static final int SYSTEM_DEBUG_NR = 13;
    // Array
    public static final String SYSTEM = "system";
    public static final int SYSTEM_MAX_ELEM = 14;
    public static final String[] SYSTEM_COLUMN_NAMES = {SYSTEM_MELDEN_PWD_XML, SYSTEM_MELDEN_URL_XML, SYSTEM_MELDEN_PWD_JSON, SYSTEM_MELDEN_URL_JSON,
        SYSTEM_USER_AGENT, SYSTEM_UPDATE_SUCHEN, SYSTEM_IMPORT_EXTEND_URL, SYSTEM_IMPORT_URL_REPLACE,
        SYSTEM_EXPORT_FILE_FILMLISTE, SYSTEM_EXPORT_FILE_FILMLISTE_ORG,
        SYSTEM_PFAD_LOGDATEI, SYSTEM_PROXY_URL, SYSTEM_PROXY_PORT, SYSTEM_DEBUG};
    //
    //
}
