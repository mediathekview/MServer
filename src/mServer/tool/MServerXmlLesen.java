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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import mServer.daten.MServerDatenUpload;
import mServer.daten.MServerSearchTask;
import msearch.tool.MSearchConst;

public class MServerXmlLesen {

    public static void xmlDatenLesen() {
        try {
            String datei = MServerDaten.getKonfigDatei();
            if (new File(datei).exists()) {
                //nur wenn die Datei schon existiert
                int event;
                XMLInputFactory inFactory = XMLInputFactory.newInstance();
                inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
                XMLStreamReader parser;
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(datei), MSearchConst.KODIERUNG_UTF);
                parser = inFactory.createXMLStreamReader(in);
                while (parser.hasNext()) {
                    event = parser.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        //String t = parser.getLocalName();
                        if (parser.getLocalName().equals(MServerKonstanten.SYSTEM)) {
                            get(parser, event, MServerKonstanten.SYSTEM, MServerKonstanten.SYSTEM_COLUMN_NAMES, MServerDaten.system);
                        }
                        if (parser.getLocalName().equals(MServerSearchTask.SUCHEN)) {
                            MServerSearchTask cron = new MServerSearchTask();
                            get(parser, event, MServerSearchTask.SUCHEN, MServerSearchTask.SUCHEN_COLUMN_NAMES, cron.arr);
                            MServerDaten.listeSuchen.add(cron);
                        }
                        if (parser.getLocalName().equals(MServerDatenUpload.UPLOAD)) {
                            MServerDatenUpload upload = new MServerDatenUpload();
                            get(parser, event, MServerDatenUpload.UPLOAD, MServerDatenUpload.UPLOAD_COLUMN_NAMES, upload.arr);
                            MServerDaten.listeUpload.add(upload);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(909078531, MServerXmlLesen.class.getName(), "xmlDatenLesen", ex);
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
                            strRet[i] = parser.getElementText().trim();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ret = false;
            if (log) {
                MServerLog.fehlerMeldung(201456980, MServerXmlLesen.class.getName(), "get", ex);
            }
        }
        return ret;
    }
}
