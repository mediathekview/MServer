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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import mServer.daten.MServerDatenSuchen;
import mServer.daten.MServerDatenUpload;
import msearch.tool.MSearchConst;

public class MServerTest {

    private static XMLOutputFactory outFactory;
    private static XMLStreamWriter writer;
    private static OutputStreamWriter out = null;

    public static void schreiben(String datei) {
        try {
            MServerLog.systemMeldung("Daten Schreiben");
            File file = new File(datei);
            MServerLog.systemMeldung("Start Schreiben nach: " + datei);
            outFactory = XMLOutputFactory.newInstance();
            out = new OutputStreamWriter(new FileOutputStream(file), MSearchConst.KODIERUNG_UTF);
            writer = outFactory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");//neue Zeile
            writer.writeStartElement(MServerKonstanten.XML_START);
            writer.writeCharacters("\n");//neue Zeile
            // System schreibem
            xmlSchreibenDaten(MServerKonstanten.SYSTEM, MServerKonstanten.SYSTEM_COLUMN_NAMES, MServerDaten.system);
            xmlSchreibenDaten(MServerDatenSuchen.SUCHEN, MServerDatenSuchen.SUCHEN_COLUMN_NAMES, new MServerDatenSuchen().arr);
            xmlSchreibenDaten(MServerDatenUpload.UPLOAD, MServerDatenUpload.UPLOAD_COLUMN_NAMES, new MServerDatenUpload().arr);
            // Schließen
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            MServerLog.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MServerLog.fehlerMeldung(645421039, MServerXmlSchreiben.class.getName(), "xmlDatenSchreiben", ex);
        }
    }

    private static void xmlSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray) {
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
            MServerLog.fehlerMeldung(102365897, MServerLog.class.getName(), "xmlSchreibenDaten", ex);
        }
    }
}
