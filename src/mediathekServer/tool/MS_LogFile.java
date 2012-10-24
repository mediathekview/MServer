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

import java.util.LinkedList;

public class MS_LogFile {

    public LinkedList<MS_LogMeldung> listeLogMeldungen;

    public MS_LogFile() {
        listeLogMeldungen = new Liste();
    }

    private class Liste extends LinkedList<MS_LogMeldung> {

        @Override
        public boolean add(MS_LogMeldung m) {
            MS_Log.systemMeldung(m.arr[MS_LogMeldung.MS_LOG_WANN_NR] + ": " + m.arr[MS_LogMeldung.MS_LOG_WAS_NR]);
            return super.add(m);
        }
    }
}
