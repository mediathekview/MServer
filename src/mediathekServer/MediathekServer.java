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
import mediathekServer.search.MS_FilmeSuchen;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Log;
import mediathekServer.tool.MS_XmlLesen;
import mediathekServer.tool.MS_XmlSchreiben;
import mediathekServer.update.MS_Update;
import mediathekServer.upload.MS_Upload;

public class MediathekServer {

    private boolean allesLaden = false;
    private String output = "";
    private String imprtUrl = "";
    private String userAgent = "";
    private MS_Daten msDaten;

    public MediathekServer() {
    }

    public MediathekServer(String[] ar) {
        String pfad = "";
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
            MS_XmlSchreiben.xmlMusterDatenSchreiben();
            // und Tschüss
            System.exit(1);
        } else {
            MS_XmlLesen.xmlDatenLesen();
            MS_XmlLesen.xmlLogLesen();
        }
    }

    public void musterSchreiben(String[] ar) {
        String pfad = "";
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
        MS_Log.systemMeldung("Muster Konfig anlegen");
        // Demo schreiben
        MS_XmlSchreiben.xmlMusterDatenSchreiben();
        // und Tschüss
        System.exit(0);
    }

    public void starten() {
        // ---------------------------
        // Update suchen
        if (!MS_Update.updaten()) {
            MS_Log.fehlerMeldung(852104739, MediathekServer.class.getName(), "Update mit Fehler beendet");
        }

        // ---------------------------
        // Filme suchen
        if (!MS_FilmeSuchen.filmeSuchen(allesLaden, output, imprtUrl, userAgent)) {
            return;
        }

        // ---------------------------
        // Filmliste hochladen
        MS_Upload.upload(output);

        // ---------------------------
        undTschuess();
    }

    private void undTschuess() {
        MS_Log.printEndeMeldung();
        MS_XmlSchreiben.xmlLogSchreiben();
        System.exit(0);
    }
}
