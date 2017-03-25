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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.newsearch.DownloadDTO;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.newsearch.VideoDTO;
import mServer.crawler.sender.newsearch.ZDFSearchTask;
import mServer.crawler.sender.newsearch.ZdfDatenFilm;

public class MediathekZdf extends MediathekReader
{

    public final static String SENDERNAME = Const.ZDF;
    //    public static final String URL_PATTERN_SENDUNG_VERPASST = "https://www.zdf.de/sendung-verpasst?airtimeDate=%s";
//    public static final String[] KATEGORIE_ENDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0+-+9"};
//    public static final String KATEGORIEN_URL_PATTERN = "https://www.zdf.de/sendungen-a-z/?group=%s";
    private final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4);
//    private final MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekZdf(FilmeSuchen ssearch, int startPrio)
    {
        super(ssearch, SENDERNAME, 0 /* threads */, 150 /* urlWarten */, startPrio);
        setName("MediathekZdf");
    }

    private final Phaser phaser = new Phaser();

    @Override
    public void addToList() {
        meldungStart();
        meldungAddThread();
        
        int days = CrawlerTool.loadLongMax() ? 300 : 20;
                
        final ZDFSearchTask newTask = new ZDFSearchTask(days);
        forkJoinPool.execute(newTask);
        Collection<VideoDTO> filmList = newTask.join();
        System.out.println("VIDEO LIST SIZE: " + filmList.size());
        // Convert new DTO to old DatenFilm class
        Log.sysLog("convert VideoDTO to DatenFilm started...");

        EtmPoint perfPoint = EtmManager.getEtmMonitor().createPoint("MediathekZdf.convertVideoDTO");

        filmList.parallelStream().forEach((video) -> {
            VideoDtoDatenFilmConverterAction action = new VideoDtoDatenFilmConverterAction(video);
            forkJoinPool.execute(action);
        });

        filmList.clear();

        boolean wasInterrupted = false;
        while (!phaser.isTerminated()) {
            try {
                if (Config.getStop()) {
                    wasInterrupted = true;
                    shutdownAndAwaitTermination(forkJoinPool, 5, TimeUnit.SECONDS);
                } else
                    TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

        //explicitely shutdown the pool
        shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);

        perfPoint.collect();
        if (wasInterrupted)
            Log.sysLog("VideoDTO conversion interrupted.");
        else
            Log.sysLog("convert VideoDTO to DatenFilm finished.");

        meldungThreadUndFertig();
    }

    void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(delay, delayUnit)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(delay, delayUnit))
                    Log.sysLog("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("serial")
    private class VideoDtoDatenFilmConverterAction extends RecursiveAction {
        private final VideoDTO video;

        public VideoDtoDatenFilmConverterAction(VideoDTO aVideoDTO) {
            video = aVideoDTO;
            phaser.register();
        }

        @Override
        protected void compute() {
            if(video != null) {
                   try {
                        DownloadDTO download = video.getDownloadDto();

                        DatenFilm film = new ZdfDatenFilm(SENDERNAME, video.getTopic(), video.getWebsiteUrl() /*urlThema*/,
                                video.getTitle(), download.getUrl(Qualities.NORMAL), "" /*urlRtmp*/,
                                video.getDate(), video.getTime(), video.getDuration(), video.getDescription());
                        urlTauschen(film, video.getWebsiteUrl(), mlibFilmeSuchen);

                       //don´t use addFilm here
                       if (mlibFilmeSuchen.listeFilmeNeu.addFilmVomSender(film)) {
                           // dann ist er neu
                           FilmeSuchen.listeSenderLaufen.inc(film.arr[DatenFilm.FILM_SENDER], RunSender.Count.FILME);
                       }

                        if (!download.getUrl(Qualities.HD).isEmpty())
                        {
                            CrawlerTool.addUrlHd(film, download.getUrl(Qualities.HD), "");
                        }
                        if (!download.getUrl(Qualities.SMALL).isEmpty())
                        {
                            CrawlerTool.addUrlKlein(film, download.getUrl(Qualities.SMALL), "");
                        }
                        if (!download.getSubTitleUrl().isEmpty())
                        {
                            CrawlerTool.addUrlSubtitle(film, download.getSubTitleUrl());
                        }      
                        if(download.getGeoLocation() != GeoLocations.GEO_NONE) {
                            film.arr[DatenFilm.FILM_GEO] = download.getGeoLocation().getDescription();
                        }
                    } catch (Exception ex) {
                        Log.errorLog(496583211, ex, "add film failed: " + video.getWebsiteUrl());
                    }            
            }
            phaser.arrive();
        }    
    }

    private static void updateHdStatus(DatenFilm film, String urlSeite) {
        // manuell die Auflösung für HD setzen, 2 Versuche
        updateHd("1456k_p13v12.mp4", "3328k_p36v12.mp4", film, urlSeite);
        updateHd("2256k_p14v12.mp4", "3328k_p36v12.mp4", film, urlSeite);
        updateHd("2328k_p35v12.mp4", "3328k_p36v12.mp4", film, urlSeite);

        updateHd("1456k_p13v12.mp4", "3256k_p15v12.mp4", film, urlSeite);
        updateHd("2256k_p14v12.mp4", "3256k_p15v12.mp4", film, urlSeite);
        updateHd("2328k_p35v12.mp4", "3256k_p15v12.mp4", film, urlSeite);

        updateHd("1496k_p13v13.mp4", "3296k_p15v13.mp4", film, urlSeite);
        updateHd("2296k_p14v13.mp4", "3296k_p15v13.mp4", film, urlSeite);
        updateHd("2328k_p35v13.mp4", "3296k_p15v13.mp4", film, urlSeite);

        updateHd("1496k_p13v13.mp4", "3328k_p36v13.mp4", film, urlSeite);
        updateHd("2296k_p14v13.mp4", "3328k_p36v13.mp4", film, urlSeite);
        updateHd("2328k_p35v13.mp4", "3328k_p36v13.mp4", film, urlSeite);
    }

    private static void modifyUrl(DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen) {
        //große URL verbessern
        changeUrl("2256k_p14v11.mp4", "2328k_p35v11.mp4", film, urlSeite, mSFilmeSuchen);
        changeUrl("2256k_p14v12.mp4", "2328k_p35v12.mp4", film, urlSeite, mSFilmeSuchen);
        changeUrl("2296k_p14v13.mp4", "2328k_p35v13.mp4", film, urlSeite, mSFilmeSuchen);

        //klein nach groß
        changeUrl("1456k_p13v11.mp4", "2328k_p35v11.mp4", film, urlSeite, mSFilmeSuchen);
        changeUrl("1456k_p13v11.mp4", "2256k_p14v11.mp4", film, urlSeite, mSFilmeSuchen); //wenns nicht geht, dann vielleicht so

        changeUrl("1456k_p13v12.mp4", "2328k_p35v12.mp4", film, urlSeite, mSFilmeSuchen);
        changeUrl("1456k_p13v12.mp4", "2256k_p14v12.mp4", film, urlSeite, mSFilmeSuchen); //wenns nicht geht, dann vielleicht so

        changeUrl("1496k_p13v13.mp4", "2328k_p35v13.mp4", film, urlSeite, mSFilmeSuchen);
        changeUrl("1496k_p13v13.mp4", "2296k_p14v13.mp4", film, urlSeite, mSFilmeSuchen); //wenns nicht geht, dann vielleicht so
    }

    public static void urlTauschen(DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen) {
        modifyUrl(film, urlSeite, mSFilmeSuchen);
        updateHdStatus(film, urlSeite);
    }

    private static void changeUrl(String from, String to, DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen)
    {
        if (film.arr[DatenFilm.FILM_URL].endsWith(from))
        {
            String url_ = film.arr[DatenFilm.FILM_URL].substring(0, film.arr[DatenFilm.FILM_URL].lastIndexOf(from)) + to;
            String l = mSFilmeSuchen.listeFilmeAlt.getFileSizeUrl(url_);
            // zum Testen immer machen!!
            if (!l.isEmpty())
            {
                film.arr[DatenFilm.FILM_GROESSE] = l;
                film.arr[DatenFilm.FILM_URL] = url_;
            } else if (urlExists(url_))
            {
                // dann wars wohl nur ein "403er"
                film.arr[DatenFilm.FILM_URL] = url_;
            } else
            {
                Log.errorLog(945120369, "urlTauschen: " + urlSeite);
            }
        }
    }

    private static void updateHd(String from, String to, DatenFilm film, String urlSeite)
    {
        if (film.arr[DatenFilm.FILM_URL_HD].isEmpty() && film.arr[DatenFilm.FILM_URL].endsWith(from))
        {
            String url_ = film.arr[DatenFilm.FILM_URL].substring(0, film.arr[DatenFilm.FILM_URL].lastIndexOf(from)) + to;
            // zum Testen immer machen!!
            if (urlExists(url_))
            {
                CrawlerTool.addUrlHd(film, url_, "");
            } else
            {
                Log.errorLog(945120147, "urlTauschen: " + urlSeite);
            }
        }
    }
}
