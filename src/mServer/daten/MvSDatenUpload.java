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

import mSearch.Config;
import mSearch.tool.Functions;
import mServer.upload.MvSUpload;

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
//    public static final int UPLOAD_DEST_NAME_NR = 6;
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

//    public String[] getFilmlisteSrc() {
//        String[] f = new String[]{MSConfig.getPathFilmlist_json_diff_xz(),
//            MSConfig.getPathFilmlist_json_akt_xz()};
//        return f;
//    }
//    public boolean rename() {
//        switch (arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
//            case (MvSUpload.LISTE_DIFF):
//            case (MvSUpload.LISTE_AKT):
//                return true;
//            default:
//                return false;
//        }
//    }
    public String getDestDir() {
        return arr[UPLOAD_DEST_DIR_NR];
    }

    public String getFilmlisteDestPfadName(String dateinameFilmliste) {
        return Functions.addsPfad(arr[UPLOAD_DEST_DIR_NR], dateinameFilmliste);
    }

}
