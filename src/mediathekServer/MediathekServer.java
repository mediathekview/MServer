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
package mediathekServer;

import java.io.File;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Log;
import mediathekServer.tool.MS_XmlLesen;
import mediathekServer.tool.MS_XmlSchreiben;
import update.MS_UpdateSuchen;
import update.MS_Updaten;

public class MediathekServer {

    private String pfad = "";
    private MS_Daten msDaten;

    public MediathekServer(String[] ar) {
        if (ar != null) {
            if (ar.length > 0) {
                if (!ar[0].startsWith("-")) {
                    if (!ar[0].endsWith(File.separator)) {
                        ar[0] += File.separator;
                    }
                    pfad = ar[0];
                }
            }
        }
        msDaten = new MS_Daten();
        MS_Daten.setBasisVerzeichnis(pfad);
        // Infos schreiben
        MS_Log.startMeldungen(this.getClass().getName());
        MS_Log.systemMeldung("");
        MS_Log.systemMeldung("");
        // los gehts
        if (!MS_Daten.konfigExistiert()) {
            MS_Log.fehlerMeldung(858589654, MediathekServer.class.getName(), new String[]{"Konfig-Datei existiert nicht", MS_Daten.getKonfigDatei()});
            // Demo schriben
            MS_XmlSchreiben.xmlDatenSchreiben();
            // und Tschüss
            System.exit(1);
        } else {
            MS_XmlLesen.xmlDatenLesen();
        }
    }

    public void starten() {
        // ---------------------------
        // Update suchen
        String updateUrl = MS_UpdateSuchen.checkVersion();
        if (!updateUrl.equals("")) {
            new MS_Updaten(pfad).updaten();
        }
        // ---------------------------
        // Filme suchen
        // ---------------------------
        // Filmliste hochladen


    }

    private void undTschuess() {
        MS_Log.printEndeMeldung();
        System.exit(0);
    }
}
