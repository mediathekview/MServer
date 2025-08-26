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
package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl;
import de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls;
import de.mediathekview.mlib.tool.Functions;
import de.mediathekview.mlib.tool.Log;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class FilmlistenSuchen {

    // damit werden die DownloadURLs zum Laden einer Filmliste gesucht
    // Liste mit den URLs zum Download der Filmliste
    public de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls listeFilmlistenUrls_akt = new de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls();
    public de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls listeFilmlistenUrls_diff = new de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls();
    private static boolean firstSearchAkt = true;
    private static boolean firstSearchDiff = true;
    private final int UPDATE_LISTE_MAX = 10; // die Downloadliste für die Filmlisten nur jeden 10. Programmstart aktualisieren

    public String suchenAkt(ArrayList<String> bereitsVersucht) {
        // passende URL zum Laden der Filmliste suchen
        String retUrl;
        if (listeFilmlistenUrls_akt.isEmpty()) {
            // bei leerer Liste immer aktualisieren
            updateURLsFilmlisten(true);
        } else if (firstSearchAkt) {
            // nach dem Programmstart wird die Liste einmal aktualisiert aber
            // da sich die Listen nicht ändern, nur jeden xx Start
            int nr = new Random().nextInt(UPDATE_LISTE_MAX);
            if (nr == 0) {
                updateURLsFilmlisten(true);
            }
        }
        firstSearchAkt = false;
        retUrl = (listeFilmlistenUrls_akt.getRand(bereitsVersucht)); //eine Zufällige Adresse wählen
        if (bereitsVersucht != null) {
            bereitsVersucht.add(retUrl);
        }
        return retUrl;
    }

    public String suchenDiff(ArrayList<String> bereitsVersucht) {
        // passende URL zum Laden der Filmliste suchen
        String retUrl;
        if (listeFilmlistenUrls_diff.isEmpty()) {
            // bei leerer Liste immer aktualisieren
            updateURLsFilmlisten(false);
        } else if (firstSearchDiff) {
            // nach dem Programmstart wird die Liste einmal aktualisiert aber
            // da sich die Listen nicht ändern, nur jeden xx Start
            int nr = new Random().nextInt(UPDATE_LISTE_MAX);
            if (nr == 0) {
                updateURLsFilmlisten(false);
            }
        }
        firstSearchDiff = false;
        retUrl = (listeFilmlistenUrls_diff.getRand(bereitsVersucht)); //eine Zufällige Adresse wählen
        if (bereitsVersucht != null) {
            bereitsVersucht.add(retUrl);
        }
        return retUrl;
    }

    /**
     * Add our default full list servers.
     */
    private void insertDefaultActiveServers()
    {
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://m.picn.de/f/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://m1.picn.de/f/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://m2.picn.de/f/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://download10.onlinetvrecorder.com/mediathekview/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://mediathekview.jankal.me/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://verteiler1.mediathekview.de/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://verteiler2.mediathekview.de/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
        listeFilmlistenUrls_akt.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://verteiler3.mediathekview.de/Filmliste-akt.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT));
    }

    /**
     * Add our default diff list servers.
     */
    private void insertDefaultDifferentialListServers()
    {
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://m.picn.de/f/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://m1.picn.de/f/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://m2.picn.de/f/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://download10.onlinetvrecorder.com/mediathekview/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://mediathekview.jankal.me/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://verteiler1.mediathekview.de/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://verteiler2.mediathekview.de/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
        listeFilmlistenUrls_diff.add(new de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl("http://verteiler3.mediathekview.de/Filmliste-diff.xz", de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF));
    }

    /**
     * Update the download server URLs.
     * @param updateFullList if true, update full list server, otherwise diff servers.
     **/
    public void updateURLsFilmlisten(final boolean updateFullList) {
        de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls tmp = new de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls();
        if (updateFullList) {
            getDownloadUrlsFilmlisten(Const.ADRESSE_FILMLISTEN_SERVER_AKT, tmp, Config.getUserAgent(), de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_AKT);
            if (!tmp.isEmpty()) {
                listeFilmlistenUrls_akt = tmp;
            } else if (listeFilmlistenUrls_akt.isEmpty()) {
                insertDefaultActiveServers();
            }
            listeFilmlistenUrls_akt.sort();
        } else {
            getDownloadUrlsFilmlisten(Const.ADRESSE_FILMLISTEN_SERVER_DIFF, tmp, Config.getUserAgent(), de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.SERVER_ART_DIFF);
            if (!tmp.isEmpty()) {
                listeFilmlistenUrls_diff = tmp;
            } else if (listeFilmlistenUrls_diff.isEmpty()) {
                insertDefaultDifferentialListServers();
            }
            listeFilmlistenUrls_diff.sort();
        }
        if (tmp.isEmpty()) {
            Log.errorLog(491203216, new String[]{"Es ist ein Fehler aufgetreten!",
                    "Es konnten keine Updateserver zum aktualisieren der Filme",
                    "gefunden werden."});
        }
    }

    public void getDownloadUrlsFilmlisten(String dateiUrl, de.mediathekview.mlib.filmlisten.ListeFilmlistenUrls listeFilmlistenUrls, String userAgent, String art) {
        //String[] ret = new String[]{""/* version */, ""/* release */, ""/* updateUrl */};
        try {
            int event;
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            XMLStreamReader parser;
            InputStreamReader inReader;
            if (Functions.istUrl(dateiUrl)) {
                // eine URL verarbeiten
                int timeout = 20000; //ms
                URLConnection conn;
                conn = new URL(dateiUrl).openConnection();
                conn.setRequestProperty("User-Agent", userAgent);
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                inReader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            } else {
                // eine Datei verarbeiten
                File f = new File(dateiUrl);
                if (!f.exists()) {
                    return;
                }
                inReader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
            }
            parser = inFactory.createXMLStreamReader(inReader);
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String parsername = parser.getLocalName();
                    if (parsername.equals("Server")) {
                        //wieder ein neuer Server, toll
                        parseServerEntry(parser, listeFilmlistenUrls, art);
                    }
                }
            }
        } catch (Exception ex) {
            Log.errorLog(821069874, ex, "Die URL-Filmlisten konnte nicht geladen werden: " + dateiUrl);
        }
    }

    /**
     * Parse the server XML file.
     * @param parser
     * @param listeFilmlistenUrls
     * @param art
     */
    private void parseServerEntry(XMLStreamReader parser, ListeFilmlistenUrls listeFilmlistenUrls, String art) {
        String serverUrl = "";
        String prio = "";
        int event;
        try {
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    //parsername = parser.getLocalName();
                    switch (parser.getLocalName()) {
                        case "URL":
                            serverUrl = parser.getElementText();
                            break;
                        case "Prio":
                            prio = parser.getElementText();
                            break;
                    }
                }
                if (event == XMLStreamConstants.END_ELEMENT) {
                    //parsername = parser.getLocalName();
                    if (parser.getLocalName().equals("Server")) {
                        if (!serverUrl.equals("")) {
                            //public DatenFilmUpdate(String url, String prio, String zeit, String datum, String anzahl) {
                            if (prio.equals("")) {
                                prio = de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1;
                            }
                            listeFilmlistenUrls.addWithCheck(new DatenFilmlisteUrl(serverUrl, prio, art));
                        }
                        break;
                    }
                }
            }
        } catch (XMLStreamException ignored) {
        }

    }

}
