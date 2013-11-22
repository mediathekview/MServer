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
package mServer.upload;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import mServer.tool.MServerLog;
import msearch.tool.GuiFunktionen;

public class MServerExport {

    public static boolean copy(String filmDateiPfad, String filmDateiName, String zielPfadDatei) {
        boolean ret = false;
        MServerLog.systemMeldung("");
        MServerLog.systemMeldung("----------------------");
        MServerLog.systemMeldung("Export start");
        MServerLog.systemMeldung("Pfad: " + filmDateiPfad);
        MServerLog.systemMeldung("Datei: " + filmDateiName);
        MServerLog.systemMeldung("Zieldatei: " + zielPfadDatei);
        try {
            String src = GuiFunktionen.addsPfad(filmDateiPfad, filmDateiName);
            String dest = zielPfadDatei;
            Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
            ret = true;
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(915237563, MServerExport.class.getName(), "export", ex);
        }
        return ret;
    }
}
