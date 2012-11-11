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
package mediathekServer.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import mediathek.controller.filmeLaden.importieren.DatenUrlFilmliste;
import mediathek.controller.filmeLaden.importieren.FilmlistenServer;
import mediathek.controller.filmeLaden.importieren.ListeUrlFilmlisten;
import mediathek.tool.Konstanten;
import mediathek.tool.Log;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Log;
import mediathekServer.tool.MS_XmlSchreiben;

public class MS_ListeFilmlisten {

    private static XMLOutputFactory outFactory;
    private static XMLStreamWriter writer;
    private static OutputStreamWriter out = null;
    private static ListeUrlFilmlisten listeFilmUpdateServer = new ListeUrlFilmlisten();

    public static File filmlisteEintragen(String urlDatei, DatenUrlFilmliste input) {
        // erst mal die Liste holen
        try {
            FilmlistenServer.getFilmlisten(urlDatei, listeFilmUpdateServer, MS_Daten.getUserAgent());
        } catch (Exception ex) {
            Log.fehlerMeldung(347895642, "FilmUpdateServer.suchen: " + urlDatei, ex);
        }
        // Einträge mit der URL löschen und dann "input" eintragen
        // gibt immer nur einen Eintrag mit einer URL
        Iterator<DatenUrlFilmliste> it = listeFilmUpdateServer.iterator();
        while (it.hasNext()) {
            DatenUrlFilmliste d = it.next();
            if (d.arr[FilmlistenServer.FILM_UPDATE_SERVER_URL_NR].equals(input.arr[FilmlistenServer.FILM_UPDATE_SERVER_URL_NR])) {
                it.remove();
            }
        }
        listeFilmUpdateServer.add(input);
        // Liste in Datei schreiben
        return ListeFilmlistenSchreiben();
    }
//<Mediathek>
//<Server>
//<Download_Filme_1>http://176.28.14.91/mediathek1/Mediathek_10.bz2</Download_Filme_1>
//<Datum>07.11.2012</Datum>
//<Anzahl/>
//<Zeit>10:45:06</Zeit>
//</Server>    
//<Mediathek>
    private static final String TAG_LISTE = "Mediathek";
    private static final String TAG_SERVER = "Server";
    private static final String TAG_SERVER_URL_PRIO_1 = "Download_Filme_1";
    private static final String TAG_SERVER_DATUM = "Datum";
    private static final String TAG_SERVER_ZEIT = "Zeit";

    private static File ListeFilmlistenSchreiben() {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("mediathek", null);
            tmpFile.deleteOnExit();
            outFactory = XMLOutputFactory.newInstance();
            out = new OutputStreamWriter(new FileOutputStream(tmpFile), Konstanten.KODIERUNG_UTF);
            writer = outFactory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");//neue Zeile
            writer.writeStartElement(TAG_LISTE);
            writer.writeCharacters("\n");//neue Zeile
            Iterator<DatenUrlFilmliste> it = listeFilmUpdateServer.iterator();
            while (it.hasNext()) {
                DatenUrlFilmliste d = it.next();
                writer.writeStartElement(TAG_SERVER);
                writer.writeCharacters("\n");
                // Tags schreiben: URL
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_URL_PRIO_1);
                writer.writeCharacters(d.arr[FilmlistenServer.FILM_UPDATE_SERVER_URL_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                // Tags schreiben: Datum
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_DATUM);
                writer.writeCharacters(d.arr[FilmlistenServer.FILM_UPDATE_SERVER_DATUM_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                // Tags schreiben: Zeit
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_ZEIT);
                writer.writeCharacters(d.arr[FilmlistenServer.FILM_UPDATE_SERVER_ZEIT_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
            // Schließen
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            MS_Log.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(645421039, MS_XmlSchreiben.class.getName(), "xmlDatenSchreiben", ex);
        }
        return tmpFile;
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
