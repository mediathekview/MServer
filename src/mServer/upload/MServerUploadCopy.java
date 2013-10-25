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
import msearch.tool.GuiFunktionen;

public class MServerUploadCopy {

    public static boolean copy(String filmDateiPfad, String filmDateiName, MServerDatenUpload datenUpload) {
        boolean ret = false;
        MServerLog.systemMeldung("");
        MServerLog.systemMeldung("----------------------");
        MServerLog.systemMeldung("Copy start");
        MServerLog.systemMeldung("Pfad: " + filmDateiPfad);
        MServerLog.systemMeldung("Datei: " + filmDateiName);
        try {
            new File(datenUpload.getDestDir()).mkdirs();
            String src = GuiFunktionen.addsPfad(filmDateiPfad, filmDateiName);
            String dest = datenUpload.getFilmlisteDestPfadName(filmDateiName);
            Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
            ret = true;
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(747452360, MServerUploadCopy.class.getName(), "copy", ex);
        }
        if (ret) {
            // Liste der Filmlisten auktualisieren
            // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
            DatenUrlFilmliste dfus = new DatenUrlFilmliste(datenUpload.getUrlFilmliste(filmDateiName), "1", MServerFunktionen.getTime(), MServerFunktionen.getDate());
            File f = MServerListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
            if (f != null) {
                try {
                    String src = f.getPath();
                    String dest = datenUpload.getListeFilmlistenDestPfadName();
                    Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
                    ret = true;
                } catch (Exception ex) {
                    MServerLog.fehlerMeldung(698741230, MServerUploadCopy.class.getName(), "copy", ex);
                }
            }
        }
        return ret;
    }
}
