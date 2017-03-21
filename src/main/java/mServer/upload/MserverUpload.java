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

import mServer.crawler.CrawlerTool;
import mServer.daten.MserverDatenUpload;
import mServer.daten.MserverSearchTask;
import mServer.tool.MserverDaten;
import mServer.tool.MserverDatumZeit;
import mServer.tool.MserverKonstanten;
import mServer.tool.MserverLog;
import mServer.tool.MserverWarten;

public class MserverUpload {

    // Konstanten Upload
    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";

    public static void upload(MserverSearchTask aktSearchTask) {
        // ==================================================
        // erst lokale Exports erledigen
        MserverLog.systemMeldung("");
        MserverLog.systemMeldung("");
        MserverLog.systemMeldung("");
        MserverLog.systemMeldung("==========================");
        MserverLog.systemMeldung("Start Upload");
        MserverLog.systemMeldung("");
        try {
            // export Akt
            // eine Filmliste mit Datum "HEUTE" exportieren
            if (!MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_AKT_NR].isEmpty()) {
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("Copy Export Filmliste");
                MserverCopy.copy(CrawlerTool.getPathFilmlist_json_akt_xz(), MserverDatumZeit.getNameAkt(MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_AKT_NR]));
            }

            // export Org
            if (!MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_FILMLISTE_ORG_NR].isEmpty() && aktSearchTask.orgListeAnlegen()) {
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("Copy Export Filmliste-Org");
                MserverCopy.copy(CrawlerTool.getPathFilmlist_json_org_xz(), MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_FILMLISTE_ORG_NR]);
            }

            // export aktuell
            if (!MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_FILMLISTE_AKT_NR].isEmpty()) {
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("Copy Export Filmliste");
                MserverCopy.copy(CrawlerTool.getPathFilmlist_json_akt_xz(), MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_FILMLISTE_AKT_NR]);
            }

            // export diff
            if (!MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_FILMLISTE_DIFF_NR].isEmpty()) {
                MserverLog.systemMeldung("");
                MserverLog.systemMeldung("Copy Export Filmliste-Diff");
                MserverCopy.copy(CrawlerTool.getPathFilmlist_json_diff_xz(), MserverDaten.system[MserverKonstanten.SYSTEM_EXPORT_FILMLISTE_DIFF_NR]);
            }

            // ======================================================
            // jetzt die anderen Uploads ins Web erledigen
            if (!MserverDaten.listeUpload.isEmpty()) {
                // nach dem Suchen Rechner Zeit zum Abau aller Verbindungen geben
                new MserverWarten().sekundenWarten(30);
            }

            for (MserverDatenUpload datenUpload : MserverDaten.listeUpload) {
                for (int i = 0; i <= 1; ++i) {
                    String[] srcPathFile = new String[]{CrawlerTool.getPathFilmlist_json_diff_xz(), CrawlerTool.getPathFilmlist_json_akt_xz()};
                    String[] destFileName = new String[]{MserverKonstanten.NAME_FILMLISTE_DIFF, MserverKonstanten.NAME_FILMLISTE_AKT};

                    MserverLog.systemMeldung("");
                    MserverLog.systemMeldung("--------------------------");
                    MserverLog.systemMeldung("Upload Liste: " + srcPathFile[i]);

                    // und jetzt der Upload/Copy
                    switch (datenUpload.arr[MserverDatenUpload.UPLOAD_ART_NR]) {
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

                    MserverLog.systemMeldung("--------------------------");
                    MserverLog.systemMeldung("");
                }
            }
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(989620146, MserverUpload.class.getName(), "MS_UploadFtp", ex);
        }
        MserverLog.systemMeldung("==========================");
        MserverLog.systemMeldung("Upload fertig");
    }

    private static void uploadFtp_(String srcPathFile, String destFileName, MserverDatenUpload datenUpload) {
        if (!MserverFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
            new MserverWarten().sekundenWarten(30);
            MserverLog.systemMeldung("2. Versuch Upload FTP");
            if (!MserverFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
                MserverLog.fehlerMeldung(649896079, MserverUpload.class.getName(), "FTP, 2.Versuch nicht geklappe");
            }
        }
    }

    private static void uploadCopy_(String srcPathFile, String destFileName, MserverDatenUpload datenUpload) {
        if (!MserverCopy.copy(srcPathFile, destFileName, datenUpload)) {
            // wenns nicht geklappt hat nochmal versuchen
            new MserverWarten().sekundenWarten(30);
            MserverLog.systemMeldung("2. Versuch Upload copy");
            if (!MserverCopy.copy(srcPathFile, destFileName, datenUpload)) {
                MserverLog.fehlerMeldung(798956236, MserverUpload.class.getName(), "Copy, 2.Versuch nicht geklappt");
            }
        }
    }

}
