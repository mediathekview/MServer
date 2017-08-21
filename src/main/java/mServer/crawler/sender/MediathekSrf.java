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

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class MediathekSrf extends MediathekReader
{

    public static final String SENDERNAME = Const.SRF;
    private static final int MAX_SEITEN_THEMA = 5;
    private static final int MAX_FILME_KURZ = 6;

    private static final String URL1_M3U8 = "https://srfvodhd-vh.akamaihd.net";
    private static final String URL2_M3U8 = "http://srfvodhd-vh.akamaihd.net";
    private static final String URL3_M3U8 = "http://hdvodsrforigin-f.akamaihd.net";

    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static final String SRF_TOPIC_PAGE_URL = "http://www.srf.ch/play/v2/tv/topicList?layout=json";
    private static final String SRF_DATA_BLOCK_BEGIN_PATTERN = "data-teaser=\"";
    private static final char SRF_DATA_BLOCK_END = '"';

    public MediathekSrf(FilmeSuchen ssearch, int startPrio)
    {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 100, startPrio);
    }

    @Override
    public void addToList()
    {
        // data-teaser-title="1 gegen 100"
        // data-teaser-url="/sendungen/1gegen100"
        final String muster = "{\"id\":\"";
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri_Utf(SENDERNAME, SRF_TOPIC_PAGE_URL, seite, "");
        int pos = 0;
        int pos1;
        String thema, id;

        while ((pos = seite.indexOf(muster, pos)) != -1)
        {
            pos += muster.length();
            if ((pos1 = seite.indexOf("\"", pos)) != -1)
            {
                id = seite.substring(pos, pos1);
                if (id.length() < 10)
                {
                    //{"id":"A","title":"A","contai....
                    continue;
                }
                if (!id.isEmpty())
                {
                    thema = seite.extract("\"title\":\"", "\"", pos1);
                    thema = StringEscapeUtils.unescapeJava(thema).trim();
                    listeThemen.addUrl(new String[]{id, thema});
                }
            }
        }

        if (Config.getStop() || listeThemen.isEmpty())
        {
            meldungThreadUndFertig();
        } else
        {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t)
            {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private class ThemaLaden extends Thread
    {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());

        private MSStringBuilder overviewPageFilm = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder filmPage = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder m3u8Page = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> urlList = new ArrayList<>();
        private final ArrayList<String> filmList = new ArrayList<>();

        @Override
        public void run()
        {
            try
            {
                meldungAddThread();
                String link[];

                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null)
                {
                    meldungProgress(link[0] /* url */);
                    addFilme(link[0]/*url*/, link[1]/*thema*/);
                }
            } catch (Exception ex)
            {
                Log.errorLog(832002877, ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String urlThema, String thema)
        {

            try
            {
                String urlFeed = "http://www.srf.ch/play/v2/tv/topic/" + urlThema + "/latest";
                overviewPageFilm = new MSStringBuilder();
                overviewPageFilm.append(extractSrfTopicData(getUrl.getUri_Utf(SENDERNAME, urlFeed, overviewPageFilm, "").substring(0)).toCharArray());
                addFilmsFromPage(overviewPageFilm, thema, urlFeed);
                //Not possible actually.
                /*if (CrawlerTool.loadLongMax())
                {
                    String url = urlFeed.substring(0, urlFeed.indexOf("&pageNumber=1"));
                    for (int i = 2; i <= MAX_SEITEN_THEMA; ++i)
                    {
                        if (overviewPageFilm.indexOf("pageNumber=" + i) == -1)
                        {
                            break;
                        } else
                        {
                            // dann gibts weitere Seiten
                            overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, url + "&pageNumber=" + i + "&layout=json", overviewPageFilm, "");
                            addFilmsFromPage(overviewPageFilm, thema, urlThema);
                        }
                    }
                }*/
            } catch (Exception ex)
            {
                Log.errorLog(195926364, ex);
            }
        }

        private String extractSrfTopicData(final String aPageContent)
        {
            int dataBlockBegin = aPageContent.indexOf(SRF_DATA_BLOCK_BEGIN_PATTERN)+SRF_DATA_BLOCK_BEGIN_PATTERN.length();
            String tempData = aPageContent.substring(dataBlockBegin);
            return StringEscapeUtils.unescapeHtml4(tempData.substring(0, tempData.indexOf(SRF_DATA_BLOCK_END)));
        }

        private void addFilmsFromPage(MSStringBuilder page, String thema, String themePageUrl)
        {
            final String baseUrlJson = "http://il.srgssr.ch/integrationlayer/1.0/ue/srf/video/play/";
            final String endUrlJson = ".jsonp";
            int count = 0;
            filmList.clear();
            page.extractList("{\"id\":\"", "?id=", "\"", filmList);

            for (String id : filmList)
            {
                if (Config.getStop())
                {
                    break;
                }
                ++count;
                if (!CrawlerTool.loadLongMax() && count > MAX_FILME_KURZ)
                {
                    break;
                }
                //http://www.srf.ch/play/tv/episodesfromshow?id=c38cc259-b5cd-4ac1-b901-e3fddd901a3d&pageNumber=1&layout=json
                String jsonMovieUrl = baseUrlJson + id + endUrlJson;
                addFilms(jsonMovieUrl, themePageUrl, thema);
            }
        }

        /**
         * This method adds the films from the json file to the film list
         *
         * @param jsonMovieUrl the json url of the film
         * @param themePageUrl the website url of the film
         * @param theme        the theme name of the film
         */
        private void addFilms(String jsonMovieUrl, String themePageUrl, String theme)
        {
            final String index0 = "index_0_av.m3u8";
            final String index1 = "index_1_av.m3u8";
            final String index2 = "index_2_av.m3u8";
            final String index3 = "index_3_av.m3u8";
            final String index4 = "index_4_av.m3u8";
            final String index5 = "index_5_av.m3u8";

            meldung(jsonMovieUrl);

            filmPage = getUrl.getUri_Utf(SENDERNAME, jsonMovieUrl, filmPage, "");
            try
            {

                String dateStr = "";
                String time = "";
                Date date = extractDateAndTime(filmPage);
                if (date != null)
                {
                    DateFormat dfDayMonthYear = new SimpleDateFormat("dd.MM.yyyy");
                    dateStr = dfDayMonthYear.format(date);
                    dfDayMonthYear = new SimpleDateFormat("HH:mm:ss");
                    time = dfDayMonthYear.format(date);
                }

                long duration = extractDuration(filmPage);
                String description = filmPage.extract("\"description\": \"", "\"");
                description = StringEscapeUtils.unescapeJava(description).trim();
                String title = filmPage.extract("AssetMetadatas", "\"title\": \"", "\"");

                String urlThema = filmPage.extract("\"homepageUrl\": \"", "\"");
                if (urlThema.isEmpty())
                {
                    urlThema = "http://www.srf.ch/play/tv/sendungen";
                }
                String subtitle = filmPage.extract("\"TTMLUrl\": \"", "\"");

                String urlHD = getUrl(filmPage, "\"@quality\": \"HD\"", "\"text\": \"");
                String urlNormal = getUrl(filmPage, "\"@quality\": \"SD\"", "\"text\": \"");
                String urlSmall = "";

                String url3u8 = urlHD.endsWith("m3u8") ? urlHD : urlNormal;

                if (url3u8.endsWith("m3u8"))
                {
                    getM3u8(url3u8);
                    if (url3u8.contains("q50,q60"))
                    {
                        if (m3u8Page.indexOf(index5) != -1)
                        {
                            urlHD = getUrlFromM3u8(url3u8, index5);
                        }
                        if (m3u8Page.indexOf(index4) != -1)
                        {
                            urlNormal = getUrlFromM3u8(url3u8, index4);
                        } else if (m3u8Page.indexOf(index3) != -1)
                        {
                            urlNormal = getUrlFromM3u8(url3u8, index3);
                        }
                        if (m3u8Page.indexOf(index2) != -1)
                        {
                            urlSmall = getUrlFromM3u8(url3u8, index2);
                        } else if (m3u8Page.indexOf(index1) != -1)
                        {
                            urlSmall = getUrlFromM3u8(url3u8, index1);
                        }
                    } else
                    {
                        if (m3u8Page.indexOf(index0) != -1)
                        {
                            urlNormal = getUrlFromM3u8(url3u8, index0);
                        }
                        if (m3u8Page.indexOf(index3) != -1)
                        {
                            urlSmall = getUrlFromM3u8(url3u8, index3);
                        } else if (m3u8Page.indexOf(index2) != -1)
                        {
                            urlSmall = getUrlFromM3u8(url3u8, index2);
                        }
                    }
                }

                if (urlNormal.isEmpty() && !urlSmall.isEmpty())
                {
                    urlNormal = urlSmall;
                    urlSmall = "";
                }
                // https -> http
                if (urlNormal.startsWith(HTTPS))
                {
                    urlNormal = urlNormal.replaceFirst(HTTPS, HTTP);
                }
                if (urlSmall.startsWith(HTTPS))
                {
                    urlSmall = urlSmall.replaceFirst(HTTPS, HTTP);
                }
                if (urlHD.startsWith(HTTPS))
                {
                    urlHD = urlHD.replaceFirst(HTTPS, HTTP);
                }

                if (urlNormal.isEmpty())
                {
                    Log.errorLog(962101451, "Keine URL: " + jsonMovieUrl);
                } else
                {
                    urlHD = checkUrl(urlHD);
                    urlNormal = checkUrl(urlNormal);
                    urlSmall = checkUrl(urlSmall);

                    DatenFilm film = new DatenFilm(SENDERNAME, theme, urlThema, title, urlNormal, ""/*rtmpURL*/, dateStr, time, duration, description);

                    if (!urlHD.isEmpty())
                    {
                        CrawlerTool.addUrlHd(film, urlHD, "");
                    }
                    if (!urlSmall.isEmpty())
                    {
                        CrawlerTool.addUrlKlein(film, urlSmall, "");
                    }
                    if (!subtitle.isEmpty())
                    {
                        CrawlerTool.addUrlSubtitle(film, subtitle);
                    }
                    addFilm(film);
                }
            } catch (Exception ex)
            {
                Log.errorLog(556320087, ex);
            }
        }

        private void getM3u8(String url3u8)
        {
            m3u8Page = getUrl.getUri_Utf(SENDERNAME, url3u8, m3u8Page, "");
            if (m3u8Page.length() == 0 && url3u8.startsWith(URL1_M3U8))
            {
                // tauschen https://srfvodhd-vh.akamaihd.net http://hdvodsrforigin-f.akamaihd.net
                // ist ein 403
                String url3u8Temp;
                url3u8Temp = url3u8.replaceFirst(URL1_M3U8, URL3_M3U8);
                m3u8Page = getUrl.getUri_Utf(SENDERNAME, url3u8Temp, m3u8Page, "");
            }
            if (m3u8Page.length() == 0 && url3u8.startsWith(URL2_M3U8))
            {
                // tauschen https://srfvodhd-vh.akamaihd.net http://hdvodsrforigin-f.akamaihd.net
                // ist ein 403
                String url3u8Temp;
                url3u8Temp = url3u8.replaceFirst(URL2_M3U8, URL3_M3U8);
                m3u8Page = getUrl.getUri_Utf(SENDERNAME, url3u8Temp, m3u8Page, "");
            }
        }

        private String checkUrl(String url)
        {
            // tauschen https://srfvodhd-vh.akamaihd.net http://hdvodsrforigin-f.akamaihd.net
            // ist ein 403
            String urlTemp;
            urlTemp = url.replaceFirst(URL1_M3U8, URL3_M3U8);
            return urlTemp.replaceFirst(URL2_M3U8, URL3_M3U8);
        }

        private String getUrl(MSStringBuilder filmPage, String s1, String s2)
        {
            String url = "";
            String m3u8 = "";
            urlList.clear();
            filmPage.extractList(s1, s2, "\"", urlList);

            for (String u : urlList)
            {
                if (!u.endsWith("m3u8") && !u.endsWith("mp4"))
                {
                    continue;
                }
                if (u.endsWith("m3u8"))
                {
                    m3u8 = u;
                }
                if (u.endsWith("mp4"))
                {
                    url = u;
                    break;
                }
            }
            if (url.isEmpty())
            {
                url = m3u8;
            }
            return url;
        }

        private String getUrlFromM3u8(String m3u8Url, String qualityIndex)
        {
            final String csmil = "csmil/";
            return m3u8Url.substring(0, m3u8Url.indexOf(csmil)) + csmil + qualityIndex;
        }

        private long extractDuration(MSStringBuilder page)
        {
            long duration = 0;
            final String patternDuration = "\"duration\":";
            String d = page.extract(patternDuration, ",").trim();
            try
            {
                if (!d.isEmpty())
                {
                    duration = Long.parseLong(d);
                    duration = duration / 1000; //ms
                }
            } catch (NumberFormatException ex)
            {
                Log.errorLog(646490237, ex);
            }
            return duration;
        }

        private Date extractDateAndTime(MSStringBuilder page)
        {
            final String patternDateTime = "\"publishedDate\": \"";
            final String patternEnd = "\"";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");//2014-12-12T09:45:00+02:00

            String dateStr = page.extract("\"AssetMetadatas\"", patternDateTime, patternEnd);

            Date date = null;
            try
            {
                date = formatter.parse(dateStr);
            } catch (ParseException ex)
            {
                Log.errorLog(784512304, ex, "Date_STR " + dateStr);
            }

            return date;
        }

    }
}
