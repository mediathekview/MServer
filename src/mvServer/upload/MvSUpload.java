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
package mvServer.upload;

import java.util.Iterator;
import mvServer.daten.MvSDatenUpload;
import mvServer.daten.MvSSearchTask;
import static mvServer.daten.MvSSearchTask.SUCHEN_WANN_NR;
import mvServer.tool.MvSDaten;
import mvServer.tool.MvSKonstanten;
import mvServer.tool.MvSLog;
import mvServer.tool.MvSWarten;
import msearch.tool.MSConfig;

public class MvSUpload {

    // Konstanten Upload
    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_XML = "xml";
    public static final String LISTE_XML = "xml";
    public static final String LISTE_JSON = "json";
    public static final String LISTE_DIFF = "diff";
    public static final String LISTE_AKT = "akt";

    public static void upload(MvSSearchTask aktSearchTask) {
        // ==================================================
        // erst lokale Exports erledigen
        MvSLog.systemMeldung("");
        MvSLog.systemMeldung("");
        MvSLog.systemMeldung("");
        MvSLog.systemMeldung("==========================");
        MvSLog.systemMeldung("Start Upload");
        MvSLog.systemMeldung("");
        try {
            if (aktSearchTask.orgListeAnlegen() && !MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR].isEmpty()) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("Copy Export Filmliste-Org");
                MvSCopy.copy(MSConfig.getPathFilmlist_json_org_xz(), MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR], true /*rename*/);
            }
            if (!MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_NR].isEmpty()) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("Copy Export Filmliste");
                MvSCopy.copy(MSConfig.getPathFilmlist_json_akt_xz(), MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_NR], true /*rename*/);
            }
            if (!MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_DIFF_NR].isEmpty()) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("Copy Export Filmliste-Diff");
                MvSCopy.copy(MSConfig.getPathFilmlist_json_diff_xz(), MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_DIFF_NR], true /*rename*/);
            }

            // ======================================================
            // jetzt die anderen Uploads erledigen
            String destFileName;
            String srcPathFile;
            Iterator<MvSDatenUpload> it = MvSDaten.listeUpload.iterator();
            if (MvSDaten.listeUpload.size() > 0) {
                // nach dem Suchen Rechner Zeit zum Abau aller Verbindungen geben
                new MvSWarten().sekundenWarten(30);
            }
            while (it.hasNext()) {
                MvSDatenUpload datenUpload = it.next();
                srcPathFile = datenUpload.getFilmlisteSrc();
                destFileName = getExportNameFilmliste(datenUpload, aktSearchTask);
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("--------------------------");
                switch (datenUpload.arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
                    case (MvSUpload.LISTE_DIFF):
                        MvSLog.systemMeldung("Upload Diff-Liste");
                        break;
                    case (MvSUpload.LISTE_AKT):
                        MvSLog.systemMeldung("Upload Akt-Liste");
                        break;
                    case (MvSUpload.LISTE_XML):
                        MvSLog.systemMeldung("Upload XML-Liste");
                        break;
                    case (MvSUpload.LISTE_JSON):
                        MvSLog.systemMeldung("Upload JSON-Liste");
                        break;
                    default:
                        MvSLog.systemMeldung("Upload Filmliste");
                }
                switch (datenUpload.arr[MvSDatenUpload.UPLOAD_ART_NR]) {
                    case UPLOAD_ART_COPY:
                        // ==============================================================
                        // kopieren
                        uploadCopy_(srcPathFile, destFileName, datenUpload);
                        break;

                    case UPLOAD_ART_FTP:
                        // ==============================================================
                        // ftp
                        uploadFtp_(srcPathFile, destFileName, datenUpload);
                        break;
                }
                MvSLog.systemMeldung("--------------------------");
                MvSLog.systemMeldung("");
            }
        } catch (Exception ex) {
            MvSLog.fehlerMeldung(989620146, MvSUpload.class.getName(), "MS_UploadFtp", ex);
        }
        MvSLog.systemMeldung("==========================");
        MvSLog.systemMeldung("Upload fertig");
    }

    private static void uploadFtp_(String srcPathFile, String destFileName, MvSDatenUpload datenUpload) {
        if (MvSFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
////            MvSMelden.melden(destFileName, datenUpload);
        } else {
            new MvSWarten().sekundenWarten(60);
            MvSLog.systemMeldung("2. Versuch Upload FTP");
            if (MvSFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
////                MvSMelden.melden(destFileName, datenUpload);
            } else {
                MvSLog.fehlerMeldung(649896079, MvSUpload.class.getName(), "FTP, 2.Versuch nicht geklappe");
            }
        }
    }

    private static void uploadCopy_(String srcPathFile, String destFileName, MvSDatenUpload datenUpload) {
        if (MvSCopy.copy(srcPathFile, destFileName, datenUpload)) {
////            MvSMelden.melden(destFileName, datenUpload);
        } else {
            // wenns nicht geklappt hat nochmal versuchen
            new MvSWarten().sekundenWarten(60);
            MvSLog.systemMeldung("2. Versuch Upload copy");
            if (MvSCopy.copy(srcPathFile, destFileName, datenUpload)) {
////                MvSMelden.melden(destFileName, datenUpload);
            } else {
                MvSLog.fehlerMeldung(798956236, MvSUpload.class.getName(), "Copy, 2.Versuch nicht geklappt");
            }
        }
    }

    private static String getExportNameFilmliste(MvSDatenUpload mvsDatenUpload, MvSSearchTask mvsSearchTask) {
        if (!mvsDatenUpload.arr[MvSDatenUpload.UPLOAD_DEST_NAME_NR].isEmpty()) {
            return mvsDatenUpload.arr[MvSDatenUpload.UPLOAD_DEST_NAME_NR];
        }
        String name;
        switch (mvsDatenUpload.arr[MvSDatenUpload.UPLOAD_LISTE_NR]) {
            case (MvSUpload.LISTE_XML):
                name = MvSKonstanten.NAME_FILMLISTE_XML;
////                // gibts noch kein diff..
////                final String FILM_DATEI_SUFF_XML = "bz2";
////                final String FILMDATEI_NAME_XML = "Filmliste-xml";
////                if (mvsSearchTask.sofortSuchen()) {
////                    name = FILMDATEI_NAME_XML + "." + FILM_DATEI_SUFF_XML;
////                } else {
////                    name = FILMDATEI_NAME_XML + "_" + mvsSearchTask.arr[SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF_XML;
////                }
                break;
            case (MvSUpload.LISTE_DIFF):
                name = MvSKonstanten.NAME_FILMLISTE_DIFF;
                break;
            case (MvSUpload.LISTE_AKT):
                name = MvSKonstanten.NAME_FILMLISTE_AKT;
                break;
            case (MvSUpload.LISTE_JSON):
            default:
                final String FILM_DATEI_SUFF_JSON = "xz";
                final String FILMDATEI_NAME_JSON = "Filmliste-json";
                if (mvsSearchTask.sofortSuchen()) {
                    name = FILMDATEI_NAME_JSON + "." + FILM_DATEI_SUFF_JSON;
                } else {
                    name = FILMDATEI_NAME_JSON + "_" + mvsSearchTask.arr[SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF_JSON;
                }
        }
        return name;
    }

}
