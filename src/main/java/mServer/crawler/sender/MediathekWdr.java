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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import java.io.IOException;
import java.util.List;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.wdr.WdrLetterPageDeserializer;
import mServer.crawler.sender.wdr.WdrSendungDayDeserializer;
import mServer.crawler.sender.wdr.WdrSendungOverviewDeserializer;
import mServer.crawler.sender.wdr.WdrSendungDto;
import mServer.crawler.sender.wdr.WdrVideoDetailsDeserializer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MediathekWdr extends MediathekReader {

    public final static String SENDERNAME = Const.WDR;
    private final static String ROCKPALAST_URL = "http://www1.wdr.de/fernsehen/rockpalast/startseite/index.html";
    private final static String ROCKPALAST_FESTIVAL = "http://www1.wdr.de/fernsehen/rockpalast/events/index.html";
    private final static String MAUS = "http://www.wdrmaus.de/lachgeschichten/spots.php5";
    
    private final ArrayList<String> listeFestival = new ArrayList<>();
    private final ArrayList<String> listeRochpalast = new ArrayList<>();
    private final ArrayList<String> listeMaus = new ArrayList<>();
    private final LinkedList<String> listeTage = new LinkedList<>();
    private MSStringBuilder seite_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekWdr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 100, startPrio);
    }

    //===================================
    // public
    //===================================
    @Override
    public synchronized void addToList() {
        //Theman suchen
        listeThemen.clear();
        listeTage.clear();
        listeFestival.clear();
        listeRochpalast.clear();
        listeMaus.clear();
        meldungStart();
        addSendungBuchstabe();
        addTage();
        
        if (CrawlerTool.loadLongMax()) {
            maus();
            rockpalast();
            festival();
            // damit sie auch gestartet werden (im idealfall in unterschiedlichen Threads
            String[] add = new String[]{ROCKPALAST_URL, "Rockpalast"};
            listeThemen.addUrl(add);
            // TODO das funktioniert noch nicht!!!
            //String[] add = new String[]{ROCKPALAST_FESTIVAL, "Rockpalast"};
            //listeThemen.addUrl(add);
            add = new String[]{MAUS, "Maus"};
            listeThemen.addUrl(add);
        }

        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty() && listeTage.isEmpty() && listeFestival.isEmpty() && listeRochpalast.isEmpty() && listeMaus.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeTage.size() + listeFestival.size() + listeRochpalast.size() + listeMaus.size());
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
    private void rockpalast() {
        try {
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite_1 = getUrlIo.getUri(SENDERNAME, ROCKPALAST_URL, StandardCharsets.UTF_8, 3 /* versuche */, seite_1, "");
            seite_1.extractList("", "", "<a href=\"/mediathek/video", "\"", "http://www1.wdr.de/mediathek/video/", listeRochpalast);
        } catch (Exception ex) {
            Log.errorLog(915423698, ex);
        }
    }

    private void maus() {
        // http://www.wdrmaus.de/lachgeschichten/mausspots/achterbahn.php5
        final String ROOTADR = "http://www.wdrmaus.de/lachgeschichten/";
        final String ITEM_1 = "<li class=\"filmvorschau\"><a href=\"../lachgeschichten/";
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri(SENDERNAME, MAUS, StandardCharsets.UTF_8, 3 /* versuche */, seite_1, "");
        try {
            seite_1.extractList("", "", ITEM_1, "\"", ROOTADR, listeMaus);
        } catch (Exception ex) {
            Log.errorLog(975456987, ex);
        }
    }

    private void festival() {
        // http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/rockpalastvideos_festivals100.html
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri(SENDERNAME, ROCKPALAST_FESTIVAL, StandardCharsets.UTF_8, 3 /* versuche */, seite_1, "");
        try {
            seite_1.extractList("", "", "<a href=\"/fernsehen/rockpalast/events/", "\"", "http://www1.wdr.de/fernsehen/rockpalast/events/", listeFestival);
        } catch (Exception ex) {
            Log.errorLog(432365698, ex);
        }
    }

    private void addTage() {
        // Sendung verpasst, da sind einige die nicht in einer "Sendung" enthalten sind
        // URLs nach dem Muster bauen:
        // http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-27022016.html
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String tag;
        for (int i = 0; i < 14; ++i) {
            final String URL = "http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-";
            tag = formatter.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            String urlString = URL + tag + ".html";
            listeTage.add(urlString);
        }
    }

    private void addSendungBuchstabe() {
        // http://www1.wdr.de/mediathek/video/sendungen/abisz-b100.html
        //Theman suchen
        final String URL = "http://www1.wdr.de/mediathek/video/sendungen-a-z/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen-a-z/";
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite_1 = getUrlIo.getUri_Iso(SENDERNAME, URL, seite_1, "");
        int pos1;
        int pos2;
        String url;
        listeThemen.addUrl(new String[]{URL, ""}); // ist die erste Seite: "a"
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
                    listeThemen.addUrl(new String[]{url, ""});
                }
            }
        }
    }

    private class ThemaLaden extends Thread {

        private MSStringBuilder sendungsSeite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        
        private final WdrSendungDayDeserializer dayDeserializer = new WdrSendungDayDeserializer();
        private final WdrSendungOverviewDeserializer overviewDeserializer = new WdrSendungOverviewDeserializer();
        private final WdrLetterPageDeserializer letterPageDeserializer = new WdrLetterPageDeserializer();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    if (null != link[0]) {
                        switch (link[0]) {
                            case ROCKPALAST_URL:
                                themenSeiteRockpalast();
                                break;
                            case ROCKPALAST_FESTIVAL:
                                themenSeiteFestival();
                                break;
                            case MAUS:
                                addFilmeMaus();
                                break;
                            default:
                                parseLetterPage(link[0] /* url */);
                                break;
                        }
                    }
                    meldungProgress(link[0]);
                }
                String url;
                
                while (!Config.getStop() && (url = getListeTage()) != null) {
                    meldungProgress(url);
                    parseDayPage(url);
                }

            } catch (Exception ex) {
                Log.errorLog(633250489, ex);
            }
            meldungThreadUndFertig();
        }
        
        private void parseDayPage(String url) {
            meldung(url);
            
            try {
                Document filmDocument = Jsoup.connect(url).get();
                List<WdrSendungDto> dtos = dayDeserializer.deserialize(filmDocument);
                parse(dtos, false);
                
            } catch(IOException ex) {
                Log.errorLog(763299001, ex);
            }            
        }

        private void parseLetterPage(String strUrl) {
            meldung(strUrl);
            
            if(Config.getStop()) {
                return;
            }                
            
            try {
                Document document = Jsoup.connect(strUrl).get();
                List<WdrSendungDto> sendungen = letterPageDeserializer.deserialize(document);
                parse(sendungen, false);
                
            } catch(IOException ex) {
                Log.errorLog(763299002, ex);
            }
        }

        private void parse(List<WdrSendungDto> sendungen, boolean recursive) {
            sendungen.forEach(sendung -> {
                parse(sendung, recursive);
            });            
        }
        
        private void parse(WdrSendungDto sendung, boolean recursive) {
            if (!Config.getStop()) {
                sendung.getOverviewUrls().forEach(url -> {
                    if(isUrlRelevant(url) && !recursive) {
                        parseSendungOverviewPage(url, sendung.getTheme(), true);
                    }
                });

                sendung.getVideoUrls().forEach(url -> {
                    if(isUrlRelevant(url)) {
                        parseFilmPage(url, sendung.getTheme());
                    }
                });
            }            
        }
        
        private void parseSendungOverviewPage(String strUrl, String parentTheme, boolean recursive) {
            if(!isUrlRelevant(strUrl)) {
                return;
            }
            
            try {
                Document filmDocument = Jsoup.connect(strUrl).get();
                WdrSendungDto dto = overviewDeserializer.deserialize(filmDocument);
                dto.setTheme(parentTheme);
                
                parse(dto, recursive);
            } catch(StackOverflowError err) {
                return;
            } catch(IOException ex) {
                Log.errorLog(763299001, ex);
            }
            
        }

        /***
         * Filtert URLs heraus, die nicht durchsucht werden sollen
         * Hintergrund: diese URLs verweisen auf andere und führen bei der Suche
         * im Rahmen der Rekursion zu endlosen Suchen
         * @param url zu prüfende URL
         * @return true, wenn die URL verarbeitet werden soll, sonst false
         */
        private boolean isUrlRelevant(String url) {
            // die Indexseite der Lokalzeit herausfiltern, da alle Beiträge
            // um die Lokalzeitenseiten der entsprechenden Regionen gefunden werden
            if(url.endsWith("lokalzeit/index.html")) {
                return false;
            } else if(url.contains("wdr.de/hilfe")) {
                return false;
            }
            
            return true;
        }

        private void parseFilmPage(String filmWebsite, String theme) {
            meldung(filmWebsite);
            
            if(Config.getStop()) {
                return;
            }
            
            try {
                Document filmDocument = Jsoup.connect(filmWebsite).get();
                WdrVideoDetailsDeserializer deserializer = new WdrVideoDetailsDeserializer();
                DatenFilm film = deserializer.deserialize(theme, filmDocument);
                
                if (film != null) {
                    addFilm(film);
                }
            } catch(IOException ex) {
                Log.errorLog(763299001, ex);
            }
        }
 
        private void themenSeiteRockpalast() {
            try {
                for (String urlRock : listeRochpalast) {
                    meldungProgress(urlRock);
                    if (Config.getStop()) {
                        break;
                    }
                    
                    parseFilmPage(urlRock, "Rockpalast");
                }
            } catch (Exception ex) {
                Log.errorLog(696963025, ex);
            }
        }

        private void themenSeiteFestival() {
            try {
                for (String urlRock : listeFestival) {
                    meldungProgress(urlRock);
                    if (Config.getStop()) {
                        break;
                    }
                    parseFilmPage(urlRock, "Rockpalast - Festival");
                }
            } catch (Exception ex) {
                Log.errorLog(915263698, ex);
            }
        }

        private void addFilmeMaus() {
            try {
                for (String filmWebsite : listeMaus) {
                    meldungProgress(filmWebsite);
                    if (Config.getStop()) {
                        break;
                    }
                    final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                    sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite1, "");

                    String titel = sendungsSeite1.extract("<title>", "<"); //<title>Achterbahn - MausSpots - Lachgeschichten - Die Seite mit der Maus - WDR Fernsehen</title>
                    titel = titel.replace("\n", "");
                    if (titel.contains("-")) {
                        titel = titel.substring(0, titel.indexOf('-')).trim();
                    }
                    String jsUrl = sendungsSeite1.extract("'mediaObj': { 'url': '", "'");
//                    addFilm2(filmWebsite, "MausSpots", titel, jsUrl, 0, "", "");
                }
            } catch (Exception ex) {
                Log.errorLog(915263698, ex);
            }
        }

    }

    private synchronized String getListeTage() {
        return listeTage.pollFirst();
    }

}
