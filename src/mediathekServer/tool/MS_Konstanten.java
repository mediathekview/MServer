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

public class MS_Konstanten {

    public static final String VERSION = "1.0";
    public static final String PROGRAMMNAME = "MediathekServer";
    public static final String USER_AGENT_DEFAULT = PROGRAMMNAME + " " + VERSION;
    // Dateien/Verzeichnisse
    public static final String VERZEICHNISS_EINSTELLUNGEN = ".mediathekServer";
    public static final String XML_DATEI = "mediathekServer.xml";
    public static final String ADRESSE_PROGRAMM_VERSION = "http://zdfmediathk.sourceforge.net/version-server.xml";
    // 
    public static final String XML_START = "MediathekServer";
    //
    // Konstanten System
    public static final String SYSTEM_PWD = "system-pwd";
    public static final int SYSTEM_PWD_NR = 0;
    public static final String SYSTEM_USER_AGENT = "system-user-agent";
    public static final int SYSTEM_USER_AGENT_NR = 1;
    public static final String SYSTEM_UPDATE_SUCHEN = "system-update-suchen";
    public static final int SYSTEM_UPDATE_SUCHEN_NR = 2;
    // Array
    public static final String SYSTEM = "system";
    public static final int SYSTEM_MAX_ELEM = 3;
    public static final String[] SYSTEM_COLUMN_NAMES = {SYSTEM_PWD, SYSTEM_USER_AGENT, SYSTEM_UPDATE_SUCHEN};
    //
    // Konstanten Update
    public static final String UPDATE_COUNT = "update-count";
    public static final int UPDATE_COUNT_NR = 0;
    // Array
    public static final String UPDATE = "update";
    public static final int UPDATE_MAX_ELEM = 1;
    public static final String[] UPDATE_COLUMN_NAMES = {UPDATE_COUNT};
    //
    // Konstanten Suchen
    public static final String SUCHEN_ALLES = "suchen-alles";
    public static final int SUCHEN_ALLES_NR = 0;
    // Array
    public static final String SUCHEN = "suchen";
    public static final int SUCHEN_MAX_ELEM = 1;
    public static final String[] SUCHEN_COLUMN_NAMES = {SUCHEN_ALLES};
    //
    // Konstanten Upload
    public static final String UPLOAD_EXEC = "upload-exec";
    public static final int UPLOAD_EXEC_NR = 0;
    // Array
    public static final String UPLOAD = "upload";
    public static final int UPLOAD_MAX_ELEM = 1;
    public static final String[] UPLOAD_COLUMN_NAMES = {UPLOAD_EXEC};
}
