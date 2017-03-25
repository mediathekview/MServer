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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import mServer.daten.MserverDatenUpload;
import mServer.daten.MserverSearchTask;

public class MserverXmlSchreiben {
    private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
    private static XMLOutputFactory outFactory;
    private static XMLStreamWriter writer;
    private static OutputStreamWriter out = null;

    public static void xmlMusterDatenSchreiben() {
        EtmPoint performancePoint = etmMonitor.createPoint("MserverXmlSchreiben:xmlMusterDatenSchreiben");
        try {
            String datei = MserverDaten.getKonfigDatei() + "_Muster";
            MserverLog.systemMeldung("Daten Schreiben");
            File file = new File(datei);
            MserverLog.systemMeldung("Start Schreiben nach: " + datei);
            outFactory = XMLOutputFactory.newInstance();
            out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer = outFactory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");//neue Zeile
            writer.writeStartElement(MserverKonstanten.XML_START);
            writer.writeCharacters("\n");//neue Zeile
            // System schreibem
            xmlSchreibenDaten(MserverKonstanten.SYSTEM, MserverKonstanten.SYSTEM_COLUMN_NAMES, MserverDaten.system);
            xmlSchreibenDaten(MserverSearchTask.SUCHEN, MserverSearchTask.SUCHEN_COLUMN_NAMES, new MserverSearchTask().arr);
            xmlSchreibenDaten(MserverDatenUpload.UPLOAD, MserverDatenUpload.UPLOAD_COLUMN_NAMES, new MserverDatenUpload().arr);
            // Schließen
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            MserverLog.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(645421039, MserverXmlSchreiben.class.getName(), "xmlDatenSchreiben", ex);
        }
        performancePoint.collect();
    }

    private static void xmlSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray) {
        EtmPoint performancePoint = etmMonitor.createPoint("MserverXmlSchreiben:xmlSchreibenDaten");
        int xmlMax = datenArray.length;
        try {
            writer.writeStartElement(xmlName);
            writer.writeCharacters("\n");
            for (int i = 0; i < xmlMax; ++i) {
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(xmlSpalten[i]);
                writer.writeCharacters(datenArray[i]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(102365897, MserverLog.class.getName(), "xmlSchreibenDaten", ex);
        }
        performancePoint.collect();
    }
}
