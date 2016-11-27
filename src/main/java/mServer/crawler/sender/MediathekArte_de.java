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
import java.util.Date;
import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
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
        } else if (listeThemen.size() == 0) {
            if (Config.loadLongMax()) {
                addConcert();
            } else {
                meldungThreadUndFertig();
            }
        } else {
            if (Config.loadLongMax()) {
                addConcert();
            }
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(sendername + t);
                th.start();
            }
        }
    }

    private void addConcert() {
        Thread th = new Thread(new ConcertLaden(0, 20));
        th.setName(sendername + "Concert-0");
        th.start();
        th = new Thread(new ConcertLaden(20, 40));
        th.setName(sendername + "Concert-1");
        th.start();
    }

    private void addTage() {
        // http://www.arte.tv/papi/tvguide/epg/schedule/D/L3/2013-08-04/2013-8-04.json
        Date d = new Date();
        String out1, out2, u;
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-M-dd");
        for (int i = 0; i <= 14; ++i) {
            out1 = formatter1.format(new Date(d.getTime() - i * (1000 * 60 * 60 * 24)));
            out2 = formatter2.format(new Date(d.getTime() - i * (1000 * 60 * 60 * 24)));
            u = URL_ARTE + out1 + "/" + out2 + ".json";
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
                seite1 = getUrlIo.getUri_Utf(sendername, urlStart, seite1, "");
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
                            seite2 = getUrlIo.getUri_Utf(sendername, urlWeb, seite2, "");
                            // genre: <span class="tag tag-link"><a href="/de/videos/rockpop">rock/pop</a></span> 
                            String genre = seite2.extract("<span class=\"tag tag-link\">", "\">", "<");
                            if (!genre.isEmpty()) {
                                beschreibung = genre + "\n" + DatenFilm.cleanDescription(beschreibung, THEMA, titel);
                            }
                            url = seite2.extract("arte_vp_url=\"", "\"");
                            if (url.isEmpty()) {
                                Log.errorLog(784512698, "keine URL");
                            } else {
                                seite2 = getUrlIo.getUri_Utf(sendername, url, seite2, "");
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
                                    DatenFilm film = new DatenFilm(sendername, THEMA, urlWeb, titel, urlNormal, "" /*urlRtmp*/,
                                            datum, "" /*zeit*/, duration, beschreibung);
                                    if (!urlHd.isEmpty()) {
                                        film.addUrlHd(urlHd, "");
                                    }
                                    if (!urlLow.isEmpty()) {
                                        film.addUrlKlein(urlLow, "");
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

        GetUrl getUrl = new GetUrl(wartenSeiteLaden);
        private final MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addTheman(seite1, seite2, link[0]);
                }
            } catch (Exception ex) {
                Log.errorLog(894330854, ex, "");
            }
            meldungThreadUndFertig();
        }
    }

    private void addTheman(MSStringBuilder seite1, MSStringBuilder seite2, String startUrl) {
        // Datum, Zeit: "BAD":"04/08/2013","BAT":"13:20"
        final String MUSTER_START = "{\"programId\":";
        final String MUSTER_URL_JSON = "\"videoStreamUrl\":\"";
        final String MUSTER_DATUM = "\"BAD\":\"";
        final String MUSTER_ZEIT = "\"BAT\":\"";
        final String MUSTER_TITEL = "\"TIT\":\"";
        final String MUSTER_THEMA = "\"GEN\":\"";
        String[] arr;
        seite1 = getUrlIo.getUri_Utf(sendername, startUrl, seite1, "");
        int posStart = 0, posStop;
        int pos1;
        int pos2;
        int pos;
        String urlJson;
        String datum;
        String zeit;
        String titel, thema;
        while ((posStart = seite1.indexOf(MUSTER_START, posStart)) != -1) {
            posStart += MUSTER_START.length();
            posStop = seite1.indexOf(MUSTER_START, posStart);
            urlJson = "";
            datum = "";
            zeit = "";
            titel = "";
            thema = "";
            if ((pos1 = seite1.indexOf(MUSTER_URL_JSON, posStart)) != -1) {
                pos1 += MUSTER_URL_JSON.length();
                if (posStop == -1 || pos1 < posStop) {
                    if ((pos2 = seite1.indexOf("\"", pos1)) != -1) {
                        urlJson = seite1.substring(pos1, pos2);
                    }
                }
            }
            pos = posStart;
            while ((pos = seite1.indexOf(MUSTER_DATUM, pos)) != -1) {
                pos += MUSTER_DATUM.length();
                if (posStop != -1 && pos > posStop) {
                    break;
                }
                if (posStop == -1 || pos < posStop) {
                    if ((pos2 = seite1.indexOf("\"", pos)) != -1) {
                        datum = seite1.substring(pos, pos2);
                    }
                }
            }
            pos = posStart;
            while ((pos = seite1.indexOf(MUSTER_ZEIT, pos)) != -1) {
                pos += MUSTER_ZEIT.length();
                if (posStop != -1 && pos > posStop) {
                    break;
                }
                if (posStop == -1 || pos < posStop) {
                    if ((pos2 = seite1.indexOf("\"", pos)) != -1) {
                        zeit = seite1.substring(pos, pos2);
                    }
                }
            }
            if ((pos1 = seite1.indexOf(MUSTER_TITEL, posStart)) != -1) {
                pos1 += MUSTER_TITEL.length();
                if (posStop == -1 || pos1 < posStop) {
                    if ((pos2 = seite1.indexOf("\",", pos1)) != -1) {
                        titel = seite1.substring(pos1, pos2);
                        titel = titel.replace("\\", "");
                    }
                }
            }
            if ((pos1 = seite1.indexOf(MUSTER_THEMA, posStart)) != -1) {
                pos1 += MUSTER_THEMA.length();
                if (posStop == -1 || pos1 < posStop) {
                    if ((pos2 = seite1.indexOf("\"", pos1)) != -1) {
                        thema = seite1.substring(pos1, pos2);
                    }
                }
            }
            if (!urlJson.isEmpty()) {
                arr = new String[]{urlJson, datum, zeit, titel, thema};
                filmeLaden(seite2, arr);
            } else {
//                Log.fehlerMeldung(-956230147, Log.FEHLER_ART_MREADER, "MediathekArte_de.addThemen", "Keine URL: " + startUrl + "**" + count);
            }
        }
    }

    void filmeLaden(MSStringBuilder seite, String[] arr) {
        // url_hd url, url_klein
        //{"version":"VOF","versionProg":"1","VFO":"HBBTV","VQU":"SQ","VMT":"mp4","VUR":"http://artestras.vo.llnwxd.net/o35/nogeo/HBBTV/042975-013-B_EXT_SQ_2_VOF_00604879_MP4-2200_AMM-HBBTV_EXTRAIT.mp4"},
        //{"version":"VOF","versionProg":"1","VFO":"HBBTV","VQU":"EQ","VMT":"mp4","VUR":"http://artestras.vo.llnwxd.net/o35/nogeo/HBBTV/042975-013-B_EXT_EQ_2_VOF_00604878_MP4-1500_AMM-HBBTV_EXTRAIT.mp4"},
        //{"version":"VOF","versionProg":"1","VFO":"HBBTV","VQU":"HQ","VMT":"mp4","VUR":"http://artestras.vo.llnwxd.net/o35/nogeo/HBBTV/042975-013-B_EXT_HQ_2_VOF_00604876_MP4-800_AMM-HBBTV_EXTRAIT.mp4"},

        String datum = "", zeit = "";
        String urlHd = "", urlKlein = "", url = "";
        String beschreibung = "";
        String filmWebsite = "";
        String dauerStr = "";
        String titel = "", thema = "", subTitle = "";
        long dauer = 0;
        final String MUSTER_BESCHREIBUNG = "\"VDE\":\"";
        final String MUSTER_FILM_WEBSITE = "\"VUP\":\"";
        final String MUSTER_URL_HD = "\"HBBTV\",\"VQU\":\"SQ\",\"VMT\":\"mp4\",\"VUR\":\"";
        final String MUSTER_URL = "HBBTV\",\"VQU\":\"EQ\",\"VMT\":\"mp4\",\"VUR\":\"";
        final String MUSTER_URL_KLEIN = "HBBTV\",\"VQU\":\"HQ\",\"VMT\":\"mp4\",\"VUR\":\"";
        final String MUSTER_DAUER = "\"videoDurationSeconds\":";
        int pos1, pos2;
        if (Config.getStop()) {
            return;
        }
        meldung(arr[0]);
        seite = getUrlIo.getUri_Utf(sendername, arr[0], seite, "");
        if ((pos1 = seite.indexOf(MUSTER_BESCHREIBUNG)) != -1) {
            pos1 += MUSTER_BESCHREIBUNG.length();
            if ((pos2 = seite.indexOf("\",", pos1)) != -1) {
                beschreibung = seite.substring(pos1, pos2);
                if (!beschreibung.isEmpty() && beschreibung.endsWith("\"")) {
                    beschreibung = beschreibung.substring(0, beschreibung.length() - 2);
                }
            }
        }
        if ((pos1 = seite.indexOf(MUSTER_FILM_WEBSITE)) != -1) {
            pos1 += MUSTER_FILM_WEBSITE.length();
            if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                filmWebsite = seite.substring(pos1, pos2);
            }
        }
        if ((pos1 = seite.indexOf(MUSTER_URL_HD)) != -1) {
            pos1 += MUSTER_URL_HD.length();
            if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                urlHd = seite.substring(pos1, pos2);
            }
        }
        if ((pos1 = seite.indexOf(MUSTER_URL_KLEIN)) != -1) {
            pos1 += MUSTER_URL_KLEIN.length();
            if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                urlKlein = seite.substring(pos1, pos2);
            }
        }
        if ((pos1 = seite.indexOf(MUSTER_URL)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                url = seite.substring(pos1, pos2);
            }
        }
        if ((pos1 = seite.indexOf(MUSTER_DAUER)) != -1) {
            pos1 += MUSTER_DAUER.length();
            if ((pos2 = seite.indexOf(",", pos1)) != -1) {
                dauerStr = seite.substring(pos1, pos2);
                if (!dauerStr.isEmpty()) {
                    try {
                        dauer = Long.parseLong(dauerStr);
                    } catch (Exception ex) {
                        dauer = 0;
                    }

                }
            }
        }
        // Datum ändern
        // arr = new String[]{urlJson, datum, zeit, titel, thema};
        datum = convertDatum(arr[1]);
        zeit = convertZeit(arr[2]);
        titel = arr[3];
        subTitle = seite.extract("\"VSU\":\"", "\",");
        subTitle = subTitle.replace("\\\"", "\"");
        if (!subTitle.isEmpty() && !titel.equals(subTitle)) {
            titel = titel + " - " + subTitle;
        }
        thema = arr[4];
        if (!url.isEmpty()) {
            //    public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
            //         String datum, String zeit, long dauerSekunden, String description, String thumbnailUrl, String imageUrl, String[] keywords) {
            if (!url.endsWith("EXTRAIT.mp4")) {
                // http://artestras.vo.llnwxd.net/o35/nogeo/HBBTV/042975-013-B_EXT_SQ_1_VA_00604871_MP4-2200_AMM-HBBTV_EXTRAIT.mp4
                // sind nur Trailer
                DatenFilm film = new DatenFilm(sendername, thema, filmWebsite, titel, url, "" /*urlRtmp*/,
                        datum, zeit, dauer, beschreibung);
                if (!urlKlein.isEmpty()) {
                    film.addUrlKlein(urlKlein, "");
                }
                if (!urlHd.isEmpty()) {
                    film.addUrlHd(urlHd, "");
                }
                addFilm(film);
            }
        } else if (!urlKlein.isEmpty()) {
            DatenFilm film = new DatenFilm(sendername, thema, filmWebsite, titel, urlKlein, "" /*urlRtmp*/,
                    datum, zeit, dauer, beschreibung);
            if (!urlHd.isEmpty()) {
                film.addUrlHd(urlHd, "");
            }
            addFilm(film);
        } else if (!urlHd.isEmpty()) {
            DatenFilm film = new DatenFilm(sendername, thema, filmWebsite, titel, urlHd, "" /*urlRtmp*/,
                    datum, zeit, dauer, beschreibung);
            addFilm(film);
        } else {
            Log.errorLog(963025874, "Keine URL: " + arr[0]);
        }
    }

    String convertDatum(String datum) {
        // "BAD":"04/08/2013","BAT":"13:20"
        return datum.replace("/", ".");
    }

    String convertZeit(String zeit) {
        // "BAD":"04/08/2013","BAT":"13:20"
        return zeit + ":00";
    }
}
