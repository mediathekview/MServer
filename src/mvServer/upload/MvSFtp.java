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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import mvServer.daten.MvSDatenUpload;
import mvServer.tool.MvSKonstanten;
import mvServer.tool.MvSLog;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public class MvSFtp {

    private static String server, strPort, username, password, srcPathFile, destFileName;
    private static MvSDatenUpload datenUpload;
    private static boolean retFtp = false;

    public static boolean uploadFtp(String srcPathFile_, String destFileName_, MvSDatenUpload datenUpload_) {
        try {
            srcPathFile = srcPathFile_;
            destFileName = destFileName_;
            datenUpload = datenUpload_;
            server = datenUpload.arr[MvSDatenUpload.UPLOAD_SERVER_NR];
            strPort = datenUpload.arr[MvSDatenUpload.UPLOAD_PORT_NR];
            username = datenUpload.arr[MvSDatenUpload.UPLOAD_USER_NR];
            password = datenUpload.arr[MvSDatenUpload.UPLOAD_PWD_NR];
            MvSLog.systemMeldung("");
            MvSLog.systemMeldung("----------------------");
            MvSLog.systemMeldung("Upload start");
            MvSLog.systemMeldung("Server: " + server);
            Thread t = new Thread(new Ftp());
            t.start();

            int warten = MvSKonstanten.MAX_WARTEN_FTP_UPLOAD/*Minuten*/;
            MvSLog.systemMeldung("Max Laufzeit FTP[Min]: " + warten);
            MvSLog.systemMeldung("-----------------------------------");
            warten = 1000 * 60 * warten;
            t.join(warten);

            if (t != null) {
                if (t.isAlive()) {
                    MvSLog.fehlerMeldung(396958702, MvSFtp.class.getName(), "Der letzte FtpUpload läuft noch");
                    MvSLog.systemMeldung("und wird gekillt");
                    t.stop();
                    retFtp = false;
                }
            }
        } catch (Exception ex) {
            MvSLog.fehlerMeldung(739861047, MvSFtp.class.getName(), "uploadFtp", ex);
        }
        return retFtp;
    }

    private static class Ftp implements Runnable {

        @Override
        public synchronized void run() {
            int port = 0;
            final FTPClient ftp = new FTPClient();

            try {
                if (!strPort.equals("")) {
                    port = Integer.parseInt(strPort);
                }
            } catch (Exception ex) {
                MvSLog.fehlerMeldung(101203698, MvSFtp.class.getName(), "uploadFtp", ex);
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
                MvSLog.debugMeldung("Connected to " + server + " on " + (port > 0 ? port : ftp.getDefaultPort()));

                // After connection attempt, you should check the reply code to verify success.
                reply = ftp.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    MvSLog.debugMeldung("FTP server refused connection.");
                    return;
                }
            } catch (IOException e) {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException f) {
                    }
                }
                MvSLog.fehlerMeldung(969363254, MvSFtp.class.getName(), "MS_UploadFtp", e);
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
                MvSLog.debugMeldung("Remote system is " + ftp.getSystemType());
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                ftp.setUseEPSVwithIPv4(false);
                // ==========================
                // Filmliste hoch laden
                InputStream input;
                input = new FileInputStream(srcPathFile);
                String dest = datenUpload.getFilmlisteDestPfadName(destFileName);
                if (datenUpload.rename()) {
                    MvSLog.debugMeldung("Upload Filmliste + rename");
                    String dest_tmp = dest + "__";
                    String dest_old = dest + "_old";
                    ftp.storeFile(dest_tmp, input);
                    MvSLog.debugMeldung("Upload Filmliste " + dest_tmp);
                    ftp.rename(dest, dest_old);
                    MvSLog.debugMeldung("Rename alte Filmliste " + dest);
                    ftp.rename(dest_tmp, dest);
                    MvSLog.debugMeldung("Rename Filmliste " + dest);
                } else {
                    ftp.storeFile(dest, input);
                    MvSLog.debugMeldung("Upload Filmliste " + dest);
                }
                input.close();
                ftp.noop(); // check that control connection is working OK

                ftp.logout();
                retFtp = true; // dann hat alles gepasst
            } catch (FTPConnectionClosedException e) {
                MvSLog.fehlerMeldung(646362014, MvSFtp.class.getName(), "MS_UploadFtp", e);
            } catch (IOException e) {
                MvSLog.fehlerMeldung(989862047, MvSFtp.class.getName(), "MS_UploadFtp", e);
            } finally {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException f) {
                        // do nothing
                    }
                }
            }
        }
    }
}
