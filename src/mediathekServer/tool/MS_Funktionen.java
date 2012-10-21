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
package mediathekServer.tool;

import java.io.File;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.Date;
import mediathek.tool.Funktionen;
import mediathekServer.Main;

public class MS_Funktionen {

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

    public static String getCompileDate() {
        String ret = "";
        try {
            //Version
            Date d = new Date(Main.class.getResource("Main.class").openConnection().getLastModified());
            ret = new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(d);
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(989632104, MS_Log.class.getName(), "getCompileDate", ex);
        }
        return ret;
    }

    public static String getVersion() {
        return MS_Konstanten.VERSION + " [" + Funktionen.getBuildNr() + "]";
    }
}
