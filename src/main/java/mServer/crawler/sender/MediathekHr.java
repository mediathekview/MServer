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
package mServer.crawler.sender;

import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Functions;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MediathekHr extends MediathekReader {

    public final static String SENDERNAME = Const.HR;
    private MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder rubrikSeite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekHr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    /**
     *
     */
    @Override
    public void addToList() {
        meldungStart();
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri_Utf(SENDERNAME, "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp", seite, "");

        //TH 7.8.2012 Erst suchen nach Rubrik-URLs, die haben Thema
        bearbeiteRubrik(seite);
        bearbeiteTage(seite);

        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void bearbeiteTage(MSStringBuilder seite) {
        // loadPlayItems('http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_39004789&xsl=media2html5.xsl');

        final String TAGE_PREFIX = "http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_";
        final String TAGE_MUSTER = "http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_";
        ArrayList<String> erg = new ArrayList<>();
        seite.extractList("", "", TAGE_MUSTER, "&", TAGE_PREFIX, erg);
        for (String url : erg) {
            String[] add = new String[]{url, ""/*thema*/, "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp"/*filmsite*/};
            if (!istInListe(listeThemen, url, 0)) {
                listeThemen.add(add);
            }
        }
    }

    //TH 7.8.2012 Suchen in Seite von Rubrik-URL
    // z.B. http://www.hr-online.de/website/fernsehen/sendungen/index.jsp?rubrik=2254
    private void bearbeiteRubrik(MSStringBuilder seite) {
        final String RUBRIK_PREFIX = "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp?rubrik=";
        final String RUBRIK_MUSTER = "<option value=\"/website/fernsehen/sendungen/index.jsp?rubrik=";
        ArrayList<String> erg = new ArrayList<>();
        seite.extractList("", "", RUBRIK_MUSTER, "\"", RUBRIK_PREFIX, erg);
        for (String s : erg) {
            if (Config.getStop()) {
                break;
            }
            rubrik(s);
        }
    }

    private void rubrik(String rubrikUrl) {
        final String MUSTER = "/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_";
        final String MUSTER_TITEL = "<meta property=\"og:title\" content=\"";

        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        rubrikSeite = getUrlIo.getUri_Iso(SENDERNAME, rubrikUrl, rubrikSeite, "");
        String url, thema;

        // 1. Titel (= Thema) holen
        thema = rubrikSeite.extract(MUSTER_TITEL, "\""); // <meta property="og:title" content="Alle Wetter | Fernsehen | hr-online.de"/>
        if (thema.contains("|")) {
            thema = thema.substring(0, thema.indexOf('|')).trim();
        }

        // 2. suchen nach XML Liste       
        url = rubrikSeite.extract(MUSTER, "&");
        if (!url.isEmpty()) {
            url = "http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_" + url;
            String[] add = new String[]{url, thema, rubrikUrl};
            if (!istInListe(listeThemen, url, 0)) {
                listeThemen.add(add);
            }
        } else {
            Log.errorLog(653210697, "keine URL");
        }
    }

    private class ThemaLaden extends Thread {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        //private MVStringBuilder seite2 = new MVStringBuilder();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /*url*/);
                    seite.setLength(0);
                    addFilme(link[0]/*url*/, link[1]/*thema*/, link[2]/*filmsite*/);
                }
            } catch (Exception ex) {
                Log.errorLog(894330854, ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String xmlWebsite, String thema_, String filmSite) {
            final String MUSTER_ITEM_1 = "<videos>";

            final String MUSTER_TITEL = "<title>"; //<title>Sonnenziel Valencia</title>
            final String MUSTER_URL = "<url type=\"mp4\">"; // <url type="mp4">http://www.hr.gl-systemhaus.de/video/fs/servicereisen/2014_11/141114214510_service_re_44765.mp4</url>
            final String MUSTER_URL_LOW = "<url type=\"mp4-small\">";
            final String MUSTER_DATUM = "<date>"; //<date>14.11.2014 18:50</date>
            final String MUSTER_THEMA = "<author>"; //<author>service: reisen</author>

            final String MUSTER_DURATION = "<duration>"; // <duration>00:43:32</duration>
            final String MUSTER_DESCRIPTION = "<description>";
            final String END = "</";
            meldung(xmlWebsite);
            seite1 = getUrl.getUri_Iso(SENDERNAME, xmlWebsite, seite1, "");
            try {
                int posItem1 = 0;
                String url = "", url_low;
                String datum, zeit = "";
                String titel, thema;
                long duration = 0;
                String description;
                while (!Config.getStop() && (posItem1 = seite1.indexOf(MUSTER_ITEM_1, posItem1)) != -1) {
                    posItem1 += MUSTER_ITEM_1.length();

                    String d = seite1.extract(MUSTER_DURATION, END, posItem1);
                    try {
                        if (!d.equals("")) {
                            duration = 0;
                            String[] parts = d.split(":");
                            long power = 1;
                            for (int i = parts.length - 1; i >= 0; i--) {
                                duration += Long.parseLong(parts[i]) * power;
                                power *= 60;
                            }
                        }
                    } catch (Exception ex) {
                        Log.errorLog(708096931, "d: " + d);
                    }
                    description = seite1.extract(MUSTER_DESCRIPTION, END, posItem1);
                    datum = seite1.extract(MUSTER_DATUM, END, posItem1);
                    if (datum.contains(" ")) {
                        zeit = datum.substring(datum.indexOf(" ")).trim() + ":00";
                        datum = datum.substring(0, datum.indexOf(" "));
                    }
                    titel = seite1.extract(MUSTER_TITEL, END, posItem1);

                    thema = seite1.extract(MUSTER_THEMA, END, posItem1);
                    if (thema.isEmpty()) {
                        thema = thema_;
                    }
                    if (thema.isEmpty()) {
                        thema = titel;
                    }
                    url = seite1.extract(MUSTER_URL, END, posItem1);
                    url_low = seite1.extract(MUSTER_URL_LOW, END, posItem1);
                    if (url.equals(url_low)) {
                        url_low = "";
                    }
                    if (!url.isEmpty()) {
                        if (datum.isEmpty()) {
                            datum = getDate(url);
                        }
                        //DatenFilm film = new DatenFilm(nameSenderMReader, thema, strUrlFeed, titel, url, furl, datum, "");
                        DatenFilm film = new DatenFilm(SENDERNAME, thema, filmSite, titel, url, "", datum, zeit, duration, description);
                        if (!url_low.isEmpty()) {
                            CrawlerTool.addUrlKlein(film, url_low, "");
                        }
                        String subtitle = url.replace(".mp4", ".xml");
                        if (urlExists(subtitle)) {
                            CrawlerTool.addUrlSubtitle(film, subtitle);
                        }
                        addFilm(film);
                    } else {
                        Log.errorLog(649882036, "keine URL");
                    }
                }
                if (url.isEmpty()) {
                    Log.errorLog(761236458, "keine URL für: " + xmlWebsite);
                }
            } catch (Exception ex) {
                Log.errorLog(487774126, ex);
            }
        }

        private String getDate(String url) {
            String ret = "";
            try {
                String tmp = Functions.getDateiName(url);
                if (tmp.length() > 8) {
                    tmp = tmp.substring(0, 8);
                    SimpleDateFormat sdfIn = new SimpleDateFormat("yyyyMMdd");
                    Date filmDate = sdfIn.parse(tmp);
                    SimpleDateFormat sdfOut;
                    sdfOut = new SimpleDateFormat("dd.MM.yyyy");
                    ret = sdfOut.format(filmDate);
                }
            } catch (Exception ex) {
                ret = "";
                Log.errorLog(356408790, "kein Datum");
            }
            return ret;
        }
    }
}
