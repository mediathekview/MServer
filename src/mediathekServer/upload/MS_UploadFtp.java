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
package mediathekServer.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import mediathek.controller.filmeLaden.importieren.DatenUrlFilmliste;
import mediathek.tool.GuiFunktionen;
import mediathekServer.daten.MS_DatenUpload;
import mediathekServer.tool.MS_Funktionen;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

public class MS_UploadFtp {

    private String server, strPort, username, password, filmlistePfad, filmlisteDateiname;
    private MS_DatenUpload datenUpload;
    private boolean retFtp = false;

    public boolean uploadFtp(String server_, String strPort_,
            String username_, String password_,
            String filmlistePfad_, String filmlisteDateiname_,
            MS_DatenUpload datenUpload_) {
        try {
            server = server_;
            strPort = strPort_;
            username = username_;
            password = password_;
            filmlistePfad = filmlistePfad_;
            filmlisteDateiname = filmlisteDateiname_;
            datenUpload = datenUpload_;
            MS_Log.systemMeldung("");
            MS_Log.systemMeldung("----------------------");
            MS_Log.systemMeldung("Upload start");
            MS_Log.systemMeldung("Server: " + server);
            Ftp f = new Ftp();
            Thread t = new Thread(f);
            t.start();
            t.join(MS_Konstanten.MAX_WARTEN_FTP_UPLOAD);
            if (t != null) {
                if (t.isAlive()) {
                    MS_Log.fehlerMeldung(396958702, MS_UploadFtp.class.getName(), "Der letzte FtpUpload läuft noch");
                    MS_Log.systemMeldung("und wird gekillt");
                    t.stop();
                    retFtp = false;
                }
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(739861047, MS_UploadFtp.class.getName(), "uploadFtp", ex);
        }
        return retFtp;
    }

    private class Ftp implements Runnable {

        @Override
        public synchronized void run() {
            // Liste der Filmlisten auktualisieren
            // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
            //String filmlisteDestPfadName = GuiFunktionen.addsPfad(filmlisteDestDir, filmlisteDateiname);
            //String listeFilmlistenDestPfadName = GuiFunktionen.addsPfad(filmlisteDestDir, MS_Konstanten.DATEINAME_LISTE_FILMLISTEN);
            DatenUrlFilmliste dfus = new DatenUrlFilmliste(datenUpload.getUrlFilmliste(filmlisteDateiname), "1", MS_Funktionen.getTime(), MS_Funktionen.getDate());
            File filmlisten = MS_ListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
            //
            int port = 0;
            try {
                if (!strPort.equals("")) {
                    port = Integer.parseInt(strPort);
                }
            } catch (Exception ex) {
                MS_Log.fehlerMeldung(101203698, MS_UploadFtp.class.getName(), "uploadFtp", ex);
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
                MS_Log.debugMeldung("Connected to " + server + " on " + (port > 0 ? port : ftp.getDefaultPort()));
                // After connection attempt, you should check the reply code to verify success.
                reply = ftp.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    MS_Log.debugMeldung("FTP server refused connection.");
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
                MS_Log.fehlerMeldung(969363254, MS_UploadFtp.class.getName(), "MS_UploadFtp", e);
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
                MS_Log.debugMeldung("Remote system is " + ftp.getSystemType());
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
                input = new FileInputStream(GuiFunktionen.addsPfad(filmlistePfad, filmlisteDateiname));
                ftp.storeFile(datenUpload.getFilmlisteDestPfadName(filmlisteDateiname), input);
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
                MS_Log.fehlerMeldung(646362014, MS_UploadFtp.class.getName(), "MS_UploadFtp", e);
            } catch (IOException e) {
                MS_Log.fehlerMeldung(989862047, MS_UploadFtp.class.getName(), "MS_UploadFtp", e);
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
        }
    }
}
