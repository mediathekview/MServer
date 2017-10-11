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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.wdr.WdrDayPageCallable;
import mServer.crawler.sender.wdr.WdrLetterPageCallable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MediathekWdr extends MediathekReader {

    public final static String SENDERNAME = Const.WDR;
    
    private final LinkedList<String> dayUrls = new LinkedList<>();
    private final LinkedList<String> letterPageUrls = new LinkedList<>();
    private MSStringBuilder seite_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    Collection<Future<ListeFilme>> futureFilme = new ArrayList<>();
    
    private static final Logger LOG = LogManager.getLogger(MediathekWdr.class);
    
    public MediathekWdr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 100, startPrio);
    }

    //===================================
    // public
    //===================================
    @Override
    public synchronized void addToList() {
        clearLists();
        meldungStart();
        fillLists();        
        
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (letterPageUrls.isEmpty()  && dayUrls.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(letterPageUrls.size() + dayUrls.size());
            
            startLetterPages();
            startDayPages();
            
            addFilms();
            
            meldungThreadUndFertig();
        }
    }
    
    private void addFilms() {
         futureFilme.forEach(e -> {
            try {
                ListeFilme filmList = e.get();
                if(filmList != null) {
                    filmList.forEach(film -> {
                        if(film != null) {
                            addFilm(film);
                        }
                    });
                }
            } catch(Exception exception)
            {
                LOG.error("Es ist ein Fehler beim lesen der WDR Filme aufgetreten.",exception);
            }
        });       
    }
    
    private void fillLists() {
        addLetterPages();
        addDayPages();
    }
    
    private void clearLists() {
        letterPageUrls.clear();
        dayUrls.clear();
    }
    
    private void startLetterPages() {
        
        letterPageUrls.forEach(url -> {
            ExecutorService executor = Executors.newCachedThreadPool();
            futureFilme.add(executor.submit(new WdrLetterPageCallable(url)));
            meldungProgress(url);
        });            
    }
    
    private void startDayPages() {
        
        dayUrls.forEach(url -> {
            ExecutorService executor = Executors.newCachedThreadPool();
            futureFilme.add(executor.submit(new WdrDayPageCallable(url)));
            meldungProgress(url);
        });            
    }

    private void addDayPages() {
        // Sendung verpasst, da sind einige die nicht in einer "Sendung" enthalten sind
        // URLs nach dem Muster bauen:
        // http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-27022016.html
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String tag;
        for (int i = 0; i < 14; ++i) {
            final String URL = "http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-";
            tag = formatter.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            String urlString = URL + tag + ".html";
            dayUrls.add(urlString);
        }
    }

    private void addLetterPages() {
        // http://www1.wdr.de/mediathek/video/sendungen/abisz-b100.html
        //Theman suchen
        final String URL = "http://www1.wdr.de/mediathek/video/sendungen-a-z/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen-a-z/";
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri_Iso(SENDERNAME, URL, seite_1, "");
        int pos1;
        int pos2;
        String url;
        letterPageUrls.add(URL); // ist die erste Seite: "a"
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
                    letterPageUrls.add(url);
                }
            }
        }
    }
}
