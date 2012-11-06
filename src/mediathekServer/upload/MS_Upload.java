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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import mediathekServer.daten.MS_DatenUpload;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_Upload {

    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";

    public static boolean upload(String filmDateiPfad, String filmDateiName) {
        boolean ret = false;
        Iterator<MS_DatenUpload> it = MS_Daten.listeUpload.iterator();
        while (it.hasNext()) {
            MS_DatenUpload datenUpload = it.next();
            if (datenUpload.arr[MS_Konstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_COPY)) {
                // ==============================================================
                // kopieren
                if (copy(filmDateiPfad, filmDateiName, datenUpload.arr[MS_Konstanten.UPLOAD_DEST_DIR_NR])) {
                    melden(addUrl(datenUpload.arr[MS_Konstanten.UPLOAD_SERVER_URL_FILME_NR], filmDateiName));
                }
            } else if (datenUpload.arr[MS_Konstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_FTP)) {
                // ==============================================================
                // ftp
                if (MS_UploadFtp.uploadFtp(datenUpload.arr[MS_Konstanten.UPLOAD_SERVER_NR], datenUpload.arr[MS_Konstanten.UPLOAD_PORT_NR], datenUpload.arr[MS_Konstanten.UPLOAD_USER_NR],
                        datenUpload.arr[MS_Konstanten.UPLOAD_PWD_NR], filmDateiPfad, filmDateiName, datenUpload.arr[MS_Konstanten.UPLOAD_DEST_DIR_NR])) {
                    melden(addUrl(datenUpload.arr[MS_Konstanten.UPLOAD_SERVER_URL_FILME_NR], filmDateiName));
                }
            }
        }
        return ret;
    }

    private static String addUrl(String u1, String u2) {
        if (u1.endsWith("/")) {
            return u1 + u2;
        } else {
            return u1 + "/" + u2;
        }
    }

    private static boolean copy(String filmDateiPfad, String filmDateiName, String strDestDir) {
        boolean ret = false;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            String strDestDirFile;
            if (!strDestDir.endsWith(File.separator)) {
                strDestDirFile = strDestDir + File.separator + filmDateiName;
            } else {
                strDestDirFile = strDestDir + filmDateiName;
            }
            new File(strDestDir).mkdirs();
            inChannel = new FileInputStream(filmDateiPfad + filmDateiName).getChannel();
            outChannel = new FileOutputStream(strDestDirFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            ret = true;
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(747452360, MS_Upload.class.getName(), "copy", ex);
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (Exception ex) {
                MS_Log.fehlerMeldung(252160987, MS_Upload.class.getName(), "copy", ex);
            }
        }
        return ret;
    }

    private static void melden(String urlFilm) {
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");
        try {
            if (!urlFilm.equals("")) {
                String zeit = sdf_zeit.format(new Date());
                String datum = sdf_datum.format(new Date());
                System.out.println("Server melden, Datum: " + datum + "  Zeit: " + zeit + "  URL: " + urlFilm);
                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
                String urlMelden = MS_Konstanten.UPDATE_SERVER_FILMLISTE
                        + "?pwd=" + MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_PWD_NR]
                        + "&zeit=" + zeit
                        + "&datum=" + datum
                        + "&server=" + urlFilm;
                int timeout = 10000;
                URLConnection conn = null;
                conn = new URL(urlMelden).openConnection();
                conn.setRequestProperty("User-Agent", MS_Daten.getUserAgent());
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MS_Konstanten.KODIERUNG_UTF);
                inReader.read();
                inReader.close();
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(301256907, MS_Upload.class.getName(), "melden", ex);
        }
    }
}
