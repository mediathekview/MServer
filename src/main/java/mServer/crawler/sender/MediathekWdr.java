/*
 *    MediathekView
 *    Copyright (C) 2008 - 2012     W. Xaver
 *                              &   thausherr
 * 
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import java.io.IOException;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.wdr.WdrSendungOverviewDeserializer;
import mServer.crawler.sender.wdr.WdrSendungOverviewDto;
import mServer.crawler.sender.wdr.WdrVideoDetailsDeserializer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MediathekWdr extends MediathekReader {

    public final static String SENDERNAME = Const.WDR;
    private final static String ROCKPALAST_URL = "http://www1.wdr.de/fernsehen/rockpalast/startseite/index.html";
    private final static String ROCKPALAST_FESTIVAL = "http://www1.wdr.de/fernsehen/rockpalast/events/index.html";
    private final static String MAUS = "http://www.wdrmaus.de/lachgeschichten/spots.php5";
    
    private final ArrayList<String> listeFestival = new ArrayList<>();
    private final ArrayList<String> listeRochpalast = new ArrayList<>();
    private final ArrayList<String> listeMaus = new ArrayList<>();
    private final LinkedList<String> listeTage = new LinkedList<>();
    private MSStringBuilder seite_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite_2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekWdr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 100, startPrio);
    }

    //===================================
    // public
    //===================================
    @Override
    public synchronized void addToList() {
        //Theman suchen
        listeThemen.clear();
        listeTage.clear();
        listeFestival.clear();
        listeRochpalast.clear();
        listeMaus.clear();
        meldungStart();
        addToList__();

//        addTage();
        if (CrawlerTool.loadLongMax()) {
            maus();
            rockpalast();
            festival();
            // damit sie auch gestartet werden (im idealfall in unterschiedlichen Threads
            String[] add = new String[]{ROCKPALAST_URL, "Rockpalast"};
            listeThemen.addUrl(add);
            add = new String[]{ROCKPALAST_FESTIVAL, "Rockpalast"};
            listeThemen.addUrl(add);
            add = new String[]{MAUS, "Maus"};
            listeThemen.addUrl(add);
        }

        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty() && listeTage.isEmpty() && listeFestival.isEmpty() && listeRochpalast.isEmpty() && listeMaus.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeTage.size() + listeFestival.size() + listeRochpalast.size() + listeMaus.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    //===================================
    // private
    //===================================
    private void rockpalast() {
        try {
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite_1 = getUrlIo.getUri(SENDERNAME, ROCKPALAST_URL, StandardCharsets.UTF_8, 3 /* versuche */, seite_1, "");
            seite_1.extractList("", "", "<a href=\"/mediathek/video", "\"", "http://www1.wdr.de/mediathek/video/", listeRochpalast);
        } catch (Exception ex) {
            Log.errorLog(915423698, ex);
        }
    }

    private void maus() {
        // http://www.wdrmaus.de/lachgeschichten/mausspots/achterbahn.php5
        final String ROOTADR = "http://www.wdrmaus.de/lachgeschichten/";
        final String ITEM_1 = "<li class=\"filmvorschau\"><a href=\"../lachgeschichten/";
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri(SENDERNAME, MAUS, StandardCharsets.UTF_8, 3 /* versuche */, seite_1, "");
        try {
            seite_1.extractList("", "", ITEM_1, "\"", ROOTADR, listeMaus);
        } catch (Exception ex) {
            Log.errorLog(975456987, ex);
        }
    }

    private void festival() {
        // http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/rockpalastvideos_festivals100.html
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri(SENDERNAME, ROCKPALAST_FESTIVAL, StandardCharsets.UTF_8, 3 /* versuche */, seite_1, "");
        try {
            seite_1.extractList("", "", "<a href=\"/fernsehen/rockpalast/events/", "\"", "http://www1.wdr.de/fernsehen/rockpalast/events/", listeFestival);
        } catch (Exception ex) {
            Log.errorLog(432365698, ex);
        }
    }

    private void addTage() {
        // Sendung verpasst, da sind einige die nicht in einer "Sendung" enthalten sind
        // URLs nach dem Muster bauen:
        // http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-27022016.html
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String tag;
        for (int i = 1; i < 14; ++i) {
            final String URL = "http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-";
            tag = formatter.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            String urlString = URL + tag + ".html";
            listeTage.add(urlString);
        }
    }

    private void addToList__() {
        // http://www1.wdr.de/mediathek/video/sendungen/abisz-b100.html
        //Theman suchen
        final String URL = "http://www1.wdr.de/mediathek/video/sendungen-a-z/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen-a-z/";
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri_Iso(SENDERNAME, URL, seite_1, "");
        int pos1;
        int pos2;
        String url;
        themenSeitenSuchen(URL); // ist die erste Seite: "a"
        pos1 = seite_1.indexOf("<strong>A</strong>");
        while (!Config.getStop() && (pos1 = seite_1.indexOf(MUSTER_URL, pos1)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = seite_1.indexOf("\"", pos1)) != -1) {
                url = seite_1.substring(pos1, pos2);
                if (url.equals("index.html")) {
                    continue;
                }
                if (url.isEmpty()) {
                    Log.errorLog(995122047, "keine URL");
                } else {
                    url = "http://www1.wdr.de/mediathek/video/sendungen-a-z/" + url;
                    themenSeitenSuchen(url);
                }
            }
        }
    }

    private void themenSeitenSuchen(String strUrlFeed) {
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
        int pos1 = 0;
        int pos2;
        String url;
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_2 = getUrlIo.getUri_Iso(SENDERNAME, strUrlFeed, seite_2, "");
        meldung(strUrlFeed);
        while (!Config.getStop() && (pos1 = seite_2.indexOf(MUSTER_URL, pos1)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = seite_2.indexOf("\"", pos1)) != -1) {
                url = seite_2.substring(pos1, pos2).trim();
                if (!url.isEmpty()) {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;
                    //weiter gehts
                    String[] add;
                    add = new String[]{url, ""};
                    listeThemen.addUrl(add);
                }
            } else {
                Log.errorLog(375862100, "keine Url" + strUrlFeed);
            }
        }
    }

    private class ThemaLaden extends Thread {

        //private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder sendungsSeite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder sendungsSeite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder sendungsSeite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder sendungsSeite4 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder m3u8Page = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> liste_1 = new ArrayList<>();
        private final ArrayList<String> liste_2 = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    if (null != link[0]) {
                        switch (link[0]) {
                            case ROCKPALAST_URL:
                                themenSeiteRockpalast();
                                break;
                            case ROCKPALAST_FESTIVAL:
                                themenSeiteFestival();
                                break;
                            case MAUS:
                                addFilmeMaus();
                                break;
                            default:
                                sendungsSeitenSuchen1(link[0] /* url */);
                                break;
                        }
                    }
                    meldungProgress(link[0]);
                }
                String url;
                // TODO
                while (!Config.getStop() && (url = getListeTage()) != null) {
                    meldungProgress(url);
                    sendungsSeitenSuchen2(url, SENDERNAME, false);
                }

            } catch (Exception ex) {
                Log.errorLog(633250489, ex);
            }
            meldungThreadUndFertig();
        }

        private void sendungsSeitenSuchen1(String strUrl) {
            meldung(strUrl);
            // Sendungen auf der Seite
            liste_1.clear();
            liste_1.add(strUrl);
            if (CrawlerTool.loadLongMax()) {
                // sonst wars das
                final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, strUrl, sendungsSeite1, "");
                sendungsSeite1.extractList("<ul class=\"pageCounterNavi\">", "</ul>", "<a href=\"/mediathek/video/sendungen/", "\"",
                        "http://www1.wdr.de/mediathek/video/sendungen/", liste_1);
            }
            for (String u : liste_1) {
                if (Config.getStop()) {
                    break;
                }
                
                sendungsSeitenSuchen2(u, "", false);
            }
        }

        private void sendungsSeitenSuchen2(String strUrl, String parentTheme, boolean recursive) {
            if(!isUrlRelevant(strUrl)) {
                return;
            }
            
            try {
                Document filmDocument = Jsoup.connect(strUrl).get();
                WdrSendungOverviewDto dto = overviewDeserializer.deserialize("http://www1.wdr.de", filmDocument);
                
                // das ermittelte Thema nicht verwenden, wenn es sich um 
                // einen Aufruf innerhalb einer Rekursion handelt, denn dann
                // muss das initial ermittelte Thema verwendet werden
                final String theme;
                if(parentTheme.isEmpty()) {
                    theme = dto.getTheme();
                } else {
                    theme = parentTheme;
                }
                
                dto.getUrls().forEach(url -> {
                    if (!Config.getStop()) {
                        if(isUrlRelevant(url)) {
                            addFilm1(theme, url, recursive);
                        }
                    }
                });
            } catch(IOException ex) {
                Log.errorLog(763299001, ex);
            }
            
        }
        private WdrSendungOverviewDeserializer overviewDeserializer;

        public ThemaLaden() {
            overviewDeserializer = new WdrSendungOverviewDeserializer();
        }
        
        /***
         * Filtert URLs heraus, die nicht durchsucht werden sollen
         * Hintergrund: diese URLs verweisen auf andere und führen bei der Suche
         * im Rahmen der Rekursion zu endlosen Suchen
         * @param url zu prüfende URL
         * @return true, wenn die URL verarbeitet werden soll, sonst false
         */
        private boolean isUrlRelevant(String url) {
            // die Indexseite der Lokalzeit herausfiltern, da alle Beiträge
            // um die Lokalzeitenseiten der entsprechenden Regionen gefunden werden
            if(url.endsWith("lokalzeit/index.html")) {
                return false;
            } else if(url.contains("wdr.de/hilfe")) {
                return false;
            }
            
            return true;
        }
        
       /* private void sendungsSeitenSuchen2(String strUrl, String th) {
            final String MUSTER_URL = "<div class=\"teaser hideTeasertext\">";
            int pos;
            String url;
            String titel;
            String dauer;
            String datum = "";
            String thema;
            long duration = 0;

            if (strUrl.startsWith("http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/uebersicht-lokalzeiten100_tag")) {
                // brauchts nicht
                return;
            }
            final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            sendungsSeite2 = getUrl.getUri_Utf(SENDERNAME, strUrl, sendungsSeite2, "");
            if (sendungsSeite2.length() == 0) {
                return;
            }
            meldung(strUrl);

            if (!th.isEmpty()) {
                thema = th;
            } else {
                thema = parseThema(sendungsSeite2);
            }

            //Lokalzeit, ..
            String u = sendungsSeite2.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
            if (!u.isEmpty()) {
                sendungsSeitenSuchenNeu(strUrl, sendungsSeite2, thema);
            }

            //Lokalzeit
            // <div class="teaser video">
            pos = 0;
            while (!Config.getStop() && (pos = sendungsSeite2.indexOf("<div class=\"teaser video\">", pos)) != -1) {
                pos += MUSTER_URL.length();
                url = sendungsSeite2.extract("<a href=\"/mediathek/video/sendungen/", "\"", pos);
                if (!url.isEmpty()) {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;

                    titel = sendungsSeite2.extract("</span>", "<", pos).trim();
                    titel = titel.replace("\n", "");

//                    datum = sendungsSeite2.extract("<p class=\"subtitle\">", "|", pos).trim();
//                    if (datum.length() != 10) {
//                        datum = "";
//                    }
                    dauer = sendungsSeite2.extract("<span class=\"hidden\">L&auml;nge: </span>", "<", pos).trim();
                    try {
                        if (!dauer.isEmpty()) {
                            String[] parts = dauer.split(":");
                            duration = 0;
                            long power = 1;
                            for (int i = parts.length - 1; i >= 0; i--) {
                                duration += Long.parseLong(parts[i]) * power;
                                power *= 60;
                            }
                        }
                    } catch (Exception ex) {
                        Log.errorLog(915263654, ex, strUrl);
                    }

                    //weiter gehts
                    addFilm1(thema, titel, url, duration, datum);
                } else {
                    Log.errorLog(731201247, "keine Url" + strUrl);
                }
            }

            pos = 0;
            while (!Config.getStop() && (pos = sendungsSeite2.indexOf(MUSTER_URL, pos)) != -1) {
                pos += MUSTER_URL.length();
                url = sendungsSeite2.extract("<a href=\"/mediathek/video/sendungen/", "\"", pos);
                if (!url.isEmpty()) {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;

                    titel = sendungsSeite2.extract("<span class=\"hidden\">Video:</span>", "<", pos).trim();
                    titel = titel.replace("\n", "");

//                    datum = sendungsSeite2.extract("<p class=\"programInfo\">", "|", pos).trim();
//                    if (datum.length() != 8) {
//                        datum = "";
//                    }
                    dauer = sendungsSeite2.extract("<span class=\"hidden\">L&auml;nge: </span>", "<", pos).trim();
                    try {
                        if (!dauer.isEmpty()) {
                            String[] parts = dauer.split(":");
                            duration = 0;
                            long power = 1;
                            for (int i = parts.length - 1; i >= 0; i--) {
                                duration += Long.parseLong(parts[i]) * power;
                                power *= 60;
                            }
                        }
                    } catch (Exception ex) {
                        Log.errorLog(306597519, ex, strUrl);
                    }

                    //weiter gehts
                    addFilm1(thema, titel, url, duration, datum);
                } else {
                    Log.errorLog(646432970, "keine Url" + strUrl);
                }
            }
        }*/

        private void sendungsSeitenSuchenNeu(String strUrl, MSStringBuilder seite, String thema) {
            //Lokalzeit, ..
            String u = seite.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
            if (!u.isEmpty()) {
//                addFilm2(strUrl, thema, "", u, 0, "", "");
            }

            liste_2.clear();
            seite.extractList("Letzte Sendungen", "Neuer Abschnitt", "<a href=\"", "\"", "http://www1.wdr.de", liste_2);
            for (String ur : liste_2) {
                if (Config.getStop()) {
                    break;
                }

                final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite = getUrl.getUri_Utf(SENDERNAME, ur, seite, "");
                if (seite.length() == 0) {
                    continue;
                }
                meldung(strUrl);

                thema = parseThema(seite);

                u = seite.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
                if (!u.isEmpty()) {
//                    addFilm2(strUrl, thema, "", u, 0, "", "");
                }
            }
        }

        private void addFilm1(String theme, String filmWebsite, boolean recursive) {
            meldung(filmWebsite);
            try {
                Document filmDocument = Jsoup.connect(filmWebsite).get();
                WdrVideoDetailsDeserializer deserializer = new WdrVideoDetailsDeserializer();
                DatenFilm film = deserializer.deserialize(theme, filmDocument);
                
                if (film != null) {
                    addFilm(film);
                } else if (!recursive && CrawlerTool.loadLongMax()){
                    // bei langer Suche eine Rekursionstufe durchführen, damit 
                    // weitere Beiträge (z.B. Lokalzeit) gefunden werden
                    sendungsSeitenSuchen2(filmWebsite, theme, true);
                }
            } catch(IOException ex) {
                Log.errorLog(763299001, ex);
            }
        }
            /*
        private void addFilm1(String thema, String titel, String filmWebsite, long dauer, String datum) {
            meldung(filmWebsite);
            final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            sendungsSeite3 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite3, "");
            if (sendungsSeite3.length() == 0) {
                return;
            }
            if (sendungsSeite3.length() == 0) {
                Log.errorLog(751236547, new String[]{"leere Seite: " + filmWebsite});
            }
            String description = sendungsSeite3.extract("<p class=\"text\">", "<");
            if (thema.isEmpty()) {
                thema = sendungsSeite3.extract("{ 'offset': '0' }}\" title=\"", "\"");
                thema = thema.replace(", WDR", "");
                if (thema.contains(":")) {
                    thema = thema.substring(0, thema.indexOf(':'));
                }
                if (thema.contains(" -")) {
                    thema = thema.substring(0, thema.indexOf(" -"));
                }
            }
            // URL suchen
            String url = sendungsSeite3.extract(URL_PATTERN_START, URL_PATTERN_END);
            if (!url.isEmpty()) {
                addFilm2(filmWebsite, thema, titel, url, dauer, datum, description);
            } else {
                Logger.getLogger("x").log(Level.SEVERE, filmWebsite);
                Log.errorLog(763299001, new String[]{"keine Url: " + filmWebsite});
            }
        }

        private void addFilm2(String filmWebsite, String thema, String titel, String urlFilmSuchen, long dauer, String datum, String beschreibung) {
            final String INDEX_0 = "index_0_av.m3u8"; //kleiner
            final String INDEX_1 = "index_1_av.m3u8"; //klein
            final String INDEX_2 = "index_2_av.m3u8"; //hohe Auflösung
            meldung(urlFilmSuchen);
            final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            sendungsSeite4 = getUrl.getUri_Utf(SENDERNAME, urlFilmSuchen, sendungsSeite4, "");
            if (sendungsSeite4.length() == 0) {
                return;
            }
            String urlNorm, urlHd = "", urlKlein = "";
            String zeit = "";

            // URL suchen
            urlNorm = sendungsSeite4.extract("\"alt\":{\"videoURL\":\"", "\"");
            String f4m = sendungsSeite4.extract("\"dflt\":{\"videoURL\":\"", "\"");

            // Fehlendes Protokoll ergänzen, wenn es fehlt. kommt teilweise vor.
            String protocol = urlFilmSuchen.substring(0, urlFilmSuchen.indexOf(':'));
            urlNorm = addProtocolIfMissing(urlNorm, protocol);
            f4m = addProtocolIfMissing(f4m, protocol);
            
            if (urlNorm.endsWith(".m3u8")) {
                final String urlM3 = urlNorm;
                m3u8Page = getUrl.getUri_Utf(SENDERNAME, urlNorm, m3u8Page, "");
                if (m3u8Page.indexOf(INDEX_2) != -1) {
                    urlNorm = getUrlFromM3u8(urlM3, INDEX_2);
                } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                    urlNorm = getUrlFromM3u8(urlM3, INDEX_1);
                }
                if (m3u8Page.indexOf(INDEX_0) != -1) {
                    urlKlein = getUrlFromM3u8(urlM3, INDEX_0);
                } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                    urlKlein = getUrlFromM3u8(urlM3, INDEX_1);
                }

                if (urlNorm.isEmpty() && !urlKlein.isEmpty()) {
                    urlNorm = urlKlein;
                }
                if (urlNorm.equals(urlKlein)) {
                    urlKlein = "";
                }
            }

            if (!f4m.isEmpty() && urlNorm.contains("_") && urlNorm.endsWith(".mp4")) {
                // http://adaptiv.wdr.de/z/medp/ww/fsk0/104/1048369/,1048369_11885064,1048369_11885062,1048369_11885066,.mp4.csmil/manifest.f4m
                // http://ondemand-ww.wdr.de/medp/fsk0/104/1048369/1048369_11885062.mp4
                String s1 = urlNorm.substring(urlNorm.lastIndexOf('_') + 1, urlNorm.indexOf(".mp4"));
                String s2 = urlNorm.substring(0, urlNorm.lastIndexOf('_') + 1);
                try {
                    int nr = Integer.parseInt(s1);
                    if (f4m.contains(nr + 2 + "")) {
                        urlHd = s2 + (nr + 2) + ".mp4";
                    }
                    if (f4m.contains(nr + 4 + "")) {
                        urlKlein = s2 + (nr + 4) + ".mp4";
                    }
                } catch (Exception ignore) {
                }
                if (!urlHd.isEmpty()) {
                    if (urlKlein.isEmpty()) {
                        urlKlein = urlNorm;
                    }
                    urlNorm = urlHd;
                }
            }

            if (titel.isEmpty()) {
                titel = sendungsSeite4.extract("\"trackerClipTitle\":\"", "\",");
                if (titel.contains("\"")) {
                    DbgMsg.print("WDR: " + urlFilmSuchen);
                }
                titel = titel.replace("\\\"", "\"");
            }

            String subtitle = sendungsSeite4.extract("\"captionURL\":\"", "\"");

            if (datum.isEmpty()) {
                String d = sendungsSeite4.extract("\"trackerClipAirTime\":\"", "\"");
                if (d.contains(" ")) {
                    zeit = d.substring(d.indexOf(' ')) + ":00";
                    datum = d.substring(0, d.indexOf(' '));
                }
            } else {
                String d = sendungsSeite4.extract("\"trackerClipAirTime\":\"", "\"");
                if (d.contains(" ")) {
                    zeit = d.substring(d.indexOf(' ')) + ":00";
                } else {
                    System.out.println("Zeit");
                }
            }

            if (!urlNorm.isEmpty()) {
                if (thema.endsWith(SENDERNAME)) {
                    // dann nur wenn die URL noch nicht enthalten
                    DatenFilm f = mlibFilmeSuchen.listeFilmeNeu.getFilmByUrl(urlNorm);
                    if (f != null) {
                        return;
                    }
                }
                DatenFilm film = new DatenFilm(SENDERNAME, thema, filmWebsite, titel, urlNorm, "", datum, zeit,
                        dauer, beschreibung);
                if (!subtitle.isEmpty()) {
                    CrawlerTool.addUrlSubtitle(film, subtitle);
                }
                if (!urlKlein.isEmpty()) {
                    CrawlerTool.addUrlKlein(film, urlKlein, "");
                }
                addFilm(film);
            } else {
                Log.errorLog(978451239, new String[]{"keine Url: " + urlFilmSuchen, "UrlThema: " + filmWebsite});
            }
        }*/
        
        private String addProtocolIfMissing(String url, String protocol) {
            if(url.startsWith("//")) {
                return protocol + ":" + url;
            } else if(url.startsWith("://")) {
                return protocol + url;
            }
            
            return url;
        }
        
        private String parseThema(MSStringBuilder seite) {
            String thema = seite.extract("<title>", "<");
            thema = thema.replace("- Sendung - Video - Mediathek - WDR", "")
                    .replace(" - Sendungen A-Z - Video - Mediathek - WDR", "").trim();
            if(thema.startsWith("Video:")) {
                thema = thema.substring(6).trim();
            }
            if (thema.startsWith("Unser Sendungsarchiv")) {
                thema = "";
            }
                
            return thema;
        }

        private String getUrlFromM3u8(String m3u8Url, String qualityIndex) {
            final String CSMIL = "csmil/";
            return m3u8Url.substring(0, m3u8Url.indexOf(CSMIL)) + CSMIL + qualityIndex;
        }

        private void themenSeiteRockpalast() {
            try {
                for (String urlRock : listeRochpalast) {
                    meldungProgress(urlRock);
                    if (Config.getStop()) {
                        break;
                    }
                    // Konzerte suchen
                    final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                    sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, urlRock, sendungsSeite1, "");
                    String u = sendungsSeite1.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
                    if (!u.isEmpty()) {
//                        addFilm2(urlRock, "Rockpalast", "", u, 0, "", "");
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(696963025, ex);
            }
        }

        private void themenSeiteFestival() {
            try {
                for (String urlRock : listeFestival) {
                    meldungProgress(urlRock);
                    if (Config.getStop()) {
                        break;
                    }
                    // Konzerte suchen
                    final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                    sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, urlRock, sendungsSeite1, "");
                    String u = sendungsSeite1.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
                    if (!u.isEmpty()) {
//                        addFilm2(urlRock, "Rockpalast - Festival", "", u, 0, "", "");
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(915263698, ex);
            }
        }

        private void addFilmeMaus() {
            try {
                for (String filmWebsite : listeMaus) {
                    meldungProgress(filmWebsite);
                    if (Config.getStop()) {
                        break;
                    }
                    final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                    sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite1, "");

                    String titel = sendungsSeite1.extract("<title>", "<"); //<title>Achterbahn - MausSpots - Lachgeschichten - Die Seite mit der Maus - WDR Fernsehen</title>
                    titel = titel.replace("\n", "");
                    if (titel.contains("-")) {
                        titel = titel.substring(0, titel.indexOf('-')).trim();
                    }
                    String jsUrl = sendungsSeite1.extract("'mediaObj': { 'url': '", "'");
//                    addFilm2(filmWebsite, "MausSpots", titel, jsUrl, 0, "", "");
                }
            } catch (Exception ex) {
                Log.errorLog(915263698, ex);
            }
        }

    }

    private synchronized String getListeTage() {
        return listeTage.pollFirst();
    }

}
