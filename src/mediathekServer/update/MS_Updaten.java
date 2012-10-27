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
package mediathekServer.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import mediathekServer.tool.MS_Log;

public class MS_Updaten {

    public static boolean updaten(String zipDatei, String nachDir, String userAgent) {
        boolean ret = true;
        File upFile;
        File zielDir;
        __update:
        try {
            upFile = updateLaden(zipDatei, userAgent);
            if (upFile == null) {
                MS_Log.fehlerMeldung(121214589, MS_Update.class.getName(), "Das Laden des Updatefiles hat nicht geklappt");
                break __update;
            }
            zielDir = new File(nachDir);
            if (!zielDir.exists()) {
                MS_Log.fehlerMeldung(912537098, MS_Update.class.getName(), "Das Zielverzeichnis existiert nicht");
                break __update;
            }
            ZipFile zipFile = new ZipFile(upFile);
            Enumeration entries;
            entries = zipFile.entries();
            byte[] buffer = new byte[16384];
            int len;
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryFileName = entry.getName();
                File dir = buildDirectoryHierarchyFor(entryFileName, zielDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (!entry.isDirectory()) {
                    BufferedOutputStream bos = new BufferedOutputStream(
                            new FileOutputStream(new File(zielDir, entryFileName)));
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    while ((len = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                }
            }
            zipFile.close();
        } catch (Exception ex) {
            ret = false;
            MS_Log.fehlerMeldung(825413078, MS_Updaten.class.getName(), "updaten", ex);
        }
        return ret;
    }

    private static File buildDirectoryHierarchyFor(String entryName, File destDir) {
        int lastIndex = entryName.lastIndexOf('/');
        String entryFileName = entryName.substring(lastIndex + 1);
        String internalPathToEntry = entryName.substring(0, lastIndex + 1);
        return new File(destDir, internalPathToEntry);
    }

    private static File updateLaden(String url, String userAgent) {
        File ret = null;
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        BufferedInputStream in = null;
        FileOutputStream fOut = null;
        try {
            conn = new URL(url).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("User-Agent", userAgent);
            File tmpFile = File.createTempFile("mediathek", null);
            tmpFile.deleteOnExit();
            in = new BufferedInputStream(conn.getInputStream());
            fOut = new FileOutputStream(tmpFile);
            final byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                fOut.write(buffer, 0, n);
            }
            ret = tmpFile;
        } catch (Exception ex) {
            ret = null;
            MS_Log.fehlerMeldung(485963614, MS_Updaten.class.getName(), "updateLaden " + url, ex);
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
            }
        }
        return ret;
    }
}
