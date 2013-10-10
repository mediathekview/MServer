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
package mServer.daten;

import mServer.tool.MServerKonstanten;
import mServer.upload.MServerUpload;
import msearch.tool.MSearchConst;
import msearch.tool.GuiFunktionen;

public class MServerDatenUpload {

    public String[] arr = new String[MServerKonstanten.UPLOAD_MAX_ELEM];

    public MServerDatenUpload() {
        init();
    }

    private void init() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public String getServer() {
        return arr[MServerKonstanten.UPLOAD_SERVER_NR];
    }

    public String get_Url_Datei_ListeFilmlisten() {
        if (arr[MServerKonstanten.UPLOAD_ART_NR].equals(MServerUpload.UPLOAD_ART_COPY)) {
            return getFilmlisteDestPfadName(MSearchConst.DATEINAME_LISTE_FILMLISTEN);
        } else {
            return GuiFunktionen.addUrl(arr[MServerKonstanten.UPLOAD_URL_FILMLISTE_NR], MSearchConst.DATEINAME_LISTE_FILMLISTEN);
        }
    }

    public String getUrlFilmliste(String dateinameFilmliste) {
        return GuiFunktionen.addUrl(arr[MServerKonstanten.UPLOAD_URL_FILMLISTE_NR], dateinameFilmliste);
    }

    public String getDestDir() {
        return arr[MServerKonstanten.UPLOAD_DEST_DIR_NR];
    }

    public String getFilmlisteDestPfadName(String dateinameFilmliste) {
        return GuiFunktionen.addsPfad(arr[MServerKonstanten.UPLOAD_DEST_DIR_NR], dateinameFilmliste);
    }

    public String getListeFilmlistenDestPfadName() {
        return GuiFunktionen.addsPfad(arr[MServerKonstanten.UPLOAD_DEST_DIR_NR], MSearchConst.DATEINAME_LISTE_FILMLISTEN);
    }

    public String getPrio() {
        return ((arr[MServerKonstanten.UPLOAD_PRIO_FILMLISTE_NR].equals("")) ? "1" : arr[MServerKonstanten.UPLOAD_PRIO_FILMLISTE_NR]);
    }
}
