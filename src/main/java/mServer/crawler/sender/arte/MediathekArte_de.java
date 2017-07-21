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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import de.mediathekview.mlib.tool.MVHttpClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.MediathekReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MediathekArte_de extends MediathekReader
{
    /*
     * Informationen zu den ARTE-URLs:
     * {} sind nur Makierungen, dass es Platzhalter sind, sie gehören nicht zur URL.
     * 
     * Allgemeine URL eines Films:  (050169-002-A = ID des Films); (die-spur-der-steine = Titel)
     * http://www.arte.tv/de/videos/{050169-002-A}/{die-spur-der-steine}
     * 
     * Alle Sendungen: (Deutsch = DE; Französisch = FR)
     * https://api.arte.tv/api/opa/v3/videos?channel={DE}
     * 
     * Informationen zum Film: (050169-002-A = ID des Films); (de für deutsch / fr für französisch)
     * https://api.arte.tv/api/player/v1/config/{de}/{050169-002-A}?platform=ARTE_NEXT
     * 
     * Zweite Quelle für Informationen zum Film: (050169-002-A = ID des Films); (de für deutsch / fr für französisch)
     * https://api.arte.tv/api/opa/v3/programs/{de}/{050169-002-A}
     * 
     */
    private static final Logger LOG = LogManager.getLogger(MediathekArte_de.class);
    private final static String SENDERNAME = Const.ARTE_DE;
    private static final String ARTE_API_TAG_URL_PATTERN = "https://api.arte.tv/api/opa/v3/videos?channel=%s&arteSchedulingDay=%s";
    
    private static final String URL_STATIC_CONTENT = "https://static-cdn.arte.tv/components/src/header/assets/locales/%s.json?ver=%s";
    private static final String URL_CATEGORY = "http://www.arte.tv/guide/api/api/pages/category/%s/web/%s";
    private static final String URL_SUBCATEGORY = "http://www.arte.tv/guide/api/api/videos/%s/subcategory/%s?page=%s";
    private static final String VERSION_STATIC_CONTENT = "2.3.13";

    private static final DateTimeFormatter ARTE_API_DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_TOKEN = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
    protected String LANG_CODE = "de";
    protected String URL_CONCERT = "http://concert.arte.tv/de/videos/all";
    protected String URL_CONCERT_NOT_CONTAIN = "-STF";
    protected String TIME_1 = "<li>Sendetermine:</li>";
    protected String TIME_2 = "um";
    private static final String SUFFIX_M3U8 = ".m3u8";

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
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else {
            if (CrawlerTool.loadLongMax()) {
                addCategories();
                meldungAddMax(listeThemen.size());
                for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                    Thread th = new CategoryLoader();
                    th.setName(getSendername() + t);
                    th.start();
                }
                
            } else {
                addTage();
                meldungAddMax(listeThemen.size());
                for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                    Thread th = new ThemaLaden();
                    th.setName(getSendername() + t);
                    th.start();
                }
            }
        }
    }

    private void addCategories() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ArteInfoDTO.class,new ArteStaticContentDeserializer())
                .registerTypeAdapter(ArteCategoryDTO.class,new ArteCategoryDeserializer())
                .create();
        
        String url = String.format(URL_STATIC_CONTENT,LANG_CODE.toLowerCase(),VERSION_STATIC_CONTENT);
        
        MVHttpClient mvhttpClient = MVHttpClient.getInstance();
        OkHttpClient httpClient = mvhttpClient.getHttpClient();
        Request request = new Request.Builder()
                    .addHeader(AUTH_HEADER, AUTH_TOKEN)
                    .url(url).build();
        try
        {
            Response response = httpClient.newCall(request).execute();

            if(response.isSuccessful())
            {
                ArteInfoDTO info = gson.fromJson(response.body().string(), ArteInfoDTO.class);
                info.getCategories().forEach(category -> {
                    String categoryUrl = String.format(URL_CATEGORY, LANG_CODE.toLowerCase(), info.getCategoryUrl(category));
                    Request requestCategory = new Request.Builder()
                                .addHeader(AUTH_HEADER, AUTH_TOKEN)
                                .url(categoryUrl).build();
                    Response responseCategory;
                    try {
                        responseCategory = httpClient.newCall(requestCategory).execute();
                        if(responseCategory.isSuccessful()) {
                            ArteCategoryDTO categoryDto = gson.fromJson(responseCategory.body().string(), ArteCategoryDTO.class);
                            categoryDto.getSubCategories().forEach(subCategory -> {
                                String subCategoryUrl = String.format(URL_SUBCATEGORY, LANG_CODE.toLowerCase(), subCategory, 1);
                                listeThemen.add(new String[]{ subCategory, subCategoryUrl });
                            });
                        }
                    } catch (IOException ioException) {
                        LOG.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.",ioException);
                    }
                });
            }

        } catch (IOException ioException)
        {
           LOG.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.",ioException);
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
            String u = String.format(ARTE_API_TAG_URL_PATTERN,LANG_CODE.toUpperCase(),LocalDate.now().minusDays(i).format(ARTE_API_DATEFORMATTER));
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
            final String thema = "Concert";
            final String musterStart = "<div class=\"header-article \">";
            final String errorMsgKeineURL = "keine URL";
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
                while (!Config.getStop() && (pos1 = seite1.indexOf(musterStart, pos1)) != -1) {
                    urlHd = "";
                    urlLow = "";
                    urlNormal = "";
                    pos1 += musterStart.length();
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
                            Log.errorLog(825241452, errorMsgKeineURL);
                        } else {
                            urlWeb = "http://concert.arte.tv" + url;
                            meldung(urlWeb);
                            seite2 = getUrlIo.getUri_Utf(getSendername(), urlWeb, seite2, "");
                            // genre: <span class="tag tag-link"><a href="/de/videos/rockpop">rock/pop</a></span> 
                            String genre = seite2.extract("<span class=\"tag tag-link\">", "\">", "<");
                            if (!genre.isEmpty()) {
                                beschreibung = genre + '\n' + DatenFilm.cleanDescription(beschreibung, thema, titel);
                            }
                            url = seite2.extract("arte_vp_url=\"", "\"");
                            if (url.isEmpty()) {
                                Log.errorLog(784512698, errorMsgKeineURL);
                            } else {
                                seite2 = getUrlIo.getUri_Utf(getSendername(), url, seite2, "");
                                int p1 = 0;
                                String a = "\"bitrate\":800";
                                String b = "\"url\":\"";
                                String c = "\"";
                                while ((p1 = seite2.indexOf(a, p1)) != -1) {
                                    p1 += a.length();
                                    urlLow = seite2.extract(b, c, p1).replace("\\", "");
                                    if (urlLow.endsWith(SUFFIX_M3U8)) {
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
                                    if (urlNormal.endsWith(SUFFIX_M3U8)) {
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
                                    if (urlHd.endsWith(SUFFIX_M3U8)) {
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
                                    Log.errorLog(951236487, errorMsgKeineURL);
                                }
                                if (urlNormal.isEmpty()) {
                                    Log.errorLog(989562301, errorMsgKeineURL);
                                } else {
                                    DatenFilm film = new DatenFilm(getSendername(), thema, urlWeb, titel, urlNormal, "" /*urlRtmp*/,
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
            Gson gson = new GsonBuilder().registerTypeAdapter(ListeFilme.class,new ArteDatenFilmDeserializer(LANG_CODE, getSendername())).create();
            
            MVHttpClient mvhttpClient = MVHttpClient.getInstance();
            OkHttpClient httpClient = mvhttpClient.getHttpClient();
            Request request = new Request.Builder()
                    .addHeader(AUTH_HEADER, AUTH_TOKEN)
                    .url(aUrl).build();
             try
             {
                 Response response = httpClient.newCall(request).execute();

                 if(response.isSuccessful())
                 {
                     ListeFilme loadedFilme = gson.fromJson(response.body().string(), ListeFilme.class);
                     for (DatenFilm film : loadedFilme)
                     {
                         addFilm(film);
                     }
                 }

             }catch (IOException ioException)
             {
                LOG.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.",ioException);
             }
        }

    }

    class CategoryLoader extends Thread {

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] + "/" + link[1] /* url */);
                    loadSubCategory(link[0], link[1]);
                }
            } catch (Exception ex) {
                Log.errorLog(894330854, ex, "");
            }
            meldungThreadUndFertig();
        }

        private void loadSubCategory(String aCategory, String aUrl) {
            Gson gson = new GsonBuilder().registerTypeAdapter(ArteCategoryFilmsDTO.class,new ArteCategoryFilmListDeserializer()).create();
            
            // erste Seite laden
            ArteCategoryFilmsDTO dto = loadSubCategoryPage(gson, aUrl);
            if(dto != null) {
                // weitere Seiten laden und zu programId-liste des ersten DTO hinzufügen
                for(int i = 1; i < dto.getPages(); i++) {
                    String url = String.format(URL_SUBCATEGORY, LANG_CODE.toLowerCase(), aCategory, i);
                    ArteCategoryFilmsDTO nextDto = loadSubCategoryPage(gson, url);
                    if(nextDto != null) {
                        nextDto.getProgramIds().forEach(programId -> dto.addProgramId(programId));
                    }
                }
            }
            
            // alle programIds verarbeiten
            ListeFilme loadedFilme = loadPrograms(dto);
            loadedFilme.forEach((film) -> {
                addFilm(film);
            });
        }

        private ListeFilme loadPrograms(ArteCategoryFilmsDTO dto) {
            ListeFilme listeFilme = new ListeFilme();

            Collection<Future<DatenFilm>> futureFilme = new ArrayList<>();
            dto.getProgramIds().forEach(programId -> {
                ExecutorService executor = Executors.newCachedThreadPool();
                futureFilme.add(executor.submit(new ArteProgramIdToDatenFilmCallable(programId, LANG_CODE, SENDERNAME)));
            });
            
            CopyOnWriteArrayList<DatenFilm> finishedFilme = new CopyOnWriteArrayList<>();
            futureFilme.parallelStream().forEach(e -> {
                try{
                    DatenFilm finishedFilm = e.get();
                    if(finishedFilm!=null)
                    {
                        finishedFilme.add(finishedFilm);
                    }
                }catch(Exception exception)
                {
                    LOG.error("Es ist ein Fehler beim lesen der Arte Filme aufgetreten.",exception);
                }

                });

            listeFilme.addAll(finishedFilme);
            return listeFilme;
        }
        
        private ArteCategoryFilmsDTO loadSubCategoryPage(Gson gson, String aUrl) {
            MVHttpClient mvhttpClient = MVHttpClient.getInstance();
            OkHttpClient httpClient = mvhttpClient.getHttpClient();
            Request request = new Request.Builder()
                    .addHeader(AUTH_HEADER, AUTH_TOKEN)
                    .url(aUrl).build();
            
            ArteCategoryFilmsDTO dto = null;
            
             try
             {
                 Response response = httpClient.newCall(request).execute();

                 if(response.isSuccessful())
                 {
                     // erste Seite lesen
                     dto = gson.fromJson(response.body().string(), ArteCategoryFilmsDTO.class);
                 }

             }catch (IOException ioException)
             {
                LOG.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.",ioException);
             }    
             
             return dto;
        }
    }
}
