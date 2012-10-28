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
import java.util.LinkedList;

public class MS_ListeSuchen extends LinkedList<MS_DatenSuchen> {

    ////////////// nach Uhrzeit in die Liste einsorieren
    public int getNow() {
        // liefert das erste "i" das in der Zukunft liegt
        int ret = -1;
        Date now = new Date();
        for (int i = 0; i < this.size(); ++i) {
            if (this.get(i).getDate().compareTo(now) > 0) {
                return i;
            }
        }
        return ret;
    }
}
