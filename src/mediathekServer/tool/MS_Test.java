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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import mediathek.tool.Konstanten;
import mediathekServer.daten.MS_DatenSuchen;
import mediathekServer.daten.MS_DatenUpload;

public class MS_Test {

    private static XMLOutputFactory outFactory;
    private static XMLStreamWriter writer;
    private static OutputStreamWriter out = null;

    public static void schreiben(String datei) {
        try {
            MS_Log.systemMeldung("Daten Schreiben");
            File file = new File(datei);
            MS_Log.systemMeldung("Start Schreiben nach: " + datei);
            outFactory = XMLOutputFactory.newInstance();
            out = new OutputStreamWriter(new FileOutputStream(file), Konstanten.KODIERUNG_UTF);
            writer = outFactory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");//neue Zeile
            writer.writeStartElement(MS_Konstanten.XML_START);
            writer.writeCharacters("\n");//neue Zeile
            // System schreibem
            xmlSchreibenDaten(MS_Konstanten.SYSTEM, MS_Konstanten.SYSTEM_COLUMN_NAMES, MS_Daten.system);
            xmlSchreibenDaten(MS_Konstanten.SUCHEN, MS_Konstanten.SUCHEN_COLUMN_NAMES, new MS_DatenSuchen().arr);
            xmlSchreibenDaten(MS_Konstanten.UPLOAD, MS_Konstanten.UPLOAD_COLUMN_NAMES, new MS_DatenUpload().arr);
            // Schließen
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            MS_Log.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(645421039, MS_XmlSchreiben.class.getName(), "xmlDatenSchreiben", ex);
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
            MS_Log.fehlerMeldung(102365897, MS_Log.class.getName(), "xmlSchreibenDaten", ex);
        }
    }
}
