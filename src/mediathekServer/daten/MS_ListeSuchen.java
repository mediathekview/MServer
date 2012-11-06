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
package mediathekServer.daten;

import java.util.Date;
import java.util.LinkedList;
import mediathekServer.tool.MS_DatumZeit;
import mediathekServer.tool.MS_Konstanten;

public class MS_ListeSuchen extends LinkedList<MS_DatenSuchen> {

    @Override
    public boolean add(MS_DatenSuchen d) {
        if (d.jetzt()) {
            super.addFirst(d);
            return true;
        }
        // nach Datum sortiert, einfügen
        for (int i = 0; i < this.size(); ++i) {
            MS_DatenSuchen ds = this.get(i);
            if (ds.spaeter(d)) {
                super.add(i, d);
                return true;
            }
        }
        return super.add(d);
    }

    public MS_DatenSuchen erste() {
        // liefert den ersten Job der in der Zukunft liegt
        Date now = new Date();
        MS_DatenSuchen akt = null;
        while ((akt = this.poll()) != null) {
            if (akt.jetzt()) {
////                akt.arr[MS_Konstanten.SUCHEN_WANN_NR] = MS_DatumZeit.getJetzt_hh_mm();
                return akt;
            }
            Date d = akt.getTimeHeute();
            if (d.compareTo(now) >= 0) {
                return akt;
            }
        }
        return akt;
    }
}
