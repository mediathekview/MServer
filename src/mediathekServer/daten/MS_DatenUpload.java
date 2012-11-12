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
package mediathekServer.daten;

import mediathek.tool.GuiFunktionen;
import mediathek.tool.Konstanten;
import mediathekServer.tool.MS_Konstanten;

public class MS_DatenUpload {

    public String[] arr = new String[MS_Konstanten.UPLOAD_MAX_ELEM];

    public MS_DatenUpload() {
        init();
    }

    private void init() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public String getServer() {
        return arr[MS_Konstanten.UPLOAD_SERVER_NR];
    }

    public String getUrlListeFilmlisten() {
        return GuiFunktionen.addUrl(arr[MS_Konstanten.UPLOAD_URL_FILMLISTE_NR], Konstanten.DATEINAME_LISTE_FILMLISTEN);
    }

    public String getUrlFilmliste(String dateinameFilmliste) {
        return GuiFunktionen.addUrl(arr[MS_Konstanten.UPLOAD_URL_FILMLISTE_NR], dateinameFilmliste);
    }

    public String getDestDir() {
        return arr[MS_Konstanten.UPLOAD_DEST_DIR_NR];
    }

    public String getFilmlisteDestPfadName(String dateinameFilmliste) {
        return GuiFunktionen.addsPfad(arr[MS_Konstanten.UPLOAD_DEST_DIR_NR], dateinameFilmliste);
    }

    public String getListeFilmlistenDestPfadName() {
        return GuiFunktionen.addsPfad(arr[MS_Konstanten.UPLOAD_DEST_DIR_NR], Konstanten.DATEINAME_LISTE_FILMLISTEN);
    }

    public String getPrio() {
        return arr[MS_Konstanten.UPLOAD_PRIO_FILMLISTE_NR];
    }
}
