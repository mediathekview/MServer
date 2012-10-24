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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import mediathek.tool.Konstanten;

public class MS_XmlLesen {

    public static void xmlLogLesen() {
        try {
            String datei = MS_Daten.getLogDatei();
            if (new File(datei).exists()) {
                //nur wenn die Datei schon existiert
                int event;
                XMLInputFactory inFactory = XMLInputFactory.newInstance();
                inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
                XMLStreamReader parser;
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(datei), Konstanten.KODIERUNG_UTF);
                parser = inFactory.createXMLStreamReader(in);
                while (parser.hasNext()) {
                    event = parser.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        //String t = parser.getLocalName();
                        if (parser.getLocalName().equals(MS_LogMeldung.MS_LOG)) {
                            MS_LogMeldung meldung = new MS_LogMeldung();
                            get(parser, event, MS_LogMeldung.MS_LOG, MS_LogMeldung.MS_LOG_COLUMN_NAMES, meldung.arr);
                            MS_Daten.logFile.listeLogMeldungen.add(meldung);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(696307458, MS_XmlLesen.class.getName(), "xmlLogLesen", ex);
        } finally {
        }
    }

    public static void xmlDatenLesen() {
        try {
            String datei = MS_Daten.getKonfigDatei();
            if (new File(datei).exists()) {
                //nur wenn die Datei schon existiert
                int event;
                XMLInputFactory inFactory = XMLInputFactory.newInstance();
                inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
                XMLStreamReader parser;
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(datei), Konstanten.KODIERUNG_UTF);
                parser = inFactory.createXMLStreamReader(in);
                while (parser.hasNext()) {
                    event = parser.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        //String t = parser.getLocalName();
                        if (parser.getLocalName().equals(MS_Konstanten.SYSTEM)) {
                            get(parser, event, MS_Konstanten.SYSTEM, MS_Konstanten.SYSTEM_COLUMN_NAMES, MS_Daten.system);
                        }
                        if (parser.getLocalName().equals(MS_Konstanten.UPDATE)) {
                            get(parser, event, MS_Konstanten.UPDATE, MS_Konstanten.UPDATE_COLUMN_NAMES, MS_Daten.update);
                        }
                        if (parser.getLocalName().equals(MS_Konstanten.SUCHEN)) {
                            get(parser, event, MS_Konstanten.SUCHEN, MS_Konstanten.SUCHEN_COLUMN_NAMES, MS_Daten.suchen);
                        }
                        if (parser.getLocalName().equals(MS_Konstanten.UPLOAD)) {
                            get(parser, event, MS_Konstanten.UPLOAD, MS_Konstanten.UPLOAD_COLUMN_NAMES, MS_Daten.upload);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(909078531, MS_XmlLesen.class.getName(), "xmlDatenLesen", ex);
        } finally {
        }
    }

    private static boolean get(XMLStreamReader parser, int event, String xmlElem, String[] xmlNames, String[] strRet) {
        return get(parser, event, xmlElem, xmlNames, strRet, true);
    }

    private static boolean get(XMLStreamReader parser, int event, String xmlElem, String[] xmlNames, String[] strRet, boolean log) {
        boolean ret = true;
        int maxElem = strRet.length;
        for (int i = 0; i < maxElem; ++i) {
            if (strRet[i] == null) {
                // damit Vorgaben nicht verschwinden!
                strRet[i] = "";
            }
        }
        try {
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (parser.getLocalName().equals(xmlElem)) {
                        break;
                    }
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    for (int i = 0; i < maxElem; ++i) {
                        if (parser.getLocalName().equals(xmlNames[i])) {
                            strRet[i] = parser.getElementText();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ret = false;
            if (log) {
                MS_Log.fehlerMeldung(201456980, MS_XmlLesen.class.getName(), "get", ex);
            }
        }
        return ret;
    }
}
