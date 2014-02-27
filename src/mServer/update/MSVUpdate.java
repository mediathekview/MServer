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

import mServer.tool.MSVDaten;
import mServer.tool.MSVFunktionen;
import mServer.tool.MSVLog;

public class MSVUpdate {

    public static boolean updaten() {
        boolean ret = false;
        // nach Update suchen
        String updateUrl = MSVUpdateSuchen.checkVersion();
        if (updateUrl.equals("")) {
            MSVLog.systemMeldung("Programm noch aktuell");
        } else {
            String jarPfad = MSVFunktionen.getPathJar();
            if (MSVUpdateSuchen.updateLaden(updateUrl, jarPfad, MSVDaten.getUserAgent()) != null) {
                MSVLog.systemMeldung("Programmupdate OK");
                ret = true;
            }
        }
        return ret;
    }
}
