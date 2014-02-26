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
import mServer.daten.MServerSearchTask;
import mServer.daten.MServerDatenUpload;
import mServer.tool.MServerDaten;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import mServer.tool.MServerWarten;
import msearch.daten.MSearchConfig;

public class MServerUpload {

    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";

    public static void upload(MServerSearchTask aktSearchTask) {
        // ==================================================
        // erst lokale Exports erledigen
        if (!MServerDaten.system[MServerKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_NR].isEmpty()) {
            MServerCopy.copy(MSearchConfig.getPathFilmlist_json_xz(), MServerDaten.system[MServerKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_NR]);
        }
        if (aktSearchTask.orgListeAnlegen() && !MServerDaten.system[MServerKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR].isEmpty()) {
            MServerCopy.copy(MSearchConfig.getPathFilmlist_org_xz(), MServerDaten.system[MServerKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_ORG_NR]);
        }
        if (!MServerDaten.system[MServerKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_DIFF_NR].isEmpty()) {
            MServerCopy.copy(MSearchConfig.getPathFilmlist_diff_xz(), MServerDaten.system[MServerKonstanten.SYSTEM_EXPORT_FILE_FILMLISTE_DIFF_NR]);
        }

        // ======================================================
        // jetzt die anderen Uploads erledigen
        String destFileName;
        String srcPathFile;
        Iterator<MServerDatenUpload> it = MServerDaten.listeUpload.iterator();
        if (MServerDaten.listeUpload.size() > 0) {
            // nach dem Suchen Rechner Zeit zum Abau aller Verbindungen geben
            new MServerWarten().sekundenWarten(30);
        }
        while (it.hasNext()) {
            MServerDatenUpload datenUpload = it.next();
            if (datenUpload.arr[MServerDatenUpload.UPLOAD_FORMAT_NR].equals(MServerDatenUpload.FORMAT_JSON)) {
                srcPathFile = MSearchConfig.getPathFilmlist_json_xz();
                destFileName = aktSearchTask.getExportJsonName();
            } else {
                srcPathFile = MSearchConfig.getPathFilmlist_xml_bz2();
                destFileName = aktSearchTask.getExportXmlName();
            }

            switch (datenUpload.arr[MServerDatenUpload.UPLOAD_ART_NR]) {
                case UPLOAD_ART_COPY:
                    // ==============================================================
                    // kopieren
                    if (!uploadCopy_(srcPathFile, destFileName, datenUpload)) {
                        // wenns nicht geklappt hat nochmal versuchen
                        new MServerWarten().sekundenWarten(60);
                        MServerLog.systemMeldung("2. Versuch Upload copy");
                        if (!uploadCopy_(srcPathFile, destFileName, datenUpload)) {
                            MServerLog.fehlerMeldung(798956236, MServerUpload.class.getName(), "Copy, 2.Versuch nicht geklappt");
                        }
                    }
                    break;

                case UPLOAD_ART_FTP:
                    // ==============================================================
                    // ftp
                    if (!uploadFtp_(srcPathFile, destFileName, datenUpload)) {
                        // wenns nicht geklappt hat nochmal versuchen
                        new MServerWarten().sekundenWarten(60);
                        MServerLog.systemMeldung("2. Versuch Upload FTP");
                        if (!uploadFtp_(srcPathFile, destFileName, datenUpload)) {
                            MServerLog.fehlerMeldung(649896079, MServerUpload.class.getName(), "FTP, 2.Versuch nicht geklappe");
                        }
                    }
                    break;
            }
        }
        MServerLog.systemMeldung("Upload Ok");
    }

    private static boolean uploadFtp_(String srcPathFile, String destFileName, MServerDatenUpload datenUpload) {
        boolean ret = false;
        if (MServerUploadFtp.uploadFtp(srcPathFile, destFileName, datenUpload)) {
            if (MServerMelden.melden(destFileName, datenUpload)) {
                ret = true;
            }
        }
        return ret;
    }

    private static boolean uploadCopy_(String srcPathFile, String destFileName, MServerDatenUpload datenUpload) {
        boolean ret = false;
        if (MServerUploadCopy.copy(srcPathFile, destFileName, datenUpload)) {
            if (MServerMelden.melden(destFileName, datenUpload)) {
                ret = true;
            }
        }
        return ret;

    }
}
