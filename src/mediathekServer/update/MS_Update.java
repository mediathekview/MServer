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
package mediathekServer.update;

import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Funktionen;
import mediathekUpdate.MediathekUpdate;

public class MS_Update {

    private static String url = "http://176.28.14.91/mediathek1/ms.zip";

    public MS_Update() {
    }

    public static boolean updaten() {
        boolean ret = true;
        String updateUrl = MS_UpdateSuchen.checkVersion();
        if (!updateUrl.equals("")) {
            String jarPfad = MS_Funktionen.getPathJar();
            ret = MediathekUpdate.starten(url, jarPfad, MS_Daten.getUserAgent());
        }
        return ret;
    }
}
