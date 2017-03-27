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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import mServer.daten.MserverDatenUpload;
import mServer.tool.MserverLog;

public class MserverCopy {

    public static boolean copy(String srcPathFile, String destFileName, MserverDatenUpload datenUpload) {
        boolean ret = false;

        try {
            MserverLog.systemMeldung("");
            MserverLog.systemMeldung("UploadCopy");
            new File(datenUpload.getDestDir()).mkdirs();
            String dest = datenUpload.getFilmlisteDestPfadName(destFileName);
            copy(srcPathFile, dest);
            ret = true;
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(747452360, MserverCopy.class.getName(), "copy", ex);
        }

        return ret;
    }

    public static void copy(String srcPathFile, String destPathFile) {
        MserverLog.systemMeldung("");
        MserverLog.systemMeldung("----------------------");
        MserverLog.systemMeldung("Copy start");
        MserverLog.systemMeldung("src: " + srcPathFile);
        MserverLog.systemMeldung("dest: " + destPathFile);
        try {

            String dest_tmp = destPathFile + "__";
            String dest_old = destPathFile + "_old";
            MserverLog.systemMeldung("Copy Filmliste (rename): " + destPathFile);
            Files.copy(Paths.get(srcPathFile), Paths.get(dest_tmp), StandardCopyOption.REPLACE_EXISTING);

            if (Files.exists(Paths.get(destPathFile), LinkOption.NOFOLLOW_LINKS)) {
                // wenns die Datei schon gibt, umbenennen, ist der Normalfall
                MserverLog.systemMeldung("Rename alte Filmliste: " + dest_tmp);
                Files.move(Paths.get(destPathFile), Paths.get(dest_old), StandardCopyOption.REPLACE_EXISTING);
            }
            MserverLog.systemMeldung("Rename neue Filmliste: " + destPathFile);
            Files.move(Paths.get(dest_tmp), Paths.get(destPathFile), StandardCopyOption.REPLACE_EXISTING);

            MserverLog.systemMeldung("====================================");
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(832164870, MserverCopy.class.getName(), "MvSCopy.copy", ex);
        }
    }

}
