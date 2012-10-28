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
package mediathekServer.MS_Daten;

import java.util.Date;
import mediathekServer.tool.MS_DatumZeit;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_DatenSuchen {

    public String[] arr = new String[MS_Konstanten.SUCHEN_MAX_ELEM];

    public MS_DatenSuchen() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public Date getTime() {
        Date ret;
        try {
            ret = MS_DatumZeit.convertTime(this.arr[MS_Konstanten.SUCHEN_WANN_NR]);
        } catch (Exception ex) {
            ret = null;
            MS_Log.fehlerMeldung(825439079, MS_DatenSuchen.class.getName(), "getTime", ex);
        }
        return ret;
    }

    public boolean allesLaden() {
        return this.arr[MS_Konstanten.SUCHEN_WAS_NR].equals(MS_Konstanten.SUCHEN_ALLES);
    }

    public boolean starten() {
        // true wenn gestartet werden soll: Auftrag liegt jetzt oder in der Vergangenheit
        Date now = new Date();
        if (this.getTime().compareTo(now) <= 0) {
            return true;
        }
        return false;
    }
}
