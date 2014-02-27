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
package mServer.tool;

import java.io.File;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import mServer.Main;

public class MSVFunktionen {

    public static String getPathJar() {
        String pFilePath = "pFile";
        File propFile = new File(pFilePath);
        if (!propFile.exists()) {
            try {
                CodeSource cS = Main.class.getProtectionDomain().getCodeSource();
                File jarFile = new File(cS.getLocation().toURI().getPath());
                String jarDir = jarFile.getParentFile().getPath();
                propFile = new File(jarDir + File.separator + pFilePath);
            } catch (Exception ex) {
            }
        }
        return propFile.getAbsolutePath().replace(pFilePath, "");
    }

    public static String getTime() {
        // liefert die Zeit in der Zeitzone "UTC"
        SimpleDateFormat sdf_zeit = new SimpleDateFormat("HH:mm:ss");
        sdf_zeit.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return sdf_zeit.format(new Date());
    }

    public static String getDate() {
        // liefert das Datum in der Zeitzone "UTC"
        SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");
        sdf_datum.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return sdf_datum.format(new Date());
    }
}
