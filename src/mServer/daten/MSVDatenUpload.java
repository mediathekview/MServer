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

import mServer.tool.MSVDaten;
import mServer.tool.MSVKonstanten;
import mServer.upload.MSVUpload;
import msearch.daten.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSGuiFunktionen;

public class MSVDatenUpload {

    // Array
    public static final String UPLOAD = "upload";
    public static final String UPLOAD_ART = "upload-art"; // copy/ftp
    public static final int UPLOAD_ART_NR = 0;
    public static final String UPLOAD_FORMAT = "upload-format"; // json/xml(altes Format)
    public static final int UPLOAD_FORMAT_NR = 1;
    public static final String UPLOAD_LISTE = "upload-liste"; // normal/diff/org/akt --> komplette Liste/diff-Liste/org-Liste/akt-Liste
    public static final int UPLOAD_LISTE_NR = 2;

    public static final String UPLOAD_SERVER = "upload-server";
    public static final int UPLOAD_SERVER_NR = 3;
    public static final String UPLOAD_USER = "upload-user";
    public static final int UPLOAD_USER_NR = 4;
    public static final String UPLOAD_PWD = "upload-pwd";
    public static final int UPLOAD_PWD_NR = 5;
    public static final String UPLOAD_DEST_DIR = "upload-dest-dir";
    public static final int UPLOAD_DEST_DIR_NR = 6;
    public static final String UPLOAD_DEST_NAME = "upload-dest-name";
    public static final int UPLOAD_DEST_NAME_NR = 7;
    public static final String UPLOAD_PORT = "upload-port";
    public static final int UPLOAD_PORT_NR = 8;

    public static final String UPLOAD_URL_FILMLISTE = "upload-url-filmliste"; // ist die dann entstehende Download-URL
    public static final int UPLOAD_URL_FILMLISTE_NR = 9;
    public static final String UPLOAD_PRIO_FILMLISTE = "upload-prio-filmliste";
    public static final int UPLOAD_PRIO_FILMLISTE_NR = 10;
    public static final String UPLOAD_VORHER_LOESCHEN = "upload-vorher-loeschen"; // wird vor dem neuen Suchen aus der Downloadliste gelöscht
    public static final int UPLOAD_VORHER_LOESCHEN_NR = 11;
//    public static final String UPLOAD_RENAME = "upload-rename"; // vorhandene Datei wird vor dem Überschreiben umbenannt
//    public static final int UPLOAD_RENAME_NR = 12;

    public static final int MAX_ELEM = 12;
    public static final String[] UPLOAD_COLUMN_NAMES = {UPLOAD_ART, UPLOAD_FORMAT, UPLOAD_LISTE,
        UPLOAD_SERVER, UPLOAD_USER, UPLOAD_PWD, UPLOAD_DEST_DIR, UPLOAD_DEST_NAME, UPLOAD_PORT,
        UPLOAD_URL_FILMLISTE, UPLOAD_PRIO_FILMLISTE, UPLOAD_VORHER_LOESCHEN};
    public String[] arr = new String[MAX_ELEM];

    public MSVDatenUpload() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public MSVDatenUpload getCopy() {
        MSVDatenUpload mSVDatenUpload = new MSVDatenUpload();
        System.arraycopy(this.arr, 0, mSVDatenUpload.arr, 0, this.arr.length);
        return mSVDatenUpload;
    }

    public String getFilmlisteSrc() {
        String f;
        if (arr[MSVDatenUpload.UPLOAD_FORMAT_NR].equals(MSVUpload.FORMAT_XML)) {
            // altes Format, gibts noch kein Diff
            f = MSConfig.getPathFilmlist_xml_bz2();
        } else {
            switch (arr[MSVDatenUpload.UPLOAD_LISTE_NR]) {
                case (MSVUpload.LISTE_DIFF):
                    f = MSConfig.getPathFilmlist_json_diff_xz();
                    break;
                case (MSVUpload.LISTE_AKT): // da unterscheidet sich dann nur der Zieldateiname
                default:
                    f = MSConfig.getPathFilmlist_json_akt_xz();
            }
        }
        return f;
    }

    public boolean vorherLoeschen() {
        return arr[UPLOAD_VORHER_LOESCHEN_NR].equals(MSVKonstanten.STR_TRUE);
    }

    public boolean rename() {
        boolean ret;
        switch (arr[MSVDatenUpload.UPLOAD_LISTE_NR]) {
            case (MSVUpload.LISTE_DIFF):
                ret = true;
                break;
            case (MSVUpload.LISTE_AKT):
                ret = true;
                break;
            default:
                ret = false;
        }
        return ret;
        //return arr[UPLOAD_RENAME_NR].equals(MSVKonstanten.STR_TRUE);
    }

    public String get_Url_Datei_ListeFilmlisten() {
        if (arr[UPLOAD_ART_NR].equals(MSVUpload.UPLOAD_ART_COPY)) {
            return getFilmlisteDestPfadName(MSConst.DATEINAME_LISTE_FILMLISTEN);
        } else {
            return MSGuiFunktionen.addUrl(arr[UPLOAD_URL_FILMLISTE_NR], MSConst.DATEINAME_LISTE_FILMLISTEN);
        }
    }

    public String getUrlFilmliste(String dateinameFilmliste) {
        if (arr[UPLOAD_URL_FILMLISTE_NR].isEmpty()) {
            return "";
        } else {
            return MSGuiFunktionen.addUrl(arr[UPLOAD_URL_FILMLISTE_NR], dateinameFilmliste);
        }
    }

    public String getDestDir() {
        return arr[UPLOAD_DEST_DIR_NR];
    }

    public String getFilmlisteDestPfadName(String dateinameFilmliste) {
        return MSGuiFunktionen.addsPfad(arr[UPLOAD_DEST_DIR_NR], dateinameFilmliste);
    }

    public String getListeFilmlistenDestPfadName() {
        return MSGuiFunktionen.addsPfad(arr[UPLOAD_DEST_DIR_NR], MSConst.DATEINAME_LISTE_FILMLISTEN);
    }

    public String getPrio() {
        return ((arr[UPLOAD_PRIO_FILMLISTE_NR].equals("")) ? "1" : arr[UPLOAD_PRIO_FILMLISTE_NR]).trim();
    }

    public String getUrlFilmlistenServer() {
        if (arr[MSVDatenUpload.UPLOAD_FORMAT_NR].equals(MSVUpload.FORMAT_JSON)) {
            return MSConst.ADRESSE_FILMLISTEN_SERVER_JSON;
        } else {
            return MSConst.ADRESSE_FILMLISTEN_SERVER_XML;
        }
    }

    public String getMeldenUrl() {
        if (arr[MSVDatenUpload.UPLOAD_FORMAT_NR].equals(MSVUpload.FORMAT_JSON)) {
            return MSVDaten.system[MSVKonstanten.SYSTEM_MELDEN_URL_JSON_NR].trim();
        } else {
            return MSVDaten.system[MSVKonstanten.SYSTEM_MELDEN_URL_XML_NR].trim();
        }
    }

    public String getMeldenPwd() {
        if (arr[MSVDatenUpload.UPLOAD_FORMAT_NR].equals(MSVUpload.FORMAT_JSON)) {
            return MSVDaten.system[MSVKonstanten.SYSTEM_MELDEN_PWD_JSON_NR].trim();
        } else {
            return MSVDaten.system[MSVKonstanten.SYSTEM_MELDEN_PWD_XML_NR].trim();
        }
    }
}
