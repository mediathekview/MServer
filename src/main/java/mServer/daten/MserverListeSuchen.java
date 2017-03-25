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
package mServer.daten;

import java.util.Date;
import java.util.LinkedList;

public class MserverListeSuchen extends LinkedList<MserverSearchTask> {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean add(MserverSearchTask d) {
        if (d.sofortSuchen()) {
            addFirst(d);
            return true;
        }
        // nach Datum sortiert, einfügen
        for (int i = 0; i < this.size(); ++i) {
            MserverSearchTask ds = this.get(i);
            if (ds.spaeter(d)) {
                add(i, d);
                return true;
            }
        }
        return super.add(d);
    }

    public MserverSearchTask erste() {
        // liefert den ersten Job der in der Zukunft liegt
        Date now = new Date();
        MserverSearchTask akt = null;
        while ((akt = this.poll()) != null) {
            if (akt.sofortSuchen()) {
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
