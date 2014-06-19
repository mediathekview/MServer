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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import mServer.daten.MSVDatenUpload;
import mServer.tool.MSVFunktionen;
import mServer.tool.MSVKonstanten;
import mServer.tool.MSVLog;
import msearch.filmeLaden.DatenFilmlisteUrl;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public class MSVFtp {

    private static String server, strPort, username, password, srcPathFile, destFileName;
    private static MSVDatenUpload datenUpload;
    private static boolean retFtp = false;

    public static boolean uploadFtp(String srcPathFile_, String destFileName_, MSVDatenUpload datenUpload_) {
        try {
            srcPathFile = srcPathFile_;
            destFileName = destFileName_;
            datenUpload = datenUpload_;
            server = datenUpload.arr[MSVDatenUpload.UPLOAD_SERVER_NR];
            strPort = datenUpload.arr[MSVDatenUpload.UPLOAD_PORT_NR];
            username = datenUpload.arr[MSVDatenUpload.UPLOAD_USER_NR];
            password = datenUpload.arr[MSVDatenUpload.UPLOAD_PWD_NR];
            MSVLog.systemMeldung("");
            MSVLog.systemMeldung("----------------------");
            MSVLog.systemMeldung("Upload start");
            MSVLog.systemMeldung("Server: " + server);
            Thread t = new Thread(new Ftp());
            t.start();
            t.join(MSVKonstanten.MAX_WARTEN_FTP_UPLOAD);
            if (t != null) {
                if (t.isAlive()) {
                    MSVLog.fehlerMeldung(396958702, MSVFtp.class.getName(), "Der letzte FtpUpload läuft noch");
                    MSVLog.systemMeldung("und wird gekillt");
                    t.stop();
                    retFtp = false;
                }
            }
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(739861047, MSVFtp.class.getName(), "uploadFtp", ex);
        }
        return retFtp;
    }

    private static class Ftp implements Runnable {

        @Override
        public synchronized void run() {
            int port = 0;
            final FTPClient ftp = new FTPClient();
//            File filmlisten = null;
//
//            if (datenUpload.aktListeFilmlisten()) {
//                // Liste der Filmlisten auktualisieren
//                MSVLog.systemMeldung("");
//                MSVLog.systemMeldung("und auch die Liste mit Filmlisten-DownloadURLs aktualisieren");
//                DatenFilmlisteUrl dfus = new DatenFilmlisteUrl(datenUpload.getUrlFilmliste(destFileName), "1", MSVFunktionen.getTime(), MSVFunktionen.getDate(), DatenFilmlisteUrl.SERVER_ART_OLD);
//                filmlisten = MSVListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
//            }

            try {
                if (!strPort.equals("")) {
                    port = Integer.parseInt(strPort);
                }
            } catch (Exception ex) {
                MSVLog.fehlerMeldung(101203698, MSVFtp.class.getName(), "uploadFtp", ex);
                port = 0;
            }

            // suppress login details
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

            // connect versuchen
            try {
                int reply;
                if (port > 0) {
                    ftp.connect(server, port);
                } else {
                    ftp.connect(server);
                }
                MSVLog.debugMeldung("Connected to " + server + " on " + (port > 0 ? port : ftp.getDefaultPort()));

                // After connection attempt, you should check the reply code to verify success.
                reply = ftp.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    MSVLog.debugMeldung("FTP server refused connection.");
                    return;
                }
            } catch (IOException e) {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException f) {
                    }
                }
                MSVLog.fehlerMeldung(969363254, MSVFtp.class.getName(), "MS_UploadFtp", e);
                return;
            }

            // ==================================
            // login und Daten übertragen
            __upload:
            try {
                if (!ftp.login(username, password)) {
                    ftp.logout();
                    break __upload;
                }
                MSVLog.debugMeldung("Remote system is " + ftp.getSystemType());
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                ftp.setUseEPSVwithIPv4(false);
                // ==========================
                // Filmliste hoch laden
                InputStream input;
                input = new FileInputStream(srcPathFile);
                String dest = datenUpload.getFilmlisteDestPfadName(destFileName);
                if (datenUpload.rename()) {
                    MSVLog.debugMeldung("Upload Filmliste + rename");
                    String dest_tmp = dest + "__";
                    String dest_old = dest + "_old";
                    ftp.storeFile(dest_tmp, input);
                    MSVLog.debugMeldung("Upload Filmliste " + dest_tmp);
                    ftp.rename(dest, dest_old);
                    MSVLog.debugMeldung("Rename alte Filmliste " + dest);
                    ftp.rename(dest_tmp, dest);
                    MSVLog.debugMeldung("Rename Filmliste " + dest);
                } else {
                    ftp.storeFile(dest, input);
                    MSVLog.debugMeldung("Upload Filmliste " + dest);
                }
                input.close();
                ftp.noop(); // check that control connection is working OK

                // ==========================
                // Liste der Filmlisten hoch laden
//                if (filmlisten != null) {
//                    input = new FileInputStream(filmlisten);
//                    ftp.storeFile(datenUpload.getListeFilmlistenDestPfadName(), input);
//                    input.close();
//                    ftp.noop(); // check that control connection is working OK
//                }
                ftp.logout();
                retFtp = true; // dann hat alles gepasst
            } catch (FTPConnectionClosedException e) {
                MSVLog.fehlerMeldung(646362014, MSVFtp.class.getName(), "MS_UploadFtp", e);
            } catch (IOException e) {
                MSVLog.fehlerMeldung(989862047, MSVFtp.class.getName(), "MS_UploadFtp", e);
            } finally {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException f) {
                        // do nothing
                    }
                }
            }
            // ==================================
//            if (filmlisten != null) {
//                try {
//                    filmlisten.delete();
//                } catch (Exception ignore) {
//                }
//            }
        }
    }
}
