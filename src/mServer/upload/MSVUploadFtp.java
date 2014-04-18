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
import msearch.filmeLaden.DatenUrlFilmliste;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

public class MSVUploadFtp {

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
            Ftp f = new Ftp();
            Thread t = new Thread(f);
            t.start();
            t.join(MSVKonstanten.MAX_WARTEN_FTP_UPLOAD);
            if (t != null) {
                if (t.isAlive()) {
                    MSVLog.fehlerMeldung(396958702, MSVUploadFtp.class.getName(), "Der letzte FtpUpload läuft noch");
                    MSVLog.systemMeldung("und wird gekillt");
                    t.stop();
                    retFtp = false;
                }
            }
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(739861047, MSVUploadFtp.class.getName(), "uploadFtp", ex);
        }
        return retFtp;
    }

    private static class Ftp implements Runnable {

        @Override
        public synchronized void run() {
            // Liste der Filmlisten auktualisieren
            // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
            //String filmlisteDestPfadName = GuiFunktionen.addsPfad(filmlisteDestDir, filmlisteDateiname);
            //String listeFilmlistenDestPfadName = GuiFunktionen.addsPfad(filmlisteDestDir, MS_Konstanten.DATEINAME_LISTE_FILMLISTEN);
            DatenUrlFilmliste dfus = new DatenUrlFilmliste(datenUpload.getUrlFilmliste(destFileName), "1", MSVFunktionen.getTime(), MSVFunktionen.getDate());
            File filmlisten = null;
            filmlisten = MSVListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
            //
            int port = 0;
            try {
                if (!strPort.equals("")) {
                    port = Integer.parseInt(strPort);
                }
            } catch (Exception ex) {
                MSVLog.fehlerMeldung(101203698, MSVUploadFtp.class.getName(), "uploadFtp", ex);
                port = 0;
            }
            boolean binaryTransfer = true;
            boolean localActive = false, useEpsvWithIPv4 = false;
            long keepAliveTimeout = -1;
            int controlKeepAliveReplyTimeout = -1;
            String protocol = null; // SSL protocol
            String trustmgr = null;
            String proxyHost = null;
            int proxyPort = 80;
            String proxyUser = null;
            String proxyPassword = null;
            final FTPClient ftp;
            if (protocol == null) {
                if (proxyHost != null) {
                    ftp = new FTPHTTPClient(proxyHost, proxyPort, proxyUser, proxyPassword);
                } else {
                    ftp = new FTPClient();
                }
            } else {
                FTPSClient ftps;
                if (protocol.equals("true")) {
                    ftps = new FTPSClient(true);
                } else if (protocol.equals("false")) {
                    ftps = new FTPSClient(false);
                } else {
                    String prot[] = protocol.split(",");
                    if (prot.length == 1) { // Just protocol
                        ftps = new FTPSClient(protocol);
                    } else { // protocol,true|false
                        ftps = new FTPSClient(prot[0], Boolean.parseBoolean(prot[1]));
                    }
                }
                ftp = ftps;
                if ("all".equals(trustmgr)) {
                    ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
                } else if ("valid".equals(trustmgr)) {
                    ftps.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
                } else if ("none".equals(trustmgr)) {
                    ftps.setTrustManager(null);
                }
            }
            if (keepAliveTimeout >= 0) {
                ftp.setControlKeepAliveTimeout(keepAliveTimeout);
            }
            if (controlKeepAliveReplyTimeout >= 0) {
                ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
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
                        // do nothing
                    }
                }
                MSVLog.fehlerMeldung(969363254, MSVUploadFtp.class.getName(), "MS_UploadFtp", e);
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
                if (binaryTransfer) {
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);
                }
                // passive mode as default
                if (localActive) {
                    ftp.enterLocalActiveMode();
                } else {
                    ftp.enterLocalPassiveMode();
                }
                ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);
                // ==========================
                // Filmliste hoch laden
                InputStream input;
                input = new FileInputStream(srcPathFile);
                ftp.storeFile(datenUpload.getFilmlisteDestPfadName(destFileName), input);
                input.close();
                ftp.noop(); // check that control connection is working OK
                // ==========================
                // Liste der Filmlisten hoch laden
                if (filmlisten != null) {
                    input = new FileInputStream(filmlisten);
                    ftp.storeFile(datenUpload.getListeFilmlistenDestPfadName(), input);
                    input.close();
                    ftp.noop(); // check that control connection is working OK
                }
                ftp.logout();
                retFtp = true; // dann hat alles gepasst
            } catch (FTPConnectionClosedException e) {
                MSVLog.fehlerMeldung(646362014, MSVUploadFtp.class.getName(), "MS_UploadFtp", e);
            } catch (IOException e) {
                MSVLog.fehlerMeldung(989862047, MSVUploadFtp.class.getName(), "MS_UploadFtp", e);
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
            if (filmlisten != null) {
                try {
                    filmlisten.delete();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
