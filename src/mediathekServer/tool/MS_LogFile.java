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
        listeLogMeldungen = new LinkedList<MS_LogMeldung>();
    }

    public void meldung_upate_suchen() {
        MS_LogMeldung meldung = new MS_LogMeldung(MS_LogMeldung.MS_LOG__UPDATE_SUCHEN);
        listeLogMeldungen.add(meldung);
    }

    public void meldung_upate_aktuell() {
        MS_LogMeldung meldung = new MS_LogMeldung(MS_LogMeldung.MS_LOG__UPDATE_AKTUELL);
        listeLogMeldungen.add(meldung);
    }

    public void meldung_upate_aktualisiert() {
        MS_LogMeldung meldung = new MS_LogMeldung(MS_LogMeldung.MS_LOG__UPDATE_AKTUALISIERT);
        listeLogMeldungen.add(meldung);
    }
}
