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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringEscapeUtils;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.tool.MserverDaten;

public class MediathekSwr extends MediathekReader {

    private static final int wartenKurz = 2000;
    private static final int wartenLang = 4000;

    public final static String SENDERNAME = Const.SWR;

    public MediathekSwr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ wartenLang, startPrio);
    }

    //===================================
    // public
    //===================================
    @Override
    public synchronized void addToList() {
        meldungStart();
        //Theman suchen
        listeThemen.clear();
        addToList__();
        if (CrawlerTool.loadLongMax()) {
            addToList_verpasst(); // brauchst eigentlich nicht und dauer zu lange
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

    //===================================
    // private
    //===================================
    private void addToList__() {
        //Theman suchen
        final String MUSTER_START = "<div class=\"mediaCon\">";
        final String MUSTER_STOPP = "<h2 class=\"rasterHeadline\">OFT GESUCHT</h2>";
        final String MUSTER_URL = "<a href=\"tvshow.htm?show=";
        final String MUSTER_THEMA = "title=\"";
        MSStringBuilder strSeite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        strSeite = getUrlIo.getUriWithDelay(SENDERNAME, "http://swrmediathek.de/tvlist.htm", StandardCharsets.UTF_8, 2, strSeite, "", 4, TimeUnit.SECONDS);
        int pos = 0;
        String url;
        String thema;
        int stop = strSeite.indexOf(MUSTER_STOPP);
        while (!Config.getStop() && (pos = strSeite.indexOf(MUSTER_START, pos)) != -1) {
            if (stop > 0 && pos > stop) {
                break;
            }
            pos += MUSTER_START.length();
            url = strSeite.extract(MUSTER_URL, "\"", pos);
            thema = strSeite.extract(MUSTER_THEMA, "\"", pos);
            thema = StringEscapeUtils.unescapeHtml4(thema.trim()); //wird gleich benutzt und muss dann schon stimmen
            if (thema.isEmpty()) {
                Log.errorLog(915263078, "kein Thema");
            }
            if (url.isEmpty()) {
                Log.errorLog(163255009, "keine URL");
            } else {
                //url = url.replace("&amp;", "&");
                String[] add = new String[]{"http://swrmediathek.de/tvshow.htm?show=" + url, thema};
                listeThemen.addUrl(add);
            }
        }
    }

    private void addToList_verpasst() {
        //Theman suchen
        MSStringBuilder strSeite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        strSeite = getUrlIo.getUriWithDelay(SENDERNAME, "http://swrmediathek.de/sendungverpasst.htm", StandardCharsets.UTF_8, 2, strSeite, "", 4, TimeUnit.SECONDS);
        ArrayList<String> list = new ArrayList<>();
        strSeite.extractList("<ul class=\"progChannelList\" tabindex=\"-1\">", "<div class=\"box mediBoxBorder\"",
                "<a href=\"sendungverpasst.htm?show=&date=", "\"", "http://www.swrmediathek.de/sendungverpasst.htm?show=&date=", list);
        for (String s : list) {
            //url = url.replace("&amp;", "&");
            String[] add = new String[]{s, ""};
            listeThemen.addUrl(add);
        }
    }

    private class ThemaLaden extends Thread {

        private final GetUrl getUrlThemaLaden = new GetUrl(CrawlerTool.loadLongMax() ? wartenLang : wartenKurz);
        private MSStringBuilder strSeite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder strSeite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> gefunden = new ArrayList<>();

        public ThemaLaden() {
        }

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    themenSeitenSuchen(link[0] /* url */, link[1] /* Thema */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                Log.errorLog(739285690, ex);
            }
            meldungThreadUndFertig();
        }

        private void themenSeitenSuchen(String strUrlFeed, String thema) {
            final String MUSTER_URL = "<a href=\"/player.htm?show=";
            //strSeite1 = getUrl.getUri_Utf(nameSenderMReader, strUrlFeed, strSeite1, thema);
            strSeite1 = getUrlThemaLaden.getUriWithDelay(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 2 /* versuche */, strSeite1, thema, 4, TimeUnit.SECONDS);
            if (strSeite1.length() == 0) {
                Log.errorLog(945120365, "Seite leer: " + strUrlFeed);
                return;
            }
            meldung(strUrlFeed);
            int pos1 = 0;
            int pos2;
            String url;
            int max = 0;
            gefunden.clear();
            while (!Config.getStop() && (pos1 = strSeite1.indexOf(MUSTER_URL, pos1)) != -1) {
                if (!CrawlerTool.loadLongMax()) {
                    ++max;
                    if (max > 2) {
                        break;
                    }
                } else {
                    ++max;
                    if (max > 20) {
                        break;
                    }
                }
                pos1 += MUSTER_URL.length();
                if ((pos2 = strSeite1.indexOf("\"", pos1)) != -1) {
                    url = strSeite1.substring(pos1, pos2);
                    if (gefunden.contains(url)) {
                        if (max > 0) {
                            --max;
                        }
                        continue;
                    } else {
                        gefunden.add(url);
                    }
                    if (url.isEmpty()) {
                        Log.errorLog(875012369, "keine URL, Thema: " + thema);
                    } else {
                        url = "http://swrmediathek.de/AjaxEntry?callback=jsonp1347979401564&ekey=" + url;
                        json(strUrlFeed, thema, url);
                    }

                }
            }
            gefunden.clear();
        }

        private void json(String strUrlFeed, String thema, String urlJson) {
            //:"entry_media","attr":{"val0":"h264","val1":"3","val2":"rtmp://fc-ondemand.swr.de/a4332/e6/swr-fernsehen/landesschau-rp/aktuell/2012/11/582111.l.mp4",
            // oder
            // "entry_media":"http://mp4-download.swr.de/swr-fernsehen/zur-sache-baden-wuerttemberg/das-letzte-wort-podcast/20120913-2015.m.mp4"
            // oder
            // :"entry_media","attr":{"val0":"flashmedia","val1":"1","val2":"rtmp://fc-ondemand.swr.de/a4332/e6/swr-fernsehen/eisenbahn-romantik/381104.s.flv","val3":"rtmp://fc-ondemand.swr.de/a4332/e6/"},"sub":[]},{"name":"entry_media","attr":{"val0":"flashmedia","val1":"2","val2":"rtmp://fc-ondemand.swr.de/a4332/e6/swr-fernsehen/eisenbahn-romantik/381104.m.flv","val3":"rtmp://fc-ondemand.swr.de/a4332/e6/"},"sub":[]
            // "entry_title":"\"Troika-Tragödie - Verspielt die Regierung unser Steuergeld?\"
            try {
                strSeite2 = getUrlThemaLaden.getUriWithDelay(SENDERNAME, urlJson, StandardCharsets.UTF_8, 1, strSeite2, "", 4, TimeUnit.SECONDS);
                if (strSeite2.length() == 0) {
                    if (MserverDaten.debug)
                        Log.errorLog(912365478, "Seite leer: " + urlJson);
                    return;
                }
                String title = getTitle();
                String date = getDate();
                String time = getTime();
                String description = getDescription();
                long duration = getDuration();
                String urldHd = getHDUrl();
                String normalUrl = getNormalUrl();
                String smallUrl = getSmallUrl();
                String rtmpUrl = getRtmpUrl();
                String subtitle = strSeite2.extract("\"entry_capuri\":\"", "\"");
                if (thema.isEmpty()) {
                    thema = strSeite2.extract("\"group_title\":\"", "\"");
                }
                if (normalUrl.isEmpty() && smallUrl.isEmpty() && rtmpUrl.isEmpty()) {
                    Log.errorLog(203690478, thema + " NO normal and small url:  " + urlJson);
                } else {
                    if (normalUrl.isEmpty() && !smallUrl.isEmpty()) {
                        normalUrl = smallUrl;
                    } else if (normalUrl.isEmpty()) {
                        normalUrl = rtmpUrl;
                    }
                    if (smallUrl.isEmpty()) {
                        smallUrl = getSuperSmalUrl();
                    }
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, strUrlFeed, title, normalUrl, ""/*rtmpURL*/, date, time, duration, description);

                    if (!urldHd.isEmpty()) {
                        CrawlerTool.addUrlHd(film, urldHd, "");
                    }
                    if (!smallUrl.isEmpty()) {
                        CrawlerTool.addUrlKlein(film, smallUrl, "");
                    }
                    if (!subtitle.isEmpty()) {
                        CrawlerTool.addUrlSubtitle(film, subtitle);
                    }
                    addFilm(film);
                }
            } catch (Exception ex) {
                Log.errorLog(939584720, thema + ' ' + urlJson);
            }
        }

        final static String PATTERN_END = "\"";
        final static String HTTP = "http";

        private String getTitle() {
            final String PATTERN_TITLE_START = "\"entry_title\":\"";
            //final String MUSTER_TITEL_2 = "\"entry_title\":\"\\\"";
            return strSeite2.extract(PATTERN_TITLE_START, PATTERN_END);
        }

        private String getDescription() {
            final String PATTERN_DESCRIPTION_START = "\"entry_descl\":\"";
            return strSeite2.extract(PATTERN_DESCRIPTION_START, PATTERN_END);
        }

        private String getDate() {
            final String PATTERN_DATE_START = "\"entry_pdatehd\":\"";
            String datum = strSeite2.extract(PATTERN_DATE_START, PATTERN_END);
            if (datum.length() < 10) {
                if (datum.contains(".")) {
                    if ((datum.substring(0, datum.indexOf('.'))).length() != 2) {
                        datum = '0' + datum;
                    }
                }
                if (datum.indexOf('.') != datum.lastIndexOf('.')) {
                    if ((datum.substring(datum.indexOf('.') + 1, datum.lastIndexOf('.'))).length() != 2) {
                        datum = datum.substring(0, datum.indexOf('.') + 1) + '0' + datum.substring(datum.indexOf('.') + 1);
                    }
                }
            }
            return datum;
        }

        private long getDuration() {
            final String PATTERN_DURATION_START = "\"entry_durat\":\"";
            String dur = null;
            long duration = 0;
            try {
                dur = strSeite2.extract(PATTERN_DURATION_START, PATTERN_END);
                if (!dur.isEmpty()) {
                    String[] parts = dur.split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        duration += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            } catch (NumberFormatException ex) {
                Log.errorLog(679012497, "duration: " + (dur == null ? " " : duration));
            }
            return duration;
        }

        private String getTime() {
            final String PATTERN_TIME_START = "\"entry_pdateht\":\"";
            String time = strSeite2.extract(PATTERN_TIME_START, PATTERN_END);
            if (time.length() <= 5) {
                time = time.trim() + ":00";
            }
            time = time.replace(".", ":");
            if (time.length() < 8) {
                if (time.contains(":")) {
                    if (time.substring(0, time.indexOf(':')).length() != 2) {
                        time = '0' + time;
                    }
                }
                if (time.indexOf(':') != time.lastIndexOf(':')) {
                    if (time.substring(time.indexOf(':') + 1, time.lastIndexOf(':')).length() != 2) {
                        time = time.substring(0, time.indexOf(':') + 1) + '0' + time + time.substring(time.lastIndexOf(':'));
                    }
                }
            }
            return time;
        }

        private String getHDUrl() {
            final String PATTTERN_PROT_HTTP_HD = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"4\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_HD, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            return HTTP + urlWithOutprot;
        }

        private String getNormalUrl() {
            final String PATTTERN_PROT_HTTP_L = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"3\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_L, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            return HTTP + urlWithOutprot;
        }

        private String getRtmpUrl() {
            final String PATTERN_1 = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"3\",\"val2\":\"rtmp";
            final String PATTERN_2 = "entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"2\",\"val2\":\"rtmp";
            String urlWithOutprot = strSeite2.extract(PATTERN_1, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                urlWithOutprot = strSeite2.extract(PATTERN_2, PATTERN_END);
                if (urlWithOutprot.isEmpty()) {
                    return "";
                }
            }
            return "rtmp" + urlWithOutprot;
        }

        private String getSmallUrl() {
            final String PATTTERN_PROT_HTTP_M = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"2\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_M, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            return HTTP + urlWithOutprot;
        }

        private String getSuperSmalUrl() {
            final String PATTTERN_PROT_HTTP_S = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"1\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_S, PATTERN_END);
            return HTTP + urlWithOutprot;
        }

    }
}
