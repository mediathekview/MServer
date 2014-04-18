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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import mServer.daten.MSVDatenUpload;
import mServer.tool.MSVFunktionen;
import mServer.tool.MSVLog;
import msearch.filmeLaden.DatenUrlFilmliste;

public class MSVUploadCopy {

    public static boolean copy(String srcPathFile, String destFileName, MSVDatenUpload datenUpload) {
        boolean ret = false;
        File f = null;
        try {
            MSVLog.systemMeldung("");
            MSVLog.systemMeldung("UploadCopy");
            new File(datenUpload.getDestDir()).mkdirs();
            String dest = datenUpload.getFilmlisteDestPfadName(destFileName);
            MSVCopy.copy(srcPathFile, dest);

            MSVLog.systemMeldung("");
            MSVLog.systemMeldung("und noch melden");
            // Liste der Filmlisten auktualisieren
            // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
            DatenUrlFilmliste dfus = new DatenUrlFilmliste(datenUpload.getUrlFilmliste(destFileName), "1", MSVFunktionen.getTime(), MSVFunktionen.getDate());
            f = MSVListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
            if (f != null) {
                String src = f.getPath();
                String destListen = datenUpload.getListeFilmlistenDestPfadName();
                Files.copy(Paths.get(src), Paths.get(destListen), StandardCopyOption.REPLACE_EXISTING);
                ret = true;
            }
            ret = true;
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(747452360, MSVUploadCopy.class.getName(), "copy", ex);
        }
        if (f != null) {
            try {
                f.delete();
            } catch (Exception ignore) {
            }
        }
        return ret;
    }
}
