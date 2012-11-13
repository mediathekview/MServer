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
package mediathekServer.search;

import mediathek.MediathekNoGui;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_FilmeSuchen {

    public boolean filmeSuchen(boolean allesLaden, String output, String userAgent) {
        boolean ret = false;
        try {
            String importUrl = MS_Daten.system[MS_Konstanten.SYSTEM_IMPORT_URL_NR].toString();
            new MediathekNoGui(MS_Daten.getBasisVerzeichnis(), allesLaden, output, importUrl, userAgent).serverStarten("BR");
            ret = true;
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(636987308, MS_FilmeSuchen.class.getName(), "filmeSuchen", ex);
        }
        return ret;
    }
}
