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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.dw.DwVideoDTO;
import mServer.crawler.sender.dw.DwVideoDeserializer;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.tool.MserverDaten;

public class MediathekDw extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.DW;
    private final static String ADDURL = "http://tv-download.dw.de/dwtv_video/flv/";
    private final static String HTTP = "http";
    private final static String URL_VIDEO_JSON = "http://www.dw.com/playersources/v-";
    
    public MediathekDw(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 4, /* urlWarten */ 200, startPrio);
    }

    @Override
    protected void addToList() {
        listeThemen.clear();
        meldungStart();
        sendungenLaden();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            listeSort(listeThemen, 1);
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void sendungenLaden() {
        final String ADRESSE = "http://www.dw.com/de/media-center/alle-inhalte/s-100814";
        final String MUSTER_URL = "value=\"";
        final String MUSTER_START = "<div class=\"label\">Sendungen</div>";
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri_Utf(SENDERNAME, ADRESSE, seite, "");
        int pos1, pos2;
        String url = "", thema = "";
        pos1 = seite.indexOf(MUSTER_START);
        if (pos1 == -1) {
            Log.errorLog(915230456, "Nichts gefunden");
            return;
        }
        pos1 += MUSTER_START.length();
        int stop = seite.indexOf("</div>", pos1);
        while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
            if (pos1 > stop) {
                break;
            }
            try {
                pos1 += MUSTER_URL.length();
                if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (url.isEmpty()) {
                    continue;
                }
                if (CrawlerTool.loadLongMax()) {
                    //http://www.dw.com/de/media-center/alle-inhalte/s-100814/filter/programs/3204/sort/date/results/16/
                    //http://www.dw.com/de/media-center/alle-inhalte/s-100814?filter=&programs=17274211&sort=date&results=36
                    url = "http://www.dw.com/de/media-center/alle-inhalte/s-100814?filter=&programs=" + url + "&sort=date&results=100";
                } else {
                    url = "http://www.dw.com/de/media-center/alle-inhalte/s-100814?filter=&programs=" + url + "&sort=date&results=20";
                }
                if ((pos1 = seite.indexOf(">", pos1)) != -1) {
                    pos1 += 1;
                    if ((pos2 = seite.indexOf("<", pos1)) != -1) {
                        thema = seite.substring(pos1, pos2);
                    }
                }
                // in die Liste eintragen
                String[] add = new String[]{url, thema};
                listeThemen.addUrl(add);
            } catch (Exception ex) {
                Log.errorLog(731245970, ex);
            }
        }

    }

    private class ThemaLaden extends Thread {

        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> listUrl = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1] /* Thema */);
                }
            } catch (Exception ex) {
                Log.errorLog(915423640, ex);
            }
            meldungThreadUndFertig();
        }

        private void laden(String urlThema, String thema) {

            final String MUSTER_START = "<div class=\"news searchres hov\">";
            String urlSendung;
            meldung(urlThema);
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite1 = getUrlIo.getUri_Utf(SENDERNAME, urlThema, seite1, "");
            int pos1 = 0;
            String titel;
            while (!Config.getStop() && (pos1 = seite1.indexOf(MUSTER_START, pos1)) != -1) {
                pos1 += MUSTER_START.length();
                urlSendung = seite1.extract("<a href=\"", "\"", pos1);
                titel = seite1.extract("<h2>", "<", pos1).trim();
                if (!urlSendung.isEmpty()) {
                    laden2(urlThema, thema, titel, "http://www.dw.com" + urlSendung);
                }
            }
        }

        private void laden2(String urlThema, String thema, String titel, String urlSendung) {
            String url = "", urlLow = "", urlHd = "";
            meldung(urlThema);
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite2 = getUrlIo.getUri_Utf(SENDERNAME, urlSendung, seite2, "");

            seite2.extractList("%22file%22%3A%22", "%22%7D", listUrl);
            if (listUrl.isEmpty()) {
               seite2.extractList("name=\"file_name\" value=\"", "\"", listUrl); 
            }
            for (String u : listUrl) {
                u = u.replace("%2F", "/");
                if (urlLow.isEmpty() && u.endsWith("vp6.flv")) {
                    urlLow = addUrlPrefixIfNecessary(u);
                }
                if (url.isEmpty() && u.endsWith("sor.mp4")) {
                    url = addUrlPrefixIfNecessary(u);
                }
                if (urlHd.isEmpty() && u.endsWith("avc.mp4")) {
                    urlHd = addUrlPrefixIfNecessary(u);
                }
            }
            listUrl.clear();
            
            if (url.isEmpty()) {
                // wenn keine URL vorhanden ist, 
                // dann URLs ermitteln über Anfrage nach playersourcen 
                String id = seite2.extract("<input type=\"hidden\" name=\"media_id\" value=\"", "\"");
                
                GetUrl getUrlVideo = new GetUrl(getWartenSeiteLaden());
                MSStringBuilder seiteVideo = new MSStringBuilder();
                seiteVideo = getUrlVideo.getUri_Utf(SENDERNAME, URL_VIDEO_JSON + id, seiteVideo, "");

                if (seiteVideo.length() > 0) {
                    Gson gson = new GsonBuilder().registerTypeAdapter(DwVideoDTO.class,new DwVideoDeserializer()).create();
                    DwVideoDTO dto = gson.fromJson(seiteVideo.substring(0), DwVideoDTO.class); 
                    url = dto.getUrl(Qualities.NORMAL);
                    urlHd = dto.getUrl(Qualities.HD);
                }
            }

            String description = seite2.extract("<meta name=\"description\" content=\"", "\"");
            String datum = seite2.extract("<strong>Datum</strong>", "</li>").trim();
            String dur = seite2.extract("<strong>Dauer</strong>", "Min.").trim();
            dur = dur.replace("\n", "");
            dur = dur.replace("\r", "");
            long duration = 0;
            try {
                if (!dur.isEmpty()) {
                    String[] parts = dur.split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        duration += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            } catch (NumberFormatException ex) {
                if (MserverDaten.debug)
                    Log.errorLog(912034567, "duration: " + dur);
            } catch (Exception ex) {
                Log.errorLog(912034567, "duration: " + dur);
            }

            if (url.isEmpty() && !urlLow.isEmpty()) {
                url = urlLow;
                urlLow = "";
            }
            if (url.isEmpty() && !urlHd.isEmpty()) {
                url = urlHd;
                urlHd = "";
            }
            if (url.isEmpty()) {
                if (MserverDaten.debug)
                    Log.errorLog(643230120, "empty URL: " + urlSendung);
            } else {
                DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, "", datum, ""/*Zeit*/, duration, description);
                if (!urlLow.isEmpty()) {
                    CrawlerTool.addUrlKlein(film, urlLow, "");
                }
                if (!urlHd.isEmpty()) {
                    CrawlerTool.addUrlHd(film, urlHd, "");
                }
                addFilm(film);
            }

        }        
        
        private String addUrlPrefixIfNecessary(String url) {
            if (!url.startsWith(HTTP)) {
                return ADDURL + url;
            }            
            return url;
        }    
    }   
}
