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
package mServer.update;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import mServer.tool.MServerDaten;
import mServer.tool.MServerKonstanten;
import mServer.tool.MServerLog;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchFunktionen;
import msearch.tool.MSearchGuiFunktionen;

public class MServerUpdateSuchen {

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
            MServerLog.fehlerMeldung(825210369, MServerUpdateSuchen.class.getName(), "checkVersion", ex);
        }
        return "";
    }

    private static boolean checkObNeueVersion(String release) {
        // liefert true, wenn es eine neue Version gibt
        try {
            String haben = MSearchFunktionen.getBuildNr().replace(".", "");
            int intHaben = Integer.parseInt(haben);
            int intRelease = Integer.parseInt(release.replace(".", ""));
            if (intRelease > intHaben) {
                return true;
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(784510369, MServerUpdateSuchen.class.getName(), "checkObNeueVersion", ex);
        }
        return false;
    }

    private static String[] suchen() throws MalformedURLException, IOException, XMLStreamException {
        // <title><![CDATA[/Entwicklerversion/MediathekView_3.0.0_2012.10.26.zip]]></title>
        final String PROGRAMM_UPDATE_TAG_CDATA_TITEL = "MediathekServer_";
        final String PROGRAMM_UPDATE_TAG_TITEL = "title";
        // <link>http://176.28.14.91/mediathek1/MediathekServer_2012.11.10.zip</link>
        final String PROGRAMM_UPDATE_TAG_URL = "link";
        //
        String[] ret = new String[]{""/* release */, ""/* updateUrl */};
        int event;
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader parser;
        InputStreamReader inReader;
        int timeout = 10000;
        URLConnection conn;
        conn = new URL(MServerKonstanten.PROGRAMM_UPDATE_URL_RSS).openConnection();
        conn.setRequestProperty("User-Agent", MServerDaten.getUserAgent());
        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(timeout);
        inReader = new InputStreamReader(conn.getInputStream(), MSearchConst.KODIERUNG_UTF);
        parser = inFactory.createXMLStreamReader(inReader);
        boolean found = false;
        while (parser.hasNext()) {
            event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                //String parsername = parser.getLocalName();
                if (parser.getLocalName().contains(PROGRAMM_UPDATE_TAG_TITEL)) {
                    String text = parser.getElementText();
                    if (text.contains(PROGRAMM_UPDATE_TAG_CDATA_TITEL)) {
                        found = true;
                        final String s1 = "MediathekServer_";
                        ret[0] = text.substring(text.indexOf(s1) + s1.length(), text.indexOf(".zip"));
                    }
                }
                if (found && parser.getLocalName().equals(PROGRAMM_UPDATE_TAG_URL)) {
                    ret[1] = parser.getElementText();
                    return ret;
                }
            }
        }
        return null;
    }

    public static File updateLaden(String url, String zielPfad, String userAgent) {
        String zielDatei = MSearchGuiFunktionen.addsPfad(zielPfad, MServerKonstanten.PROGRAMMDATEI_UPDATE);
        File ret = new File(zielDatei);
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        BufferedInputStream in = null;
        FileOutputStream fOut = null;
        try {
            conn = new URL(url).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("User-Agent", userAgent);
            in = new BufferedInputStream(conn.getInputStream());
            fOut = new FileOutputStream(ret);
            final byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                fOut.write(buffer, 0, n);
            }
        } catch (Exception ex) {
            ret = null;
            MServerLog.fehlerMeldung(485963614, MServerUpdateSuchen.class.getName(), "updateLaden " + url, ex);
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
