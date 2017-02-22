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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

public class MediathekArte_de extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.ARTE_DE;

    // "Freitag, 02. August um 12:41 Uhr"
    SimpleDateFormat sdfZeit = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat sdfDatum = new SimpleDateFormat("dd.MM.yyyy");
    String URL_ARTE = "http://www.arte.tv/papi/tvguide/epg/schedule/D/L3/";
    String URL_CONCERT = "http://concert.arte.tv/de/videos/all";
    String URL_CONCERT_NOT_CONTAIN = "-STF";
    String URL_ARTE_MEDIATHEK_1 = "http://www.arte.tv/guide/de/plus7/videos?day=-";
    String URL_ARTE_MEDIATHEK_2 = "&page=1&isLoading=true&sort=newest&country=DE";
    String TIME_1 = "<li>Sendetermine:</li>";
    String TIME_2 = "um";

    public MediathekArte_de(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 2, /* urlWarten */ 500, startPrio);
        getUrlIo.setTimeout(15000);
    }

    public MediathekArte_de(FilmeSuchen ssearch, int startPrio, String name) {
        super(ssearch, name,/* threads */ 2, /* urlWarten */ 500, startPrio);
        getUrlIo.setTimeout(15000);
    }

    //===================================
    // public
    //===================================
    @Override
    public void addToList() {
        meldungStart();
        addTage();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty()) {
            if (CrawlerTool.loadLongMax()) {
                addConcert();
            } else {
                meldungThreadUndFertig();
            }
        } else {
            if (CrawlerTool.loadLongMax()) {
                addConcert();
            }
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(getSendername() + t);
                th.start();
            }
        }
    }

    private void addConcert() {
        Thread th = new Thread(new ConcertLaden(0, 20));
        th.setName(getSendername() + "Concert-0");
        th.start();
        th = new Thread(new ConcertLaden(20, 40));
        th.setName(getSendername() + "Concert-1");
        th.start();
    }

    private void addTage() {
        // http://www.arte.tv/guide/de/plus7/videos?day=-2&page=1&isLoading=true&sort=newest&country=DE
        for (int i = 0; i <= 14; ++i) {
            String u = URL_ARTE_MEDIATHEK_1 + i + URL_ARTE_MEDIATHEK_2;
            listeThemen.add(new String[]{u});
        }
    }

    private class ConcertLaden implements Runnable {

        private final int start, anz;
        MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        public ConcertLaden(int start, int anz) {
            this.start = start;
            this.anz = anz;
        }

        @Override
        public void run() {
            try {
                meldungAddThread();
                addConcert(start, anz);
            } catch (Exception ex) {
                Log.errorLog(787452309, ex, "");
            }
            meldungThreadUndFertig();
        }

        private void addConcert(int start, int anz) {
            final String THEMA = "Concert";
            final String MUSTER_START = "<div class=\"header-article \">";
            String urlStart;
            meldungAddMax(anz);
            for (int i = start; !Config.getStop() && i < anz; ++i) {
                if (i > 0) {
                    urlStart = URL_CONCERT + "?page=" + i;
                } else {
                    urlStart = URL_CONCERT;
                }
                meldungProgress(urlStart);
                seite1 = getUrlIo.getUri_Utf(getSendername(), urlStart, seite1, "");
                int pos1 = 0;
                String url, urlWeb, titel, urlHd = "", urlLow = "", urlNormal = "", beschreibung, datum, dauer;
                while (!Config.getStop() && (pos1 = seite1.indexOf(MUSTER_START, pos1)) != -1) {
                    urlHd = "";
                    urlLow = "";
                    urlNormal = "";
                    pos1 += MUSTER_START.length();
                    try {
                        url = seite1.extract("<a href=\"", "\"", pos1);
                        titel = seite1.extract("title=\"", "\"", pos1);
                        datum = seite1.extract("<span class=\"date-container\">", "<", pos1).trim();
                        beschreibung = seite1.extract("property=\"content:encoded\">", "<", pos1);
                        dauer = seite1.extract("<span class=\"time-container\">", "<", pos1).trim();
                        dauer = dauer.replace("\"", "");
                        int duration = 0;
                        if (!dauer.equals("")) {
                            String[] parts = dauer.split(":");
                            duration = 0;
                            long power = 1;
                            for (int ii = parts.length - 1; ii >= 0; ii--) {
                                duration += Long.parseLong(parts[ii]) * power;
                                power *= 60;
                            }
                        }
                        if (url.equals("")) {
                            Log.errorLog(825241452, "keine URL");
                        } else {
                            urlWeb = "http://concert.arte.tv" + url;
                            meldung(urlWeb);
                            seite2 = getUrlIo.getUri_Utf(getSendername(), urlWeb, seite2, "");
                            // genre: <span class="tag tag-link"><a href="/de/videos/rockpop">rock/pop</a></span> 
                            String genre = seite2.extract("<span class=\"tag tag-link\">", "\">", "<");
                            if (!genre.isEmpty()) {
                                beschreibung = genre + "\n" + DatenFilm.cleanDescription(beschreibung, THEMA, titel);
                            }
                            url = seite2.extract("arte_vp_url=\"", "\"");
                            if (url.isEmpty()) {
                                Log.errorLog(784512698, "keine URL");
                            } else {
                                seite2 = getUrlIo.getUri_Utf(getSendername(), url, seite2, "");
                                int p1 = 0;
                                String a = "\"bitrate\":800";
                                String b = "\"url\":\"";
                                String c = "\"";
                                while ((p1 = seite2.indexOf(a, p1)) != -1) {
                                    p1 += a.length();
                                    urlLow = seite2.extract(b, c, p1).replace("\\", "");
                                    if (urlLow.endsWith(".m3u8")) {
                                        urlLow = "";
                                        continue;
                                    }
                                    if (!urlLow.contains(URL_CONCERT_NOT_CONTAIN)) {
                                        break;
                                    }
                                }
                                a = "\"bitrate\":1500";
                                p1 = 0;
                                while ((p1 = seite2.indexOf(a, p1)) != -1) {
                                    p1 += a.length();
                                    urlNormal = seite2.extract(b, c, p1).replace("\\", "");
                                    if (urlNormal.endsWith(".m3u8")) {
                                        urlNormal = "";
                                        continue;
                                    }
                                    if (!urlNormal.contains(URL_CONCERT_NOT_CONTAIN)) {
                                        break;
                                    }
                                }
                                a = "\"bitrate\":2200";
                                p1 = 0;
                                while ((p1 = seite2.indexOf(a, p1)) != -1) {
                                    p1 += a.length();
                                    urlHd = seite2.extract(b, c, p1).replace("\\", "");
                                    if (urlHd.endsWith(".m3u8")) {
                                        urlHd = "";
                                        continue;
                                    }
                                    if (!urlHd.contains(URL_CONCERT_NOT_CONTAIN)) {
                                        break;
                                    }
                                }

                                if (urlNormal.isEmpty()) {
                                    urlNormal = urlLow;
                                    urlLow = "";
                                    Log.errorLog(951236487, "keine URL");
                                }
                                if (urlNormal.isEmpty()) {
                                    Log.errorLog(989562301, "keine URL");
                                } else {
                                    DatenFilm film = new DatenFilm(getSendername(), THEMA, urlWeb, titel, urlNormal, "" /*urlRtmp*/,
                                            datum, "" /*zeit*/, duration, beschreibung);
                                    if (!urlHd.isEmpty()) {
                                        CrawlerTool.addUrlHd(film, urlHd, "");
                                    }
                                    if (!urlLow.isEmpty()) {
                                        CrawlerTool.addUrlKlein(film, urlLow, "");
                                    }
                                    addFilm(film);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.errorLog(465623121, ex);
                    }
                }
            }
        }
    }

    class ThemaLaden implements Runnable {

        GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private final MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
//        private final MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
//        private final MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> liste = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addThemen(link[0]);
                }
            } catch (Exception ex) {
                Log.errorLog(894330854, ex, "");
            }
            meldungThreadUndFertig();
        }

        private void addThemen(String startUrl) {
            // http://www.arte.tv/guide/de/plus7/videos?day=-2&page=1&isLoading=true&sort=newest&country=DE
            getUrl.getUri_Utf(getSendername(), startUrl, seite1, "");
            seite1.extractList("\"url\":\"http:\\/\\/www.arte.tv", "\"", liste);
            for (String s : liste) {
                if (Config.getStop()) {
                    break;
                }
                s = "http://www.arte.tv" + s.replace("\\", "");
                //Datum: url: xx-0 => heute, xx-1 => gestern, ...
                String date = "";
                try {
                    String d = startUrl.substring(startUrl.lastIndexOf("day=-") + "day=-".length(), startUrl.indexOf("&page="));
                    int iD = Integer.parseInt(d);
                    SimpleDateFormat form = new SimpleDateFormat("dd.MM.yyyy");
                    date = form.format(new Date(new Date().getTime() - iD * (1000 * 60 * 60 * 24)));
                } catch (Exception ignore) {
                }
                getFilm1(s, date);
            }
        }

        private void getFilm1(String filmWebsite, String date) {
            getUrl.getUri_Utf(getSendername(), filmWebsite, seite1, "");
            String title = seite1.extract("<h1 class=\"title\" itemprop=\"name\">", "<");
            String subtitle = seite1.extract("<h2 class=\"subtitle\">", "<");
            if (!subtitle.isEmpty() && !title.equals(subtitle)) {
                title = title + " - " + subtitle;
            }
            String thema = seite1.extract("<span class=\"video-meta-genre\">", "<");
            String description = seite1.extract("<p class=\"program-description-short\">", "<");
            String duration = seite1.extract("<span class=\"duration\">", "<");
            long dauer = 0;
            try {
                duration = duration.replace("min", "");
                duration = duration.replace("Min.", "");
                duration = duration.replace("\n", "").trim();
                dauer = Integer.parseInt(duration) * 60;
            } catch (Exception ignore) {
            }
            String time = seite1.extract(TIME_1, TIME_2, "<"); //Donnerstag, 15. Dezember um 23.30 Uhr
            time = time.replace("\n", "");
            time = time.replace("Uhr", "").trim();
            time = time.replace(".", ":");
            time = time.replace("h", ":");
            time = time + ":00";
            if (time.length() < 8) {
                time = "0" + time;
            }

            String fUrl = seite1.extract("src=\"http://www.arte.tv/player", "\"");

            if (!fUrl.isEmpty()) {
                fUrl = "http://www.arte.tv/player" + fUrl;
                meldung(fUrl);
                getFilm2(fUrl, filmWebsite, thema, title, description, dauer, date, time);
            }
        }

        private void getFilm2(String urlWeb, String filmWebsite, String thema, String title, String description, long dauer, String date, String time) {
            getUrl.getUri_Utf(getSendername(), urlWeb, seite1, "");
            String urlHd = seite1.extract("\"id\":\"HTTP_MP4_SQ_1\"", "\"url\":\"", "\"").replace("\\", "");
            String urlNorm = seite1.extract("\"id\":\"HTTP_MP4_EQ_1\"", "\"url\":\"", "\"").replace("\\", "");
            String urlKlein = seite1.extract("\"id\":\"HTTP_MP4_HQ_1\"", "\"url\":\"", "\"").replace("\\", "");

            if (urlNorm.isEmpty() && !urlKlein.isEmpty()) {
                urlNorm = urlKlein;
                urlKlein = "";
            }
            if (urlNorm.isEmpty() && !urlHd.isEmpty()) {
                urlNorm = urlHd;
                urlHd = "";
            }

            if (!urlNorm.isEmpty() && !urlNorm.endsWith("EXTRAIT.mp4")) {
                // http://artestras.vo.llnwxd.net/o35/nogeo/HBBTV/042975-013-B_EXT_SQ_1_VA_00604871_MP4-2200_AMM-HBBTV_EXTRAIT.mp4
                // sind nur Trailer
                DatenFilm film = new DatenFilm(getSendername(), thema, filmWebsite, title, urlNorm, "" /*urlRtmp*/,
                        date, time, dauer, description);
                if (!urlKlein.isEmpty()) {
                    CrawlerTool.addUrlKlein(film, urlKlein, "");
                }
                if (!urlHd.isEmpty()) {
                    CrawlerTool.addUrlHd(film, urlHd, "");
                }
                addFilm(film);
            } else {
                Log.errorLog(915263647, "Keine URL: " + filmWebsite);
            }
        }

    }

}
