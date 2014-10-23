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

import java.util.Iterator;
import mServer.daten.MSVDatenUpload;
import mServer.daten.MSVSearchTask;
import static mServer.daten.MSVSearchTask.SUCHEN_WANN_NR;
import mServer.tool.MSVDaten;
import mServer.tool.MSVKonstanten;
import mServer.tool.MSVLog;
import mServer.tool.MSVWarten;
import msearch.daten.MSConfig;

public class MSVUpload {

    // Konstanten Upload
    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_XML = "xml";
    public static final String LISTE_XML = "xml";
    public static final String LISTE_JSON = "json";
    public static final String LISTE_DIFF = "diff";
    public static final String LISTE_AKT = "akt";

    public static void upload(MSVSearchTask aktSearchTask) {
        // ==================================================
        // erst lokale Exports erledigen
        MSVLog.systemMeldung("");
        MSVLog.systemMeldung("");
        MSVLog.systemMeldung("");
        MSVLog.systemMeldung("==========================");
        MSVLog.systemMeldung("Start Upload");
        MSVLog.systemMeldung("");
        try {
            if (aktSearchTask.orgListeAnlegen() && !MSVDaten.system[MSVKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR].isEmpty()) {
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("Copy Export Filmliste-Org");
                MSVCopy.copy(MSConfig.getPathFilmlist_json_org_xz(), MSVDaten.system[MSVKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR], true /*rename*/);
            }
            if (!MSVDaten.system[MSVKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_NR].isEmpty()) {
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("Copy Export Filmliste");
                MSVCopy.copy(MSConfig.getPathFilmlist_json_akt_xz(), MSVDaten.system[MSVKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_NR], true /*rename*/);
            }
            if (!MSVDaten.system[MSVKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_DIFF_NR].isEmpty()) {
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("Copy Export Filmliste-Diff");
                MSVCopy.copy(MSConfig.getPathFilmlist_json_diff_xz(), MSVDaten.system[MSVKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_DIFF_NR], true /*rename*/);
            }

            // ======================================================
            // jetzt die anderen Uploads erledigen
            String destFileName;
            String srcPathFile;
            Iterator<MSVDatenUpload> it = MSVDaten.listeUpload.iterator();
            if (MSVDaten.listeUpload.size() > 0) {
                // nach dem Suchen Rechner Zeit zum Abau aller Verbindungen geben
                new MSVWarten().sekundenWarten(30);
            }
            while (it.hasNext()) {
                MSVDatenUpload datenUpload = it.next();
                srcPathFile = datenUpload.getFilmlisteSrc();
                destFileName = getExportNameFilmliste(datenUpload, aktSearchTask);
                MSVLog.systemMeldung("");
                MSVLog.systemMeldung("--------------------------");
                switch (datenUpload.arr[MSVDatenUpload.UPLOAD_LISTE_NR]) {
                    case (MSVUpload.LISTE_DIFF):
                        MSVLog.systemMeldung("Upload Diff-Liste");
                        break;
                    case (MSVUpload.LISTE_AKT):
                        MSVLog.systemMeldung("Upload Akt-Liste");
                        break;
                    case (MSVUpload.LISTE_XML):
                        MSVLog.systemMeldung("Upload XML-Liste");
                        break;
                    case (MSVUpload.LISTE_JSON):
                        MSVLog.systemMeldung("Upload JSON-Liste");
                        break;
                    default:
                        MSVLog.systemMeldung("Upload Filmliste");
                }
                switch (datenUpload.arr[MSVDatenUpload.UPLOAD_ART_NR]) {
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
                MSVLog.systemMeldung("--------------------------");
                MSVLog.systemMeldung("");
            }
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(989620146, MSVUpload.class.getName(), "MS_UploadFtp", ex);
        }
        MSVLog.systemMeldung("==========================");
        MSVLog.systemMeldung("Upload fertig");
    }

    private static void uploadFtp_(String srcPathFile, String destFileName, MSVDatenUpload datenUpload) {
        if (MSVFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
            MSVMelden.melden(destFileName, datenUpload);
        } else {
            new MSVWarten().sekundenWarten(60);
            MSVLog.systemMeldung("2. Versuch Upload FTP");
            if (MSVFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
                MSVMelden.melden(destFileName, datenUpload);
            } else {
                MSVLog.fehlerMeldung(649896079, MSVUpload.class.getName(), "FTP, 2.Versuch nicht geklappe");
            }
        }
    }

    private static void uploadCopy_(String srcPathFile, String destFileName, MSVDatenUpload datenUpload) {
        if (MSVCopy.copy(srcPathFile, destFileName, datenUpload)) {
            MSVMelden.melden(destFileName, datenUpload);
        } else {
            // wenns nicht geklappt hat nochmal versuchen
            new MSVWarten().sekundenWarten(60);
            MSVLog.systemMeldung("2. Versuch Upload copy");
            if (MSVCopy.copy(srcPathFile, destFileName, datenUpload)) {
                MSVMelden.melden(destFileName, datenUpload);
            } else {
                MSVLog.fehlerMeldung(798956236, MSVUpload.class.getName(), "Copy, 2.Versuch nicht geklappt");
            }
        }
    }

    private static String getExportNameFilmliste(MSVDatenUpload mSVDatenUpload, MSVSearchTask mSVSearchTask) {
        if (!mSVDatenUpload.arr[MSVDatenUpload.UPLOAD_DEST_NAME_NR].isEmpty()) {
            return mSVDatenUpload.arr[MSVDatenUpload.UPLOAD_DEST_NAME_NR];
        }
        String name;
        switch (mSVDatenUpload.arr[MSVDatenUpload.UPLOAD_LISTE_NR]) {
            case (MSVUpload.LISTE_XML):
                // gibts noch kein diff..
                final String FILM_DATEI_SUFF_XML = "bz2";
                final String FILMDATEI_NAME_XML = "Filmliste-xml";
                if (mSVSearchTask.sofortSuchen()) {
                    name = FILMDATEI_NAME_XML + "." + FILM_DATEI_SUFF_XML;
                } else {
                    name = FILMDATEI_NAME_XML + "_" + mSVSearchTask.arr[SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF_XML;
                }
                break;
            case (MSVUpload.LISTE_DIFF):
                name = MSVKonstanten.NAME_FILMLISTE_DIFF;
                break;
            case (MSVUpload.LISTE_AKT):
                name = MSVKonstanten.NAME_FILMLISTE_AKT;
                break;
            case (MSVUpload.LISTE_JSON):
            default:
                final String FILM_DATEI_SUFF_JSON = "xz";
                final String FILMDATEI_NAME_JSON = "Filmliste-json";
                if (mSVSearchTask.sofortSuchen()) {
                    name = FILMDATEI_NAME_JSON + "." + FILM_DATEI_SUFF_JSON;
                } else {
                    name = FILMDATEI_NAME_JSON + "_" + mSVSearchTask.arr[SUCHEN_WANN_NR].replace(":", "_") + "." + FILM_DATEI_SUFF_JSON;
                }
        }
        return name;
    }

}
