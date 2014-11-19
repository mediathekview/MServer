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
package mvServer.daten;

import msearch.daten.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSFunktionen;
import mvServer.tool.MvSDaten;
import mvServer.tool.MvSKonstanten;
import mvServer.upload.MvSUpload;

public class MvSDatenUpload {

    // Array
    public static final String UPLOAD = "upload";
    public static final String UPLOAD_ART = "upload-art"; // copy/ftp
    public static final int UPLOAD_ART_NR = 0;
    public static final String UPLOAD_LISTE = "upload-liste"; // xml/json/diff/org/akt --> komplette Liste/diff-Liste/org-Liste/akt-Liste
    public static final int UPLOAD_LISTE_NR = 1;

    public static final String UPLOAD_SERVER = "upload-server";
    public static final int UPLOAD_SERVER_NR = 2;
    public static final String UPLOAD_USER = "upload-user";
    public static final int UPLOAD_USER_NR = 3;
    public static final String UPLOAD_PWD = "upload-pwd";
    public static final int UPLOAD_PWD_NR = 4;
    public static final String UPLOAD_DEST_DIR = "upload-dest-dir";
    public static final int UPLOAD_DEST_DIR_NR = 5;
    public static final String UPLOAD_DEST_NAME = "upload-dest-name";
    public static final int UPLOAD_DEST_NAME_NR = 6;
    public static final String UPLOAD_PORT = "upload-port";
    public static final int UPLOAD_PORT_NR = 7;

    public static final String UPLOAD_URL_FILMLISTE = "upload-url-filmliste"; // ist die dann entstehende Download-URL
    public static final int UPLOAD_URL_FILMLISTE_NR = 8;
    public static final String UPLOAD_PRIO_FILMLISTE = "upload-prio-filmliste";
    public static final int UPLOAD_PRIO_FILMLISTE_NR = 9;

    public static final int MAX_ELEM = 10;
    public static final String[] UPLOAD_COLUMN_NAMES = {UPLOAD_ART, UPLOAD_LISTE,
        UPLOAD_SERVER, UPLOAD_USER, UPLOAD_PWD, UPLOAD_DEST_DIR, UPLOAD_DEST_NAME, UPLOAD_PORT,
        UPLOAD_URL_FILMLISTE, UPLOAD_PRIO_FILMLISTE};
    public String[] arr = new String[MAX_ELEM];

    public MvSDatenUpload() {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }

    public MvSDatenUpload getCopy() {
        MvSDatenUpload mvsDatenUpload = new MvSDatenUpload();
        System.arraycopy(this.arr, 0, mvsDatenUpload.arr, 0, this.arr.length);
        return mvsDatenUpload;
    }

    public String getFilmlisteSrc() {
        String f;
        switch (arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
            case (MvSUpload.LISTE_XML):
                // altes Format, gibts noch kein Diff
                f = MSConfig.getPathFilmlist_xml_bz2();
                break;
            case (MvSUpload.LISTE_DIFF):
                f = MSConfig.getPathFilmlist_json_diff_xz();
                break;
            case (MvSUpload.LISTE_AKT):
            case (MvSUpload.LISTE_JSON):
            default:
                // da unterscheidet sich dann nur der Zieldateiname
                f = MSConfig.getPathFilmlist_json_akt_xz();
        }
        return f;
    }

    public boolean rename() {
        switch (arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
            case (MvSUpload.LISTE_DIFF):
            case (MvSUpload.LISTE_AKT):
                return true;
            case (MvSUpload.LISTE_XML):
            case (MvSUpload.LISTE_JSON):
            default:
                return false;
        }
    }

    public String getUrlFilmliste(String dateinameFilmliste) {
        if (arr[UPLOAD_URL_FILMLISTE_NR].isEmpty()) {
            return "";
        } else {
            return MSFunktionen.addUrl(arr[UPLOAD_URL_FILMLISTE_NR], dateinameFilmliste);
        }
    }

    public String getDestDir() {
        return arr[UPLOAD_DEST_DIR_NR];
    }

    public String getFilmlisteDestPfadName(String dateinameFilmliste) {
        return MSFunktionen.addsPfad(arr[UPLOAD_DEST_DIR_NR], dateinameFilmliste);
    }

    public String getPrio() {
        return ((arr[UPLOAD_PRIO_FILMLISTE_NR].equals("")) ? "1" : arr[UPLOAD_PRIO_FILMLISTE_NR]).trim();
    }

    public String getUrlFilmlistenServer() {
        // ist die URL für die Liste mit den Download-URLs der Filmlisten (....xml)
        switch (arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
            case (MvSUpload.LISTE_XML):
                return MSConst.ADRESSE_FILMLISTEN_SERVER_XML;
            case (MvSUpload.LISTE_JSON):
            case (MvSUpload.LISTE_DIFF):
            case (MvSUpload.LISTE_AKT):
            default:
                return MSConst.ADRESSE_FILMLISTEN_SERVER_JSON;
        }
    }

    public String getMeldenUrl() {
        // ist die URL zum Melden einer neuen Download-URL einer Filmliste (....php)
        switch (arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
            case (MvSUpload.LISTE_XML):
                return MvSDaten.system[MvSKonstanten.SYSTEM_MELDEN_URL_XML_NR].trim();
            case (MvSUpload.LISTE_DIFF):
            case (MvSUpload.LISTE_AKT):
            case (MvSUpload.LISTE_JSON):
            default:
                return MvSDaten.system[MvSKonstanten.SYSTEM_MELDEN_URL_JSON_NR].trim();
        }
    }

    public String getMeldenPwd() {
        switch (arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
            case (MvSUpload.LISTE_XML):
                return MvSDaten.system[MvSKonstanten.SYSTEM_MELDEN_PWD_XML_NR].trim();
            case (MvSUpload.LISTE_DIFF):
            case (MvSUpload.LISTE_AKT):
            case (MvSUpload.LISTE_JSON):
            default:
                return MvSDaten.system[MvSKonstanten.SYSTEM_MELDEN_PWD_JSON_NR].trim();
        }
    }
}
