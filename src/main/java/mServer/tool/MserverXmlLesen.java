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
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import mServer.daten.MserverDatenUpload;
import mServer.daten.MserverSearchTask;

public class MserverXmlLesen {
    private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
    public static void xmlDatenLesen() {
        xmlDatenLesen(MserverDaten.getKonfigDatei());
        xmlDatenLesen(MserverDaten.getUploadDatei());
    }

    public static void xmlDatenLesen(String datei) {
        EtmPoint performancePoint = etmMonitor.createPoint("MserverXmlLesen:xmlDatenLesen");
        try {
            if (new File(datei).exists()) {
                //nur wenn die Datei schon existiert
                int event;
                XMLInputFactory inFactory = XMLInputFactory.newInstance();
                inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
                XMLStreamReader parser;
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(datei), StandardCharsets.UTF_8);
                parser = inFactory.createXMLStreamReader(in);
                while (parser.hasNext()) {
                    event = parser.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        //String t = parser.getLocalName();
                        if (parser.getLocalName().equals(MserverKonstanten.SYSTEM)) {
                            get(parser, event, MserverKonstanten.SYSTEM, MserverKonstanten.SYSTEM_COLUMN_NAMES, MserverDaten.system);
                        }
                        if (parser.getLocalName().equals(MserverSearchTask.SUCHEN)) {
                            MserverSearchTask cron = new MserverSearchTask();
                            get(parser, event, MserverSearchTask.SUCHEN, MserverSearchTask.SUCHEN_COLUMN_NAMES, cron.arr);
                            MserverDaten.listeSuchen.add(cron);
                        }
                        if (parser.getLocalName().equals(MserverDatenUpload.UPLOAD)) {
                            MserverDatenUpload upload = new MserverDatenUpload();
                            get(parser, event, MserverDatenUpload.UPLOAD, MserverDatenUpload.UPLOAD_COLUMN_NAMES, upload.arr);
                            MserverDaten.listeUpload.add(upload);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(909078531, MserverXmlLesen.class.getName(), "xmlDatenLesen", ex);
        } finally {
            performancePoint.collect();
        }
    }

    private static boolean get(XMLStreamReader parser, int event, String xmlElem, String[] xmlNames, String[] strRet) {
        return get(parser, event, xmlElem, xmlNames, strRet, true);
    }

    private static boolean get(XMLStreamReader parser, int event, String xmlElem, String[] xmlNames, String[] strRet, boolean log) {
        EtmPoint performancePoint = etmMonitor.createPoint("MserverXmlLesen:get");
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
                MserverLog.fehlerMeldung(201456980, MserverXmlLesen.class.getName(), "get", ex);
            }
        }
        performancePoint.collect();
        return ret;
    }
}
