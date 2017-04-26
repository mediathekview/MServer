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
package mServer.crawler.sender.arte;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.MediathekReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import de.mediathekview.mlib.tool.MVHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MediathekArte_de extends MediathekReader
{
    private static final Logger LOG = LogManager.getLogger(MediathekArte_de.class);
    private final static String SENDERNAME = Const.ARTE_DE;
    private static final String ARTE_API_TAG_URL_PATTERN = "http://www.arte.tv/guide/api/api/program/de/scheduled/%s";
    private static final DateTimeFormatter ARTE_API_DATEFORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd");
    protected String URL_CONCERT = "http://concert.arte.tv/de/videos/all";
    protected String URL_CONCERT_NOT_CONTAIN = "-STF";
    protected String TIME_1 = "<li>Sendetermine:</li>";
    protected String TIME_2 = "um";

    public MediathekArte_de(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 2, /* urlWarten */ 200, startPrio);
    }

    public MediathekArte_de(FilmeSuchen ssearch, int startPrio, String name) {
        super(ssearch, name,/* threads */ 2, /* urlWarten */ 200, startPrio);
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
                Thread th = new ThemaLaden();
                th.setName(getSendername() + t);
                th.start();
            }
        }
    }

    private void addConcert() {
        Thread th = new ConcertLaden(0, 20);
        th.setName(getSendername() + "Concert-0");
        th.start();
        th = new ConcertLaden(20, 40);
        th.setName(getSendername() + "Concert-1");
        th.start();
    }

    private void addTage() {
        // http://www.arte.tv/guide/de/plus7/videos?day=-2&page=1&isLoading=true&sort=newest&country=DE
        for (int i = 0; i <= 14; ++i) {
            String u = String.format(ARTE_API_TAG_URL_PATTERN,LocalDate.now().minusDays(i).format(ARTE_API_DATEFORMATTER));
            listeThemen.add(new String[]{u});
        }
    }

    private class ConcertLaden extends Thread {

        private final int start, anz;
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

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
                GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrlIo.getUri_Utf(getSendername(), urlStart, seite1, "");
                int pos1 = 0;
                String url, urlWeb, titel, urlHd, urlLow, urlNormal, beschreibung, datum, dauer;
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
                        if (!dauer.isEmpty()) {
                            String[] parts = dauer.split(":");
                            duration = 0;
                            long power = 1;
                            for (int ii = parts.length - 1; ii >= 0; ii--) {
                                duration += Long.parseLong(parts[ii]) * power;
                                power *= 60;
                            }
                        }
                        if (url.isEmpty()) {
                            Log.errorLog(825241452, "keine URL");
                        } else {
                            urlWeb = "http://concert.arte.tv" + url;
                            meldung(urlWeb);
                            seite2 = getUrlIo.getUri_Utf(getSendername(), urlWeb, seite2, "");
                            // genre: <span class="tag tag-link"><a href="/de/videos/rockpop">rock/pop</a></span> 
                            String genre = seite2.extract("<span class=\"tag tag-link\">", "\">", "<");
                            if (!genre.isEmpty()) {
                                beschreibung = genre + '\n' + DatenFilm.cleanDescription(beschreibung, THEMA, titel);
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

    class ThemaLaden extends Thread {


        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addFilmeForTag(link[0]);
                }
            } catch (Exception ex) {
                Log.errorLog(894330854, ex, "");
            }
            meldungThreadUndFertig();
        }

        private void addFilmeForTag(String aUrl) {
            Gson gson = new GsonBuilder().registerTypeAdapter(ListeFilme.class,new ArteDatenFilmDeserializer()).create();
            
            MVHttpClient mvhttpClient = MVHttpClient.getInstance();
            OkHttpClient httpClient = mvhttpClient.getHttpClient();
            Request request = new Request.Builder().url(aUrl).build();
             try
             {
                 Response response = httpClient.newCall(request).execute();

                 ListeFilme loadedFilme = gson.fromJson(response.body().string(), ListeFilme.class);
                 for (DatenFilm film : loadedFilme)
                 {
                     addFilm(film);
                 }
             }catch (IOException ioException)
             {
                LOG.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.",ioException);
             }
        }

    }

}
