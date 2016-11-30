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

public class MserverKonstanten {

    public static final String VERSION = "2";
    public static final String PROGRAMMNAME = "MServer";
    // Dateien/Verzeichnisse
    public static final String NAME_FILMLISTE_AKT = "Filmliste-akt.xz";
    public static final String NAME_FILMLISTE_DIFF = "Filmliste-diff.xz";

    public static final String VERZEICHNISS_EINSTELLUNGEN = ".mserver";
    public static final String VERZEICHNISS_FILMLISTEN = "filmlisten";
    public static final String XML_DATEI = "mserver.xml";
    public static final String XML_DATEI_UPLOAD = "upload.xml";
    public static final String LOG_FILE_NAME = "MvServer";
    public static final String LOG_FILE_NAME_MSEARCH = "MSearch";
    public static final String LOG_FILE_PFAD = "log";
    public static final String XML_START = "MServer";
    public static final String STR_TRUE = "1";
    public static final String STR_FALSE = "0";
    public static final String KODIERUNG_UTF = "UTF-8";
    // Wartezeiten
    public static final int MAX_WARTEN_FTP_UPLOAD = 5; // max Dauer für FTP-Upload: 5 Minuten
    public static final int WAIT_TIME_LOAD_UPDATE = 60; // max Dauer fürs update-Suchen in Minuten
    public static final int WAIT_TIME_LOAD_BIG = 160; // max Dauer fürs alles Suchen in Minuten
    public static final int WAIT_TIME_LOAD_MAX = 240; // max Dauer fürs komplette Suchen in Minuten

    //
    //
    // Konstanten System
    public static final String SYSTEM = "system";

    public static final String SYSTEM_USER_AGENT = "system-user-agent";
    public static final int SYSTEM_USER_AGENT_NR = 0;

    public static final String SYSTEM_IMPORT_URL_1 = "system-filmliste-import-url-1";
    public static final int SYSTEM_IMPORT_URL_1_NR = 1;
    public static final String SYSTEM_IMPORT_URL_2 = "system-filmliste-import-url-2";
    public static final int SYSTEM_IMPORT_URL_2_NR = 2;

    public static final String SYSTEM_IMPORT_OLD = "system-filmliste-import-old";
    public static final int SYSTEM_IMPORT_OLD_NR = 3;

    public static final String SYSTEM_IMPORT_AKT = "system-filmliste-import-akt";
    public static final int SYSTEM_IMPORT_AKT_NR = 4;
    public static final String SYSTEM_EXPORT_AKT = "system-filmliste-export-akt";
    public static final int SYSTEM_EXPORT_AKT_NR = 5;

    public static final String SYSTEM_IMPORT_LIVE = "system-filmliste-import-live";
    public static final int SYSTEM_IMPORT_LIVE_NR = 6;

    public static final String SYSTEM_EXPORT_FILMLISTE_AKT = "system-export-datei-filmliste"; // die Filmliste (json.xz) kann lokal noch mit einem festen Namen kopiert werden
    public static final int SYSTEM_EXPORT_FILMLISTE_AKT_NR = 7;
    public static final String SYSTEM_EXPORT_FILMLISTE_ORG = "system-export-datei-filmliste-org"; // die Filmliste für ORG (json.xz) kann lokal noch mit einem festen Namen kopiert werden
    public static final int SYSTEM_EXPORT_FILMLISTE_ORG_NR = 8;
    public static final String SYSTEM_EXPORT_FILMLISTE_DIFF = "system-export-datei-filmliste-diff"; // die Filmliste diff (json.xz) kann lokal noch mit einem festen Namen kopiert werden
    public static final int SYSTEM_EXPORT_FILMLISTE_DIFF_NR = 9;

    public static final String SYSTEM_FILMLISTE_ORG = "system-filmliste-org"; // die Filmliste aus der diff erstellt wird, wenn leer wird die eigene org verwendet
    public static final int SYSTEM_FILMLISTE_ORG_NR = 10;
    public static final String SYSTEM_PROXY_URL = "system-proxy-url";
    public static final int SYSTEM_PROXY_URL_NR = 11;
    public static final String SYSTEM_PROXY_PORT = "system-proxy-port";
    public static final int SYSTEM_PROXY_PORT_NR = 12;
    public static final String SYSTEM_DEBUG = "system-debug";
    public static final int SYSTEM_DEBUG_NR = 13;
    public static final String SYSTEM_RESTART_AFTER_RUN = "system-restart-after-run";
    public static final int SYSTEM_RESTART_AFTER_RUN_NR = 14;

    public static final String[] SYSTEM_COLUMN_NAMES = {
        SYSTEM_USER_AGENT, SYSTEM_IMPORT_URL_1, SYSTEM_IMPORT_URL_2, SYSTEM_IMPORT_OLD,
        SYSTEM_IMPORT_AKT, SYSTEM_EXPORT_AKT, SYSTEM_IMPORT_LIVE,
        SYSTEM_EXPORT_FILMLISTE_AKT, SYSTEM_EXPORT_FILMLISTE_ORG, SYSTEM_EXPORT_FILMLISTE_DIFF, SYSTEM_FILMLISTE_ORG,
        SYSTEM_PROXY_URL, SYSTEM_PROXY_PORT, SYSTEM_DEBUG, SYSTEM_RESTART_AFTER_RUN
    };

    public static final int SYSTEM_MAX_ELEM = SYSTEM_COLUMN_NAMES.length;
}
