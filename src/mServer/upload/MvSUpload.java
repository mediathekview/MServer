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

import mSearch.Config;
import mServer.daten.MvSDatenUpload;
import mServer.daten.MvSSearchTask;
import mServer.tool.MvSDaten;
import mServer.tool.MvSKonstanten;
import mServer.tool.MvSLog;
import mServer.tool.MvSWarten;

public class MvSUpload {

    // Konstanten Upload
    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";

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
            // export Org
            if (!MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILMLISTE_ORG_NR].isEmpty() && aktSearchTask.orgListeAnlegen()) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("Copy Export Filmliste-Org");
                MvSCopy.copy(Config.getPathFilmlist_json_org_xz(), MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILMLISTE_ORG_NR]);
            }

            // export aktuell
            if (!MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILMLISTE_AKT_NR].isEmpty()) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("Copy Export Filmliste");
                MvSCopy.copy(Config.getPathFilmlist_json_akt_xz(), MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILMLISTE_AKT_NR]);
            }

            // export diff
            if (!MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILMLISTE_DIFF_NR].isEmpty()) {
                MvSLog.systemMeldung("");
                MvSLog.systemMeldung("Copy Export Filmliste-Diff");
                MvSCopy.copy(Config.getPathFilmlist_json_diff_xz(), MvSDaten.system[MvSKonstanten.SYSTEM_EXPORT_FILMLISTE_DIFF_NR]);
            }

            // ======================================================
            // jetzt die anderen Uploads ins Web erledigen
            if (MvSDaten.listeUpload.size() > 0) {
                // nach dem Suchen Rechner Zeit zum Abau aller Verbindungen geben
                new MvSWarten().sekundenWarten(30);
            }

            for (MvSDatenUpload datenUpload : MvSDaten.listeUpload) {
                for (int i = 0; i <= 1; ++i) {
                    String[] srcPathFile = new String[]{Config.getPathFilmlist_json_diff_xz(), Config.getPathFilmlist_json_akt_xz()};
                    String[] destFileName = new String[]{MvSKonstanten.NAME_FILMLISTE_DIFF, MvSKonstanten.NAME_FILMLISTE_AKT};

                    MvSLog.systemMeldung("");
                    MvSLog.systemMeldung("--------------------------");
                    MvSLog.systemMeldung("Upload Liste: " + srcPathFile[i]);

                    // und jetzt der Upload/Copy
                    switch (datenUpload.arr[MvSDatenUpload.UPLOAD_ART_NR]) {
                        case UPLOAD_ART_COPY:
                            // ==============================================================
                            // kopieren
                            uploadCopy_(srcPathFile[i], destFileName[i], datenUpload);
                            break;

                        case UPLOAD_ART_FTP:
                            // ==============================================================
                            // ftp
                            uploadFtp_(srcPathFile[i], destFileName[i], datenUpload);
                            break;
                    }

                    MvSLog.systemMeldung("--------------------------");
                    MvSLog.systemMeldung("");
                }
            }
        } catch (Exception ex) {
            MvSLog.fehlerMeldung(989620146, MvSUpload.class.getName(), "MS_UploadFtp", ex);
        }
        MvSLog.systemMeldung("==========================");
        MvSLog.systemMeldung("Upload fertig");
    }

    private static void uploadFtp_(String srcPathFile, String destFileName, MvSDatenUpload datenUpload) {
        if (!MvSFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
            new MvSWarten().sekundenWarten(30);
            MvSLog.systemMeldung("2. Versuch Upload FTP");
            if (!MvSFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
                MvSLog.fehlerMeldung(649896079, MvSUpload.class.getName(), "FTP, 2.Versuch nicht geklappe");
            }
        }
    }

    private static void uploadCopy_(String srcPathFile, String destFileName, MvSDatenUpload datenUpload) {
        if (!MvSCopy.copy(srcPathFile, destFileName, datenUpload)) {
            // wenns nicht geklappt hat nochmal versuchen
            new MvSWarten().sekundenWarten(30);
            MvSLog.systemMeldung("2. Versuch Upload copy");
            if (!MvSCopy.copy(srcPathFile, destFileName, datenUpload)) {
                MvSLog.fehlerMeldung(798956236, MvSUpload.class.getName(), "Copy, 2.Versuch nicht geklappt");
            }
        }
    }

}
