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
import java.text.SimpleDateFormat;
import java.util.Date;
import mediathek.controller.filmeLaden.importieren.DatenFilmUpdateServer;
import mediathek.tool.GuiFunktionen;
import mediathekServer.daten.MS_DatenUpload;
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

    private static SimpleDateFormat sdf_zeit = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");

//    public static String server = null;
//    public static int port = 0;
//    public static String username = null;
//    public static String password = null;
//    public static String remote = null;
//    public static String local = null;
    public static boolean uploadFtp(String server, String strPort, String username, String password, String filmlistePfad, String filmlisteDateiname,
            MS_DatenUpload datenUpload) {
        boolean ret = false;
        // Liste der Filmlisten auktualisieren
        // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
        //String filmlisteDestPfadName = GuiFunktionen.addsPfad(filmlisteDestDir, filmlisteDateiname);
        //String listeFilmlistenDestPfadName = GuiFunktionen.addsPfad(filmlisteDestDir, MS_Konstanten.DATEINAME_LISTE_FILMLISTEN);
        DatenFilmUpdateServer dfus = new DatenFilmUpdateServer(datenUpload.getUrlFilmliste(filmlisteDateiname), "1", sdf_zeit.format(new Date()), sdf_datum.format(new Date()), "");
        File filmlisten = MS_ListeFilmlisten.filmlisteEintragen(datenUpload.getUrlListeFilmlisten(), dfus);
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
        boolean binaryTransfer = false;
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
                return false;
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
            return false;
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
            ret = true; // dann hat alles gepasst
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
        return ret;
    }
}
