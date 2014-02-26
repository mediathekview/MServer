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
import mServer.daten.MServerDatenUpload;
import mServer.tool.MServerFunktionen;
import mServer.tool.MServerLog;
import msearch.filmeLaden.DatenUrlFilmliste;

public class MServerUploadCopy {

    public static boolean copy(String srcPathFile, String destFileName, MServerDatenUpload datenUpload) {
        boolean ret = false;
        try {
            MServerLog.systemMeldung("");
            MServerLog.systemMeldung("UploadCopy");
            new File(datenUpload.getDestDir()).mkdirs();
            String dest = datenUpload.getFilmlisteDestPfadName(destFileName);
            MServerCopy.copy(srcPathFile, dest);

            MServerLog.systemMeldung("");
            MServerLog.systemMeldung("und noch melden");
            // Liste der Filmlisten auktualisieren
            // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
            DatenUrlFilmliste dfus = new DatenUrlFilmliste(datenUpload.getUrlFilmliste(destFileName), "1", MServerFunktionen.getTime(), MServerFunktionen.getDate());
            File f = MServerListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
            if (f != null) {
                String src = f.getPath();
                String destListen = datenUpload.getListeFilmlistenDestPfadName();
                Files.copy(Paths.get(src), Paths.get(destListen), StandardCopyOption.REPLACE_EXISTING);
                ret = true;
            }
            ret = true;
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(747452360, MServerUploadCopy.class.getName(), "copy", ex);
        }
        return ret;
    }
}
