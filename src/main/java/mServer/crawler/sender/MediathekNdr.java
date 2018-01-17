/*    
 *    MediathekView
 *    Copyright (C) 2008   W. Xaver
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler.sender;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.tool.MserverDaten;

public class MediathekNdr extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.NDR;
    private MSStringBuilder seiteAlle = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekNdr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 50, startPrio);
    }

    //-> erste Seite:
    // <h5><a href="/mediathek/mediatheksuche103_broadcast-30.html">Nordmagazin</a></h5>
    @Override
    protected void addToList() {
        //<broadcast id="1391" site="ndrfernsehen">45 Min</broadcast>
        final String ADRESSE = "https://www.ndr.de/mediathek/sendungen_a-z/index.html";
        final String MUSTER_URL1 = "<li><a href=\"/mediathek/mediatheksuche105_broadcast-";
        listeThemen.clear();

        meldungStart();

        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri(SENDERNAME, ADRESSE, StandardCharsets.UTF_8, 5 /* versuche */, seite, ""/* meldung */);
        int pos = 0;
        int pos1;
        int pos2;
        String url = "";
        String thema = "";
        while ((pos = seite.indexOf(MUSTER_URL1, pos)) != -1) {
            try {
                pos += MUSTER_URL1.length();
                pos1 = pos;
                if ((pos2 = seite.indexOf("\"", pos)) != -1) {
                    url = seite.substring(pos1, pos2);
                }
                pos1 = seite.indexOf(">", pos);
                pos2 = seite.indexOf("<", pos);
                if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
                    thema = seite.substring(pos1 + 1, pos2);
                }
                if (url.isEmpty()) {
                    Log.errorLog(210367600, "keine Url");
                    continue;
                }
                String url_ = "https://www.ndr.de/mediathek/mediatheksuche105_broadcast-" + url;
                String[] add = new String[]{url_, thema};
                if (CrawlerTool.loadLongMax()) {
                    if (!alleSeiteSuchen(url_, thema)) {
                        // dann halt so versuchen
                        listeThemen.addUrl(add);
                    }
                } else {
                    listeThemen.addUrl(add);
                }
            } catch (Exception ex) {
                Log.errorLog(332945670, ex);
            }
        }
        // noch "Verpasst" für die letzten Tage einfügen
        // https://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2014-05-17.html
        // https://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2014-05-17_display-onlyvideo.html
        FastDateFormat formatter1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat formatter2 = FastDateFormat.getInstance("dd.MM.yyyy");
        final int maxTage = CrawlerTool.loadLongMax() ? 30 : 20;
        for (int i = 0; i < maxTage; ++i) {
            // https://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2015-09-05_display-all.html
            final String URL = "https://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-";
            final String tag = formatter1.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            final String date = formatter2.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            //String urlString = URL + tag + "_display-onlyvideo.html"; --> stimmt leider nicht immer
            final String urlString = URL + tag + "_display-all.html";
            listeThemen.addUrl(new String[]{urlString, date});
        }

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

    private boolean alleSeiteSuchen(String strUrlFeed, String tthema) {
        boolean ret = false;
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seiteAlle = getUrlIo.getUri(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 3 /* versuche */, seiteAlle, "Thema: " + tthema/* meldung */);
        int pos1 = 0, pos2, anz1, anz2 = 0;
        try {
            // <a class="square button" href="/mediathek/mediatheksuche105_broadcast-1391_page-5.html" title="Zeige Seite 5">
            // https://www.ndr.de/mediathek/mediatheksuche105_broadcast-30_page-1.html
            final String WEITER = " title=\"Zeige Seite ";
            while ((pos1 = seiteAlle.indexOf(WEITER, pos1)) != -1) {
                pos1 += WEITER.length();
                if ((pos2 = seiteAlle.indexOf("\"", pos1)) != -1) {
                    String anz = seiteAlle.substring(pos1, pos2);
                    try {
                        anz1 = Integer.parseInt(anz);
                        if (anz2 < anz1) {
                            anz2 = anz1;
                        }
                    } catch (Exception ex) {
                        Log.errorLog(643208979, strUrlFeed);
                    }
                }
            }
            for (int i = 2; i <= anz2 && i <= 10; ++i) {
                // geht bei 2 los da das ja schon die erste Seite ist!
                //das:   https://www.ndr.de/mediathek/mediatheksuche105_broadcast-30.html
                // wird: https://www.ndr.de/mediathek/mediatheksuche105_broadcast-30_page-3.html
                String url_ = strUrlFeed.replace(".html", "_page-" + i + ".html");
                listeThemen.addUrl(new String[]{url_, tthema});
                ret = true;
            }
        } catch (Exception ex) {
            Log.errorLog(913047821, strUrlFeed);
        }
        return ret;
    }

    private class ThemaLaden extends Thread {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> liste = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    try {
                        meldungProgress(link[1]);
                        feedEinerSeiteSuchen(link[0], link[1] /* thema */);
                    } catch (Exception ex) {
                        Log.errorLog(336901211, ex);
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(554632590, ex);
            }
            meldungThreadUndFertig();
        }

        private long convertDuration(final String duration, final String strUrlFeed) {
            long durationInSeconds = 0;
            try {
                if (!duration.isEmpty()) {
                    final String[] parts = duration.split(":");
                    long power = 1;
                    durationInSeconds = 0;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        durationInSeconds += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            } catch (NumberFormatException ex) {
                if (MserverDaten.debug)
                    Log.errorLog(369015497, ex, strUrlFeed);
            } catch (Exception ex) {
                Log.errorLog(369015497, ex, strUrlFeed);
            }

            return durationInSeconds;
        }

        private void feedEinerSeiteSuchen(String strUrlFeed, String tthema) {
            final String MUSTER_URL = "<a href=\"";
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite1 = getUrlIo.getUri(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 3 /* versuche */, seite1, "Thema: " + tthema/* meldung */);
            int pos = 0;
            String url;
            String titel;
            String thema = tthema;
            String datum = "";
            String zeit = "";
            long durationInSeconds;
            String tmp;
            boolean tage = false;
            try {
                meldung(strUrlFeed);
                String muster;
                if (seite1.indexOf("<strong class=\"time\">") != -1) {
                    muster = "<strong class=\"time\">";
                    tage = true;
                } else {
                    muster = "<span class=\"icon icon_video\"></span>";
                }
                while (!Config.getStop() && (pos = seite1.indexOf(muster, pos)) != -1) {
                    pos += muster.length();
                    url = seite1.extract(MUSTER_URL, "\"", pos);
                    if (url.isEmpty()) {
                        Log.errorLog(659210274, "keine Url feedEinerSeiteSuchen" + strUrlFeed);
                        continue;
                    }
                    if (!url.startsWith("http")) {
                        url = "https://www.ndr.de" + url;
                    }
                    if (tage) {
                        // <h3><a href="/fernsehen/epg/import/Rote-Rosen,sendung64120.html" title="Rote Rosen"  >Rote Rosen (1725)</a></h3>
                        thema = seite1.extract(MUSTER_URL, " title=\"", "\"", pos, 0, "");
                        titel = seite1.extract(MUSTER_URL, ">", "<", pos, 0, "");
                        if (titel.contains("(Wdh.)")) {
                            // dann sollte der Beitrag schon in der Liste sein
                            continue;
                        }
                        if (thema.equals(titel)) {
                          if (thema.contains(" - ")) {
                            thema = thema.substring(0, thema.indexOf(" - ")).trim();
                            titel = titel.substring(titel.indexOf(" - "));
                            titel = titel.replace(" - ", "").trim();
                          } else {
                            String subtitle = seite1.extract("class=\"subtitle\">", "<", pos).trim();
                            if (!subtitle.isEmpty()) {
                              titel = subtitle;
                            }
                          }
                        }
                    } else {
                        titel = seite1.extract(" title=\"", "\"", pos);
                        titel = titel.replace("Zum Video:", "").trim();
                    }
                    if (tage) {
                        tmp = seite1.substring(pos, seite1.indexOf("<", pos));
                        datum = tthema;
                        try {
                            Date filmDate = FastDateFormat.getInstance("HH:mm").parse(tmp);
                            zeit = FastDateFormat.getInstance("HH:mm:ss").format(filmDate);
                        } catch (Exception ex) {
                            Log.errorLog(795623017, "convertDatum: " + strUrlFeed);
                        }
                    } else {
                        tmp = seite1.extract("<div class=\"subline date\">", "<", pos);
                        String[] dateValues = parseDateTime(tmp, strUrlFeed);
                        datum = dateValues[0];
                        zeit = dateValues[1];
                    }
                    if (tage) {
                        //<span class="icon icon_video" aria-label="L&auml;nge"></span>29:59</div>
                        String duration = seite1.extract("\"L&auml;nge\"></span>", "<", pos).trim();
                        durationInSeconds = convertDuration(duration, strUrlFeed);
                    } else {
                        String duration = seite1.extract("Video (", ")", pos);
                        duration = duration.replace("min", "").trim();
                        durationInSeconds = convertDuration(duration, strUrlFeed);
                    }
                    filmSuchen_1(strUrlFeed, thema, titel, url, datum, zeit, durationInSeconds);
                }
            } catch (Exception ex) {
                Log.errorLog(693219870, strUrlFeed);
            }
        }
        
        private String[] parseDateTime(String dateTimeValue, String strUrlFeed) {
            String[] dateValues = new String[2];
            dateValues[0] = "";
            dateValues[1] = "";
            
            String dateTime = dateTimeValue.replace("Uhr", "").trim();
            
            if (!dateTime.isEmpty()) {
                try {
                    Date filmDate = FastDateFormat.getInstance("dd.MM.yyyy HH:mm").parse(dateTime);
                    dateValues[0] = FastDateFormat.getInstance("dd.MM.yyyy").format(filmDate);
                    dateValues[1] = FastDateFormat.getInstance("HH:mm:ss").format(filmDate);
                } catch (Exception ex) {
                    Log.errorLog(623657941, "convertDatum: " + strUrlFeed);
                }
            }

            return dateValues;
        }

        private void filmSuchen_1(String strUrlThema, String thema, String titel, String filmWebsite, String datum, String zeit,
                                  long durationInSeconds) {
            //playlist: [
            //{
            //1: {src:'https://hds.ndr.de/z/2013/0419/TV-20130419-1010-0801.,hi,hq,.mp4.csmil/manifest.f4m', type:"application/f4m+xml"},
            //2: {src:'https://hls.ndr.de/i/2013/0419/TV-20130419-1010-0801.,lo,hi,hq,.mp4.csmil/master.m3u8', type:"application/x-mpegURL"},
            //3: {src:'https://media.ndr.de/progressive/2013/0419/TV-20130419-1010-0801.hi.mp4', type:"video/mp4"},

            // https://media.ndr.de/progressive/2012/0820/TV-20120820-2300-0701.hi.mp4
            // rtmpt://cp160844.edgefcs.net/ondemand/mp4:flashmedia/streams/ndr/2012/0820/TV-20120820-2300-0701.hq.mp4
            seite2 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, seite2, "strUrlThema: " + strUrlThema);
            String description = extractDescription(seite2);
            //String[] keywords = extractKeywords(seite2);
            String subtitle = seite2.extract(",tracks: [{ src: \"", "\""); //,tracks: [{ src: "/fernsehen/sendungen/45_min/video-podcast/ut20448.xml", srclang:"de"}]
            if (!subtitle.isEmpty()) {
                subtitle = "https://www.ndr.de" + subtitle;
//            } else {
//                System.out.println("Test");
            }
            meldung(filmWebsite);
            int pos1;
            try {
                // src="/fernsehen/hallondsopplatt162-player_image-2c09ece0-0508-49bf-b4d6-afff2be2115c_theme-ndrde.html"
                // https://www.ndr.de/fernsehen/hallondsopplatt162-ppjson_image-2c09ece0-0508-49bf-b4d6-afff2be2115c.json
                // id="pp_hallondsopplatt162"
                if (datum.isEmpty()) {
                    String tmp = seite2.extract("<span itemprop=\"datePublished\"", "</");
                    if ((pos1 = tmp.indexOf(">")) != -1) {
                        tmp = tmp.substring(pos1 + 1, tmp.length());
                        String[] dateValues = parseDateTime(tmp, strUrlThema);
                        datum = dateValues[0];
                        zeit = dateValues[1];
                    }
                }
                
                String json = seite2.extract("<meta itemprop=\"embedURL\" content=\"", "\"");
                if (!json.isEmpty()) {
                    json = json.replace("-player.html", "-ardjson.json");
                    filmSuchen_2(strUrlThema, thema, titel, filmWebsite, json, datum, zeit, durationInSeconds, description, subtitle);

                } else {
                    Log.errorLog(915230214, "auch keine Url: " + filmWebsite);
                }
            } catch (Exception ex) {
                Log.errorLog(699830157, ex);
            }
        }

        private void filmSuchen_2(String strUrlThema, String thema, String titel, String filmWebsite, String json, String datum, String zeit,
                                  long durationInSeconds, String description, String subtitle) {

            seite3 = getUrl.getUri_Utf(SENDERNAME, json, seite3, "strUrlThema: " + strUrlThema);
            String url_hd = "", url_xl = "", url_m = "";
            seite3.extractList("", "", "\"_stream\": \"https://mediandr", "\"", "https://mediandr", liste);

            for (String s : liste) {
                if (s.endsWith(".hd.mp4")) {
                    url_hd = s;
                } else if (s.endsWith(".hq.mp4")) {
                    url_xl = s;
                } else if (s.endsWith(".hi.mp4")) {
                    url_m = s;
                }
            }
            liste.clear();
            if (url_xl.isEmpty()) {
                url_xl = url_m;
                url_m = "";
            }

            final String http = "http:";
            final String https = "https:";
            url_hd = url_hd.replaceFirst(https, http);
            url_xl = url_xl.replaceFirst(https, http);
            url_m = url_m.replaceFirst(https, http);

            if (subtitle.isEmpty()) {
                subtitle = seite3.extract("\"_subtitleUrl\":", "\"/", "\"", "https://www.ndr.de/");
            }

            if (!url_xl.isEmpty()) {
                DatenFilm film = new DatenFilm(SENDERNAME, thema, filmWebsite, titel, url_xl, ""/*rtmpURL*/, datum, zeit, durationInSeconds, description);
                if (!subtitle.isEmpty()) {
                    CrawlerTool.addUrlSubtitle(film, subtitle);
                }
                if (!url_hd.isEmpty()) {
                    CrawlerTool.addUrlHd(film, url_hd, "");
                }
                if (!url_m.isEmpty()) {
                    CrawlerTool.addUrlKlein(film, url_m, "");
                }
                addFilm(film);
            } else {
                Log.errorLog(915234210, "keine URL im json: " + filmWebsite);
            }
        }

        private String extractDescription(MSStringBuilder page) {
            String desc = extractString(page, "<meta property=\"og:description\" content=\"", "\"");
            if (desc == null) {
                return "";
            }
            return desc;
        }

/*        private String[] extractKeywords(MSStringBuilder page) {
            String keywords = extractString(page, "<meta name=\"keywords\"  lang=\"de\" content=\"", "\"");
            if (keywords == null) {
                return new String[]{""};
            }
            String[] k = keywords.split(",");
            for (int i = 0; i < k.length; i++) {
                k[i] = k[i].trim();
            }
            return k;
        }*/

        private String extractString(MSStringBuilder source, String startMarker, String endMarker) {
            int start = source.indexOf(startMarker);
            if (start == -1) {
                return null;
            }
            start = start + startMarker.length();
            int end = source.indexOf(endMarker, start);
            if (end == -1) {
                return null;
            }
            return source.substring(start, end);
        }
    }
}
