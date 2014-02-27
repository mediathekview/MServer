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
import mServer.tool.MSVLog;
import msearch.tool.MSearchGuiFunktionen;

public class MSVCopy {

    public static boolean copy(String srcPath, String srcFile, String destPathFile) {
        try {
            return copy(MSearchGuiFunktionen.addsPfad(srcPath, srcFile), destPathFile);
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(915237563, MSVCopy.class.getName(), "MServerCopy.copy", ex);
        }
        return false;
    }

    public static boolean copy(String srcPathFile, String destPathFile) {
        boolean ret = false;
        MSVLog.systemMeldung("");
        MSVLog.systemMeldung("----------------------");
        MSVLog.systemMeldung("Copy start");
        MSVLog.systemMeldung("src: " + srcPathFile);
        MSVLog.systemMeldung("dest: " + destPathFile);
        try {
            String dest = destPathFile;
            Files.copy(Paths.get(srcPathFile), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
            ret = true;
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(832164870, MSVCopy.class.getName(), "MServerCopy.copy", ex);
        }
        return ret;
    }
}
