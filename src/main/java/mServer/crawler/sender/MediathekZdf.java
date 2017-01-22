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

import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.sender.newsearch.DownloadDTO;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.newsearch.VideoDTO;
import mServer.crawler.sender.newsearch.ZDFSearchTask;

public class MediathekZdf extends MediathekReader implements Runnable
{

    public final static String SENDERNAME = Const.ZDF;
    public static final String URL_PATTERN_SENDUNG_VERPASST = "https://www.zdf.de/sendung-verpasst?airtimeDate=%s";
    public static final String[] KATEGORIE_ENDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0+-+9"};
    public static final String KATEGORIEN_URL_PATTERN = "https://www.zdf.de/sendungen-a-z/?group=%s";
    private final MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    LinkedListUrl listeTage = new LinkedListUrl();

    public MediathekZdf(FilmeSuchen ssearch, int startPrio)
    {
        super(ssearch, SENDERNAME, 0 /* threads */, 150 /* urlWarten */, startPrio);
    }

    @Override
    public void addToList() {
        meldungStart();
        meldungAddThread();
        
        int days = CrawlerTool.loadLongMax() ? 300 : 20;
                
        final ZDFSearchTask newTask = new ZDFSearchTask(days);
        newTask.fork();
        Collection<VideoDTO> filmList = newTask.join();
        
        // Convert new DTO to old DatenFilm class
        Log.sysLog("convert VideoDTO to DatenFilm started...");
        Collection<VideoDtoDatenFilmConverterTask> converterTasks = new ArrayList<>();
        filmList.forEach((video) -> {
            VideoDtoDatenFilmConverterTask task = new VideoDtoDatenFilmConverterTask(video);
            task.fork();
            
            converterTasks.add(task);
        });
        
        converterTasks.forEach(task -> task.join());
        Log.sysLog("convert VideoDTO to DatenFilm finished.");
        
        meldungThreadUndFertig();        
    }
    

    private class VideoDtoDatenFilmConverterTask extends RecursiveTask<Object> {

        private static final long serialVersionUID = 1L;
        
        private final VideoDTO video;        

        public VideoDtoDatenFilmConverterTask(VideoDTO aVideoDTO) {
            video = aVideoDTO;
        }

        @Override
        protected Object compute() {
            if(video != null) {
                   try {
                        DownloadDTO download = video.getDownloadDto();

                        DatenFilm film = new DatenFilm(SENDERNAME, video.getTopic(), video.getWebsiteUrl() /*urlThema*/,
                                video.getTitle(), download.getUrl(Qualities.NORMAL), "" /*urlRtmp*/,
                                video.getDate(), video.getTime(), video.getDuration(), video.getDescription());
                        urlTauschen(film, video.getWebsiteUrl(), mSearchFilmeSuchen);
                        addFilm(film);
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
                    } catch (Exception ex) {
                        Log.errorLog(496583211, ex, "add film failed: " + video.getWebsiteUrl());
                    }            
            }
            return null;
        }    
    }

    public static void urlTauschen(DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen)
    {
        // manuell die Auflösung hochsetzen

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

    private static void changeUrl(String from, String to, DatenFilm film, String urlSeite, FilmeSuchen mSFilmeSuchen)
    {
        if (film.arr[DatenFilm.FILM_URL].endsWith(from))
        {
            String url_ = film.arr[DatenFilm.FILM_URL].substring(0, film.arr[DatenFilm.FILM_URL].lastIndexOf(from)) + to;
            String l = mSFilmeSuchen.listeFilmeAlt.getFileSizeUrl(url_, film.arr[DatenFilm.FILM_SENDER]);
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
