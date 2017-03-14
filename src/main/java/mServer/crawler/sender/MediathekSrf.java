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

import java.text.DateFormat;
import java.text.ParseException;
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
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekSrf extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.SRF;
    private final static int MAX_SEITEN_THEMA = 5;
    private final static int MAX_FILME_KURZ = 6;

    public MediathekSrf(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 400, startPrio);
    }

    @Override
    public void addToList() {
        // data-teaser-title="1 gegen 100"
        // data-teaser-url="/sendungen/1gegen100"
        final String MUSTER = "{\"id\":\"";
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
        seite = getUrlIo.getUri_Utf(SENDERNAME, "http://www.srf.ch/play/tv/atozshows/list?layout=json", seite, "");
        int pos = 0;
        int pos1;
        String thema, id;

        while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
            pos += MUSTER.length();
            if ((pos1 = seite.indexOf("\"", pos)) != -1) {
                id = seite.substring(pos, pos1);
                if (id.length() < 10) {
                    //{"id":"A","title":"A","contai....
                    continue;
                }
                if (!id.isEmpty()) {
                    thema = seite.extract("\"title\":\"", "\"", pos1);
                    thema = StringEscapeUtils.unescapeJava(thema).trim();
                    listeThemen.addUrl(new String[]{id, thema});
                }
            }
        }

        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private class ThemaLaden implements Runnable {

        GetUrl getUrl = new GetUrl(getWartenSeiteLaden());

        private final MSStringBuilder film_website = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder overviewPageFilm = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder filmPage = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder m3u8Page = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final static String PATTERN_URL = "\"url\":\"";
        private final static String PATTERN_URL_END = "\"";
        private final ArrayList<String> urlList = new ArrayList<>();
        private final ArrayList<String> filmList = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];

                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addFilme(link[0]/*url*/, link[1]/*thema*/);
                }
            } catch (Exception ex) {
                Log.errorLog(832002877, ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String urlThema, String thema) {

            try {
                String urlFeed = "http://www.srf.ch/play/tv/episodesfromshow?id=" + urlThema + "&pageNumber=1&layout=json";
                overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, urlFeed, overviewPageFilm, "");
                addFilmsFromPage(overviewPageFilm, thema, urlFeed);
                if (CrawlerTool.loadLongMax()) {
                    String url = urlFeed.substring(0, urlFeed.indexOf("&pageNumber=1"));
                    for (int i = 2; i <= MAX_SEITEN_THEMA; ++i) {
                        if (overviewPageFilm.indexOf("pageNumber=" + i) == -1) {
                            break;
                        } else {
                            // dann gibts weitere Seiten
                            overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, url + "&pageNumber=" + i + "&layout=json", overviewPageFilm, "");
                            addFilmsFromPage(overviewPageFilm, thema, urlThema);
                        }
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(195926364, ex);
            }
        }

        private void addFilmsFromPage(MSStringBuilder page, String thema, String themePageUrl) {
            final String BASE_URL_JSON = "http://il.srgssr.ch/integrationlayer/1.0/ue/srf/video/play/";
            final String END_URL_JSON = ".jsonp";
            int count = 0;
            filmList.clear();
            page.extractList("{\"id\":\"", "?id=", "\"", filmList);

            for (String id : filmList) {
                if (Config.getStop()) {
                    break;
                }
                ++count;
                if (!CrawlerTool.loadLongMax() && count > MAX_FILME_KURZ) {
                    break;
                }
                //http://www.srf.ch/play/tv/episodesfromshow?id=c38cc259-b5cd-4ac1-b901-e3fddd901a3d&pageNumber=1&layout=json
                String jsonMovieUrl = BASE_URL_JSON + id + END_URL_JSON;
                addFilms(jsonMovieUrl, themePageUrl, thema);
            }
        }

        /**
         * This method adds the films from the json file to the film list
         *
         * @param jsonMovieUrl the json url of the film
         * @param themePageUrl the website url of the film
         * @param theme the theme name of the film
         */
        private void addFilms(String jsonMovieUrl, String themePageUrl, String theme) {
            final String INDEX_0 = "index_0_av.m3u8";
            final String INDEX_1 = "index_1_av.m3u8";
            final String INDEX_2 = "index_2_av.m3u8";
            final String INDEX_3 = "index_3_av.m3u8";
            final String INDEX_4 = "index_4_av.m3u8";
            final String INDEX_5 = "index_5_av.m3u8";

            meldung(jsonMovieUrl);

            filmPage = getUrl.getUri_Utf(SENDERNAME, jsonMovieUrl, filmPage, "");
            try {

                String date_str = "";
                String time = "";
                Date date = extractDateAndTime(filmPage);
                if (date != null) {
                    DateFormat dfDayMonthYear = new SimpleDateFormat("dd.MM.yyyy");
                    date_str = dfDayMonthYear.format(date);
                    dfDayMonthYear = new SimpleDateFormat("HH:mm:ss");
                    time = dfDayMonthYear.format(date);
                }

                long duration = extractDuration(filmPage);
                String description = filmPage.extract("\"description\": \"", "\"");
                description = StringEscapeUtils.unescapeJava(description).trim();
                String title = filmPage.extract("AssetMetadatas", "\"title\": \"", "\"");

                String urlThema = filmPage.extract("\"homepageUrl\": \"", "\"");
                if (urlThema.isEmpty()) {
                    urlThema = "http://www.srf.ch/play/tv/sendungen";
                }
                String subtitle = filmPage.extract("\"TTMLUrl\": \"", "\"");

                String urlHD = getUrl(filmPage, "\"@quality\": \"HD\"", "\"text\": \"");
                String url_normal = getUrl(filmPage, "\"@quality\": \"SD\"", "\"text\": \"");
                String url_small = "";

                String url3u8 = urlHD.endsWith("m3u8") ? urlHD : url_normal;

                if (url3u8.endsWith("m3u8")) {
                    m3u8Page = getUrl.getUri_Utf(SENDERNAME, url3u8, m3u8Page, "");
                    if (m3u8Page.length() == 0) {
                        // tauschen https://srfvodhd-vh.akamaihd.net http://hdvodsrforigin-f.akamaihd.net
                        // ist ein 403
                        if (url3u8.startsWith("https://srfvodhd-vh.akamaihd.net")) {
                            url3u8 = url3u8.replaceFirst("https://srfvodhd-vh.akamaihd.net", "http://hdvodsrforigin-f.akamaihd.net");
                            m3u8Page = getUrl.getUri_Utf(SENDERNAME, url3u8, m3u8Page, "");
                        }
                    }
                    if (url3u8.contains("q50,q60")) {
                        if (m3u8Page.indexOf(INDEX_5) != -1) {
                            urlHD = getUrlFromM3u8(url3u8, INDEX_5);
                        }
                        if (m3u8Page.indexOf(INDEX_4) != -1) {
                            url_normal = getUrlFromM3u8(url3u8, INDEX_4);
                        } else if (m3u8Page.indexOf(INDEX_3) != -1) {
                            url_normal = getUrlFromM3u8(url3u8, INDEX_3);
                        }
                        if (m3u8Page.indexOf(INDEX_2) != -1) {
                            url_small = getUrlFromM3u8(url3u8, INDEX_2);
                        } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                            url_small = getUrlFromM3u8(url3u8, INDEX_1);
                        }
                    } else {
                        System.out.println(url3u8);
                        if (m3u8Page.indexOf(INDEX_0) != -1) {
                            url_normal = getUrlFromM3u8(url3u8, INDEX_0);
                        }
                        if (m3u8Page.indexOf(INDEX_3) != -1) {
                            url_small = getUrlFromM3u8(url3u8, INDEX_3);
                        } else if (m3u8Page.indexOf(INDEX_2) != -1) {
                            url_small = getUrlFromM3u8(url3u8, INDEX_2);
                        }
                    }
                }

                if (url_normal.isEmpty()) {
                    if (!url_small.isEmpty()) {
                        url_normal = url_small;
                        url_small = "";
                    }
                }
                // https -> http
                if (url_normal.startsWith("https")) {
                    url_normal = url_normal.replaceFirst("https", "http");
                }
                if (url_small.startsWith("https")) {
                    url_small = url_small.replaceFirst("https", "http");
                }
                if (urlHD.startsWith("https")) {
                    urlHD = urlHD.replaceFirst("https", "http");
                }

                if (url_normal.isEmpty()) {
                    Log.errorLog(962101451, "Keine URL: " + jsonMovieUrl);
                } else {
                    DatenFilm film = new DatenFilm(SENDERNAME, theme, urlThema, title, url_normal, ""/*rtmpURL*/, date_str, time, duration, description);

                    if (!urlHD.isEmpty()) {
                        CrawlerTool.addUrlHd(film, urlHD, "");
                    }
                    if (!url_small.isEmpty()) {
                        CrawlerTool.addUrlKlein(film, url_small, "");
                    }
                    if (!subtitle.isEmpty()) {
                        CrawlerTool.addUrlSubtitle(film, subtitle);
                    }
                    addFilm(film);
                }
            } catch (Exception ex) {
                Log.errorLog(556320087, ex);
            }
        }

        private String getUrl(MSStringBuilder filmPage, String s1, String s2) {
            String url = "";
            String m3u8 = "";
            urlList.clear();
            filmPage.extractList(s1, s2, "\"", urlList);

            for (String u : urlList) {
                if (!u.endsWith("m3u8") && !u.endsWith("mp4")) {
                    continue;
                }
                if (u.endsWith("m3u8")) {
                    m3u8 = u;
                }
                if (u.endsWith("mp4")) {
                    url = u;
                    break;
                }
            }
            if (url.isEmpty()) {
                url = m3u8;
            }
            return url;
        }

        private String getUrlFromM3u8(String m3u8Url, String qualityIndex) {
            final String CSMIL = "csmil/";
            String url = m3u8Url.substring(0, m3u8Url.indexOf(CSMIL)) + CSMIL + qualityIndex;
            return url;
        }

        private long extractDuration(MSStringBuilder page) {
            long duration = 0;
            final String PATTERN_DURATION = "\"duration\":";
            String d = page.extract(PATTERN_DURATION, ",").trim();
            try {
                if (!d.isEmpty()) {
                    duration = Long.parseLong(d);
                    duration = duration / 1000; //ms
                }
            } catch (NumberFormatException ex) {
                Log.errorLog(646490237, ex);
            }
            return duration;
        }

        private Date extractDateAndTime(MSStringBuilder page) {
            final String PATTERN_DATE_TIME = "\"publishedDate\": \"";
            final String PATTERN_END = "\"";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");//2014-12-12T09:45:00+02:00

            String date_str = page.extract("\"AssetMetadatas\"", PATTERN_DATE_TIME, PATTERN_END);

            Date date = null;
            try {
                date = formatter.parse(date_str);
            } catch (ParseException ex) {
                Log.errorLog(784512304, ex, "Date_STR " + date_str);
            }

            return date;
        }

    }
}
