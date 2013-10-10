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
import mServer.daten.MServerDatenSuchen;
import mServer.daten.MServerDatenUpload;
import mServer.tool.MServerDaten;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import mServer.tool.MServerWarten;

public class MServerUpload {

    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";
    private MServerMelden ms_Melden;

    public MServerUpload() {
        ms_Melden = new MServerMelden();
    }

    public void upload(MServerDatenSuchen aktDatenSuchen) {
        String filmDateiName = aktDatenSuchen.getExportFilmliste();
        String filmDateiPfad = MServerDaten.getVerzeichnisFilme();
        Iterator<MServerDatenUpload> it = MServerDaten.listeUpload.iterator();
        if (MServerDaten.listeUpload.size() > 0) {
            // nach dem Suchen Rechner Zeit zum Abau aller Verbindungen geben
            new MServerWarten().sekundenWarten(60);
        }
        while (it.hasNext()) {
            MServerDatenUpload datenUpload = it.next();
            if (datenUpload.arr[MServerKonstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_COPY)) {
                // ==============================================================
                // kopieren
                if (!uploadCopy_(filmDateiPfad, filmDateiName, datenUpload)) {
                    // ----------------------
                    // wenns nicht geklappt hat nochmal versuchen
                    new MServerWarten().sekundenWarten(60);
                    MServerLog.systemMeldung("2. Versuch Upload copy");
                    if (!uploadCopy_(filmDateiPfad, filmDateiName, datenUpload)) {
                        MServerLog.fehlerMeldung(798956236, MServerUpload.class.getName(), "Copy, 2.Versuch nicht geklappt");
                    }
                }
            } else if (datenUpload.arr[MServerKonstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_FTP)) {
                // ==============================================================
                // ftp
                if (!uploadFtp_(filmDateiPfad, filmDateiName, datenUpload)) {
                    // ----------------------
                    // wenns nicht geklappt hat nochmal versuchen
                    new MServerWarten().sekundenWarten(60);
                    MServerLog.systemMeldung("2. Versuch Upload FTP");
                    if (!uploadFtp_(filmDateiPfad, filmDateiName, datenUpload)) {
                        MServerLog.fehlerMeldung(649896079, MServerUpload.class.getName(), "FTP, 2.Versuch nicht geklappe");
                    }
                }
            }
        }
        MServerLog.systemMeldung("Upload Ok");
    }

    private boolean uploadFtp_(String filmDateiPfad, String filmDateiName, MServerDatenUpload datenUpload) {
        boolean ret = false;
        if (new MServerUploadFtp().uploadFtp(datenUpload.arr[MServerKonstanten.UPLOAD_SERVER_NR], datenUpload.arr[MServerKonstanten.UPLOAD_PORT_NR], datenUpload.arr[MServerKonstanten.UPLOAD_USER_NR],
                datenUpload.arr[MServerKonstanten.UPLOAD_PWD_NR], filmDateiPfad, filmDateiName, datenUpload)) {
            if (ms_Melden.melden(datenUpload.getUrlFilmliste(filmDateiName), datenUpload.getPrio())) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean uploadCopy_(String filmDateiPfad, String filmDateiName, MServerDatenUpload datenUpload) {
        boolean ret = false;
        if (MServerUploadCopy.copy(filmDateiPfad, filmDateiName, datenUpload)) {
            if (ms_Melden.melden(datenUpload.getUrlFilmliste(filmDateiName), datenUpload.getPrio())) {
                ret = true;
            }
        }
        return ret;

    }
}
