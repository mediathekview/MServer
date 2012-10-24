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

public class MS_LogMeldung {

    // Konstanten Logart
    public static final String MS_LOG__UPDATE_NICHT_SUCHEN = "ms_log__update_nicht_suchen";
    public static final String MS_LOG__UPDATE_SUCHEN = "ms_log__update_suchen";
    public static final String MS_LOG__UPDATE_AKTUELL = "ms_log__update_aktuell";
    public static final String MS_LOG__UPDATE_AKTUALISIERT = "ms_log__update_aktualisiert";
    //
    // Konstanten Logmeldung
    public static final String MS_LOG_WANN = "ms_log_wann";
    public static final int MS_LOG_WANN_NR = 0;
    public static final String MS_LOG_WAS = "ms_log_was";
    public static final int MS_LOG_WAS_NR = 1;
    // Array
    public static final String MS_LOG = "ms_log";
    public static final int MS_LOG_MAX_ELEM = 2;
    public static final String[] MS_LOG_COLUMN_NAMES = {MS_LOG_WANN, MS_LOG_WAS};
    public String[] arr;

    public MS_LogMeldung() {
        arr = new String[MS_LOG_MAX_ELEM];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public MS_LogMeldung(String was) {
        arr = new String[MS_LOG_MAX_ELEM];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
        arr[MS_LogMeldung.MS_LOG_WANN_NR] = MS_DatumZeit.getJetzt();
        arr[MS_LogMeldung.MS_LOG_WAS_NR] = was;
    }
}
