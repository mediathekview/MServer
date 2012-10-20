/*    
 *    MediathekView
 *    Copyright (C) 2008   W. Xaver
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package update;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import mediathek.tool.Funktionen;
import mediathek.tool.Konstanten;
import mediathek.tool.Log;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_UpdateSuchen {

    public static String checkVersion() {
        // prüft auf neue Version, dann laden und ersetzen
        String release;
        String downloadUrlProgramm;
        String[] ret;
        try {
            ret = suchen();
            release = ret[0];
            downloadUrlProgramm = ret[1];
            if (!release.equals("")) {
                if (checkObNeueVersion(release)) {
                    if (!downloadUrlProgramm.equals("")) {
                        return downloadUrlProgramm;
                    }
                }
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(825210369, MS_UpdateSuchen.class.getName(), "checkVersion", ex);
        }
        return "";
    }

    private static boolean checkObNeueVersion(String release) {
        // liefert true, wenn es eine neue Version gibt
        try {
            String haben = Funktionen.getBuildNr().replace(".", "");
            int intHaben = Integer.parseInt(haben);
            int intRelease = Integer.parseInt(release);
            if (intRelease > intHaben) {
                return true;
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(784510369, MS_UpdateSuchen.class.getName(), "checkObNeueVersion", ex);
        }
        return false;
    }

    private static String[] suchen() throws MalformedURLException, IOException, XMLStreamException {
        //<?xml version="1.0" encoding="UTF-8"?>
        //<Mediathek>
        //    <Program_Name>MediathekServer</Program_Name>
        //	<Program_Release>20</Program_Release>
        //    <Download_Programm>https://sourceforge.net/projects/zdfmediathk/</Download_Programm>
        //</Mediathek>
        String[] ret = new String[]{""/* release */, ""/* updateUrl */};
        int event;
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader parser;
        InputStreamReader inReader;
        int timeout = 10000;
        URLConnection conn;
        conn = new URL(MS_Konstanten.ADRESSE_PROGRAMM_VERSION).openConnection();
        conn.setRequestProperty("User-Agent", MS_Daten.getUserAgent());
        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(timeout);
        inReader = new InputStreamReader(conn.getInputStream(), Konstanten.KODIERUNG_UTF);
        parser = inFactory.createXMLStreamReader(inReader);
        while (parser.hasNext()) {
            event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                //parsername = parser.getLocalName();
                if (parser.getLocalName().equals("Program_Release")) {
                    ret[0] = parser.getElementText();
                } else if (parser.getLocalName().equals("Download_Programm")) {
                    ret[1] = parser.getElementText();
                }
            }
        }
        return ret;
    }
}
