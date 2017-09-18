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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.tool.MserverDaten;

public class MediathekKika extends MediathekReader {

    public final static String SENDERNAME = Const.KIKA;
    private final HashSetUrl listeAllVideos = new HashSetUrl();
    private MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekKika(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, 16, /* urlWarten */ 50, startPrio);
        setName("MediathekKiKa");
    }

    @Override
    protected void addToList() {

        meldungStart();
        if (CrawlerTool.loadLongMax()) {
            addToListNormal();
        }
        addToListAllVideo();

        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty() && listeAllVideos.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            // dann den Sender aus der alten Liste löschen
            // URLs laufen nur begrenzte Zeit
            // delSenderInAlterListe(SENDERNAME); brauchts wohl nicht mehr
            meldungAddMax(listeThemen.size() + listeAllVideos.size());
            for (int t = 0; t <= getMaxThreadLaufen(); ++t) {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void addToListNormal() {
        EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa.addToListNormal");

        final String ADRESSE = "http://www.kika.de/sendungen/sendungenabisz100.html";
        final String MUSTER_URL = "<a href=\"/sendungen/sendungenabisz100_";
        ArrayList<String> liste1 = new ArrayList<>();
        ArrayList<String> liste2 = new ArrayList<>();

        listeThemen.clear();
        try {
            GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            seite = getUrl.getUri(SENDERNAME, ADRESSE, StandardCharsets.UTF_8, 3, seite, "KiKA: Startseite");
            seite.extractList("", "", MUSTER_URL, "\"", "http://www.kika.de/sendungen/sendungenabisz100_", liste1);

            for (String s : liste1) {
                seite = getUrl.getUri_Utf(getSendername(), s, seite, "KiKa-Sendungen");
                final String MUSTER_SENDUNGEN_1 = "<h4 class=\"headline\">";
                final String MUSTER_SENDUNGEN_2 = "<a href=\"/";
                seite.extractList("", "<!--The bottom navigation -->", MUSTER_SENDUNGEN_1, MUSTER_SENDUNGEN_2, "\"", "http://www.kika.de/", liste2);
            }

            for (String ss : liste2) {
                listeThemen.add(new String[]{ss});
            }
        } catch (Exception ex) {
            Log.errorLog(302025469, ex);
        }
        performancePoint.collect();
    }

    private void addToListAllVideo() {
        EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa.addToListAllVideo");

        final String ADRESSE = "http://www.kika.de/videos/allevideos/allevideos-buendelgruppen100.html";
        final String MUSTER_URL = "<a href=\"/videos/allevideos/allevideos-buendelgruppen100_page-";
        ArrayList<String> liste1 = new ArrayList<>();
        ArrayList<String> liste2 = new ArrayList<>();

        try {
            GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            seite = getUrl.getUri(SENDERNAME, ADRESSE, StandardCharsets.UTF_8, 3, seite, "KiKA: Startseite alle Videos");
            seite.extractList("", "", MUSTER_URL, "\"", "http://www.kika.de/videos/allevideos/allevideos-buendelgruppen100_page-", liste1);
            for (String s1 : liste1) {
                seite = getUrl.getUri_Utf(getSendername(), s1, seite, "KiKa-Sendungen");
                seite.extractList("", "", "<div class=\"media mediaA\">\n<a href=\"/", "\"", "http://www.kika.de/", liste2);
            }
            for (String s2 : liste2) {
                listeAllVideos.add(new String[]{s2});
            }
        } catch (Exception ex) {
            Log.errorLog(732120256, ex);
        }
        performancePoint.collect();
    }

    private class ThemaLaden extends Thread {
        private final ArrayList<String> liste1 = new ArrayList<>();
        private final ArrayList<String> liste2 = new ArrayList<>();
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa::ThemaLaden.run");

            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeAllVideos.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    loadAllVideo_1(link[0] /* url */);
                }
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    ladenSerien_1(link[0] /* url */);
                }
            } catch (Exception ex) {
                Log.errorLog(915236791, ex);
            }
            meldungThreadUndFertig();
            performancePoint.collect();
        }

        private void ladenSerien_1(String filmWebsite) {
            EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa::ThemaLaden.ladenSerien_1");

            try {
                liste1.clear();
                liste2.clear();
                GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrl.getUri(SENDERNAME, filmWebsite, StandardCharsets.UTF_8, 1, seite1, "Themenseite");
                String thema = seite1.extract("<title>", "<");
                thema = thema.replace("KiKA -", "").trim();

                String url = "";
                if (url.isEmpty()) {
                    url = seite1.extract("<h2 class=\"conHeadline\">Alle Folgen</h2>", "<a href=\"", "\"");
                }
                if (url.isEmpty()) {
                    url = seite1.extract("<h2 class=\"conHeadline\">Alle Sendungen</h2>", "<a href=\"", "\"");
                }
                if (url.isEmpty()) {
                    int p = seite1.indexOf("<h2 class=\"conHeadline\">Nächste Folge</h2>");
                    if (p <= 0) {
                        p = 0;
                    }
                    url = seite1.extract("<span class=\"moreBtn\">", "<a href=\"", "\"", p, 0, "");
                }
                if (url.isEmpty()) {
                    Log.errorLog(721356987, "keine URL: " + filmWebsite);
                    return;
                } else {
                    if (!url.startsWith("http://www.kika.de")) {
                        url = "http://www.kika.de" + url;
                    }
                    seite1 = getUrl.getUri(SENDERNAME, url, StandardCharsets.UTF_8, 1, seite1, "Themenseite");
                    seite1.extractList("", "<!--The bottom navigation -->", "<div class=\"shortInfos\">", "<a href=\"", "\"", "http://www.kika.de", liste1);

                    seite1.extractList("", "", "<div class=\"bundleNaviItem \">", "<a href=\"", "\"", "http://www.kika.de", liste2);
                    for (String s : liste2) {
                        seite1 = getUrl.getUri(SENDERNAME, s, StandardCharsets.UTF_8, 1, seite1, "Themenseite");
                        seite1.extractList("", "<!--The bottom navigation -->", "<div class=\"shortInfos\">", "<a href=\"", "\"", "http://www.kika.de", liste1);
                    }
                    if (liste1.isEmpty()) {
                        Log.errorLog(794512630, "keine Filme: " + filmWebsite);
                        return;
                    }
                    int count = 0;
                    int err = 0;
                    for (int i = (liste1.size() - 1); i >= 0; --i) {
                        // die jüngsten Beiträge sind am Ende
                        String s = liste1.get(i);
                        ++count;
                        if (!CrawlerTool.loadLongMax() && count > 4) {
                            return;
                        }
                        if (Config.getStop()) {
                            return;
                        }
                        if (!ladenSerien_2(s, thema)) {
                            //dann gibts evtl. nix mehr
                            if (!CrawlerTool.loadLongMax()) {
                                // nur beim kurzen Suchen
                                ++err;
                                if (err > 2) {
                                    //bei ein paar sind Beiträge in der Zukunft angekünndigt
                                    break;
                                }
                            }
                        } else {
                            err = 0;
                        }

                    }
                }
            } catch (Exception ex) {
                Log.errorLog(915263147, ex);
            }
            performancePoint.collect();
        }

        private boolean ladenSerien_2(String filmWebsite, String thema) {
            EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa::ThemaLaden.ladenSerien_2");

            boolean ret = false;
            try {
                meldung(filmWebsite);
                GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrl.getUri(SENDERNAME, filmWebsite, StandardCharsets.UTF_8, 1, seite1, "Themenseite");

                String xml = seite1.extract("<div class=\"av-playerContainer\"", "setup({dataURL:'", "'");
                if (!xml.isEmpty()) {
                    ret = true;
                    ladenXml(xml, thema, false /*alle*/);
                }
            } catch (Exception ex) {
                Log.errorLog(801202145, ex);
            }
            performancePoint.collect();
            return ret;
        }

        private void loadAllVideo_1(String url) {
            EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa::ThemaLaden.loadAllVideo_1");

            ArrayList<String> liste = new ArrayList<>();
            try {
                GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite2 = getUrl.getUri(getSendername(), url, StandardCharsets.UTF_8, 1, seite2, "KiKa-Sendungen");
                
                String urlPartSendung = getUrlPartSendung(url);
                loadAllVideo_2(seite2, urlPartSendung);
                if (CrawlerTool.loadLongMax()) {
                    seite2.extractList("", "", "<div class=\"bundleNaviItem active\">\n<a href=\"/videos/allevideos/", "\"", "http://www.kika.de/videos/allevideos/", liste);
                    seite2.extractList("", "", "<div class=\"bundleNaviItem \">\n<a href=\"/videos/allevideos/", "\"", "http://www.kika.de/videos/allevideos/", liste);
                }
                for (String u : liste) {
                    if (Config.getStop()) {
                        break;
                    }
                    seite2 = getUrl.getUri(getSendername(), u, StandardCharsets.UTF_8, 1, seite2, "KiKa-Sendungen");
                    loadAllVideo_2(seite2, urlPartSendung);
                }
            } catch (Exception ex) {
                Log.errorLog(825412369, ex);
            }
            performancePoint.collect();
        }
        
        /**
         * Extrahiert aus der URL den Teil, der den Sendungsnamen beinhaltet
         * Beispiel-URL: http://www.kika.de/kikaninchen/sendungen/sendung100266.html
         * Rückgabe: kikaninchen
         * @param url die URL
         * @return der Sendungsname aus der URL
         */
        private String getUrlPartSendung(String url) {
            String[] parts = url.split("/");
            String urlPartSendung = parts[3];
            return urlPartSendung;
        }

        private void loadAllVideo_2(MSStringBuilder sStringBuilder, String urlPartSendung) {
            EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa::ThemaLaden.loadAllVideo_2");

            ArrayList<String> liste = new ArrayList<>();

            try {
                String thema = sStringBuilder.extract("<h1 class=\"headline\">", "<").trim();
                if (thema.isEmpty()) {
                    thema = sStringBuilder.extract("<title>KiKA -", "<").trim();
                }

                sStringBuilder.extractList(".setup({dataURL:'", "'", liste);
                
                for (String s : liste) {
                    if (Config.getStop()) {
                        break;
                    }
                    
                    // URL nur verarbeiten, wenn diese sich auf die gleiche Sendung bezieht
                    // nötig, da teilweise Links zu anderen Trailern und Sendungen gefunden werden
                    if(!s.contains(urlPartSendung)) {
                        break;
                    }
                    ladenXml(s /* url */, thema, true /*nur neue URLs*/);
                }
            } catch (Exception ex) {
                Log.errorLog(201036987, ex);
            }
            performancePoint.collect();
        }

        private void ladenXml(String xmlWebsite, String thema, boolean urlPruefen) {
            EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("MediathekKiKa::ThemaLaden.ladenXml");

            try {
                GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite3 = getUrl.getUri(getSendername(), xmlWebsite, StandardCharsets.UTF_8, 1, seite3, "");
                if (thema.isEmpty()) {
                    thema = getSendername();
                }
                // manuelle Anpassung, Notlösung!!
                if (thema.equals("ABC-Bär")) {
                    thema = "ABC Bär";
                }

                String titel = seite3.extract("<title>", "<");
                if (titel.toLowerCase().equals(thema.toLowerCase())) {
                    titel = seite3.extract("<headline>", "<");
                }
                if (titel.toLowerCase().equals(thema.toLowerCase())) {
                    titel = seite3.extract("<topline>", "<");
                    if (titel.isEmpty()) {
                        // dann bleibts dabei
                        titel = seite3.extract("<title>", "<");
                    }
                }
                String beschreibung = seite3.extract("<broadcastDescription>", "<");
                String date = seite3.extract("<broadcastDate>", "<");
                String datum = "";
                String zeit = "";
                if (!date.isEmpty()) {
                    datum = convertDatum(date);
                    zeit = convertTime(date);
                } else {
                    date = seite3.extract("<webTime>", "<"); // <webTime>08.12.2014 13:16</webTime>
                    if (!date.isEmpty()) {
                        datum = date.substring(0, date.indexOf(' ')).trim();
                        zeit = date.substring(date.indexOf(' ')).trim() + ":00";
                    }
                }
                String urlSendung = seite3.extract("<broadcastURL>", "<");
                if (urlSendung.isEmpty()) {
                    urlSendung = seite3.extract("<htmlUrl>", "<");
                }
                long duration = 0;
                long runtime = 0;
                try {
                    //<duration>00:03:07</duration>
                    String dauer = seite3.extract("<duration>", "<");
                    if (!dauer.isEmpty()) {
                        String[] parts = dauer.split(":");
                        long power = 1;
                        for (int i = parts.length - 1; i >= 0; i--) {
                            duration += Long.parseLong(parts[i]) * power;
                            power *= 60;
                        }
                    }
                } catch (NumberFormatException ex) {
                    if (MserverDaten.debug)
                        Log.errorLog(201036547, ex, xmlWebsite);
                }
                // Film-URLs suchen
                final String MUSTER_URL_MP4 = "<progressiveDownloadUrl>";
                String urlHD = seite3.extract("| MP4 Web XL |", MUSTER_URL_MP4, "<");
                String urlMp4 = seite3.extract("| MP4 Web L |", MUSTER_URL_MP4, "<");
                if (urlMp4.isEmpty()) {
                    urlMp4 = seite3.extract("| MP4 Web L+ |", MUSTER_URL_MP4, "<");
                }
                String urlMp4_klein = seite3.extract("| MP4 Web M |", MUSTER_URL_MP4, "<");

                if (urlMp4.isEmpty()) {
                    urlMp4 = urlMp4_klein;
                    urlMp4_klein = "";
                }

                if (thema.isEmpty() || urlSendung.isEmpty() || titel.isEmpty() || urlMp4.isEmpty() || date.isEmpty() || zeit.isEmpty() || duration == 0 /*|| beschreibung.isEmpty()*/) {
                    Log.errorLog(735216987, "leer: " + xmlWebsite);
                }

                if (!urlMp4.isEmpty()) {
                    meldung(urlMp4);
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, urlMp4, ""/*rtmpUrl*/, datum, zeit, duration, beschreibung);
                    CrawlerTool.addUrlKlein(film, urlMp4_klein, "");
                    CrawlerTool.addUrlHd(film, urlHD, "");
                    addFilm(film, urlPruefen);
                } else {
                    Log.errorLog(963215478, " xml: " + xmlWebsite);
                }
            } catch (Exception ex) {
                Log.errorLog(784512365, ex);
            }
            performancePoint.collect();
        }

        private final FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        private String convertDatum(String datum) {
            //<broadcastDate>2014-12-12T09:45:00.000+0100</broadcastDate>
            try {
                FastDateFormat sdfOutDay = FastDateFormat.getInstance("dd.MM.yyyy");

                Date filmDate = sdf.parse(datum);
                datum = sdfOutDay.format(filmDate);
            } catch (ParseException ex) {
                Log.errorLog(731025789, ex, "Datum: " + datum);
            }
            return datum;
        }

        private String convertTime(String zeit) {
            //<broadcastDate>2014-12-12T09:45:00.000+0100</broadcastDate>
            try {
                FastDateFormat sdfOutTime = FastDateFormat.getInstance("HH:mm:ss");

                Date filmDate = sdf.parse(zeit);
                zeit = sdfOutTime.format(filmDate);
            } catch (ParseException ex) {
                Log.errorLog(915423687, ex, "Time: " + zeit);
            }
            return zeit;
        }
    }
}
