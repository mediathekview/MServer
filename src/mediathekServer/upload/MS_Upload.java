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
import java.nio.channels.FileChannel;
import java.util.Iterator;
import mediathekServer.MS_Daten.MS_DatenUpload;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_Upload {

    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";

    public static boolean upload(String datei) {
        boolean ret = false;
        Iterator<MS_DatenUpload> it = MS_Daten.listeUpload.iterator();
        while (it.hasNext()) {
            MS_DatenUpload datenUpload = it.next();
            if (datenUpload.arr[MS_Konstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_COPY)) {
                copy(datei, datenUpload.arr[MS_Konstanten.UPLOAD_DEST_DIR_NR]);
            } else if (datenUpload.arr[MS_Konstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_FTP)) {
                // uploadFtp(String server, int port, String username, String password, String remote, String local) {
                MS_UploadFtp.uploadFtp(datenUpload.arr[MS_Konstanten.UPLOAD_SERVER_NR], datenUpload.arr[MS_Konstanten.UPLOAD_PORT_NR], datenUpload.arr[MS_Konstanten.UPLOAD_USER_NR],
                        datenUpload.arr[MS_Konstanten.UPLOAD_PWD_NR], datenUpload.arr[MS_Konstanten.UPLOAD_DEST_DIR_NR], datei);
            }
        }
        return ret;
    }

    public static boolean copy(String srcFile, String destDir) {
        boolean ret = false;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            String destFile;
            if (!destDir.endsWith(File.separator)) {
                destFile = destDir + File.separator + srcFile;
            } else {
                destFile = destDir + srcFile;
            }
            File destDirFile = new File(destDir);
            destDirFile.mkdirs();
            inChannel = new FileInputStream(srcFile).getChannel();
            outChannel = new FileOutputStream(destFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
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
}
