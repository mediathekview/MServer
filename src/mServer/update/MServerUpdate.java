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
package mServer.update;

import mServer.tool.MServerDaten;
import mServer.tool.MServerFunktionen;
import mServer.tool.MServerLog;

public class MServerUpdate {

    public static boolean updaten() {
        boolean ret = false;
        // nach Update suchen
        String updateUrl = MServerUpdateSuchen.checkVersion();
        if (updateUrl.equals("")) {
            MServerLog.systemMeldung("Programm noch aktuell");
        } else {
            String jarPfad = MServerFunktionen.getPathJar();
            if (MServerUpdateSuchen.updateLaden(updateUrl, jarPfad, MServerDaten.getUserAgent()) != null) {
                MServerLog.systemMeldung("Programmupdate OK");
                ret = true;
            }
        }
        return ret;
    }
}
