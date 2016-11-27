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
package mServer.mSearch.filmeSuchen.sender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import mSearch.Config;
import mSearch.Const;
import mSearch.Const.Sender;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mServer.mSearch.filmeSuchen.FilmeSuchen;
import mServer.mSearch.filmeSuchen.GetUrl;

public class MediathekKika extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Sender.KIKA.name;
    private MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    LinkedListUrl listeAllVideos = new LinkedListUrl();

    public MediathekKika(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 5, /* urlWarten */ 200, startPrio);
    }

    @Override
    void addToList() {

        meldungStart();
        if (Config.loadLongMax()) {
            addToListNormal();
        }
        addToListAllVideo();

////        new ThemaLaden().ladenSerien_1("http://www.kika.de/die-schule-der-kleinen-vampire/sendungen/sendung37314.html");
////        meldungThreadUndFertig();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0 && listeAllVideos.size() == 0) {
            meldungThreadUndFertig();
        } else {
            // dann den Sender aus der alten Liste löschen
            // URLs laufen nur begrenzte Zeit
            // delSenderInAlterListe(SENDERNAME); brauchts wohl nicht mehr
            meldungAddMax(listeThemen.size() + listeAllVideos.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    void addToListNormal() {
        final String ADRESSE = "http://www.kika.de/sendungen/sendungenabisz100.html";
        final String MUSTER_URL = "<a href=\"/sendungen/sendungenabisz100_";
        ArrayList<String> liste1 = new ArrayList<>();
        ArrayList<String> liste2 = new ArrayList<>();

        listeThemen.clear();
        try {
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE, Const.KODIERUNG_UTF, 3, seite, "KiKA: Startseite");
            seite.extractList("", "", MUSTER_URL, "\"", "http://www.kika.de/sendungen/sendungenabisz100_", liste1);

            for (String s : liste1) {
                seite = getUrlIo.getUri_Utf(sendername, s, seite, "KiKa-Sendungen");
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
    }

    void addToListAllVideo() {
        final String ADRESSE = "http://www.kika.de/videos/allevideos/allevideos-buendelgruppen100.html";
        final String MUSTER_URL = "<a href=\"/videos/allevideos/allevideos-buendelgruppen100_page-";
        ArrayList<String> liste1 = new ArrayList<>();
        ArrayList<String> liste2 = new ArrayList<>();

        try {
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE, Const.KODIERUNG_UTF, 3, seite, "KiKA: Startseite alle Videos");
            seite.extractList("", "", MUSTER_URL, "\"", "http://www.kika.de/videos/allevideos/allevideos-buendelgruppen100_page-", liste1);
            for (String s1 : liste1) {
                seite = getUrlIo.getUri_Utf(sendername, s1, seite, "KiKa-Sendungen");
                seite.extractList("", "", "<div class=\"media mediaA\">\n<a href=\"/", "\"", "http://www.kika.de/", liste2);
            }
            for (String s2 : liste2) {
                listeAllVideos.add(new String[]{s2});
            }
        } catch (Exception ex) {
            Log.errorLog(732120256, ex);
        }
    }

    private class ThemaLaden implements Runnable {

        GetUrl getUrl = new GetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//2014-12-12T09:45:00.000+0100
        private final SimpleDateFormat sdfOutTime = new SimpleDateFormat("HH:mm:ss");
        private final SimpleDateFormat sdfOutDay = new SimpleDateFormat("dd.MM.yyyy");
        private final ArrayList<String> liste1 = new ArrayList<>();
        private final ArrayList<String> liste2 = new ArrayList<>();

        @Override
        public synchronized void run() {
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
        }

        public void ladenSerien_1(String filmWebsite) {
            try {
                liste1.clear();
                liste2.clear();
                seite1 = getUrlIo.getUri(SENDERNAME, filmWebsite, Const.KODIERUNG_UTF, 1, seite1, "Themenseite");
                String thema = seite1.extract("<title>", "<");
                thema = thema.replace("KiKA -", "").trim();
//                String url = seite1.extract("<span class=\"moreBtn\">", "<a href=\"", "\"", 0, "Das könnte dir auch gefallen", "");
                //String url = seite1.extract("<span class=\"moreBtn\">", "<a href=\"", "\"");
//                if (!url.isEmpty()) {
//                    if (ladenSerien_3(thema)) {
//                        return;
//                    }
//                }
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
                    seite1 = getUrlIo.getUri(SENDERNAME, url, Const.KODIERUNG_UTF, 1, seite1, "Themenseite");
                    seite1.extractList("", "<!--The bottom navigation -->", "<div class=\"shortInfos\">", "<a href=\"", "\"", "http://www.kika.de", liste1);

                    seite1.extractList("", "", "<div class=\"bundleNaviItem \">", "<a href=\"", "\"", "http://www.kika.de", liste2);
                    for (String s : liste2) {
                        seite1 = getUrlIo.getUri(SENDERNAME, s, Const.KODIERUNG_UTF, 1, seite1, "Themenseite");
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
                        if (!Config.loadLongMax() && count > 4) {
                            return;
                        }
                        if (Config.getStop()) {
                            return;
                        }
                        if (!ladenSerien_2(s, thema)) {
                            //dann gibts evtl. nix mehr
                            if (!Config.loadLongMax()) {
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
        }

        boolean ladenSerien_2(String filmWebsite, String thema) {
            boolean ret = false;
            try {
                meldung(filmWebsite);
                seite1 = getUrlIo.getUri(SENDERNAME, filmWebsite, Const.KODIERUNG_UTF, 1, seite1, "Themenseite");

                String xml = seite1.extract("<div class=\"av-playerContainer\"", "setup({dataURL:'", "'");
                if (xml.isEmpty()) {
//                    MSLog.fehlerMeldung(701025987, "keine XML: " + filmWebsite);
                } else {
                    ret = true;
//                    xml = "http://www.kika.de" + xml;
                    ladenXml(xml, thema, false /*alle*/);
                }
            } catch (Exception ex) {
                Log.errorLog(801202145, ex);
            }
            return ret;
        }

//        boolean ladenSerien_3(String thema) {
//            boolean ret = false;
//            try {
//                liste1.clear();
//
//                seite1.extractList("", "", "setup({dataURL:'", "'", "http://www.kika.de", liste1);
//                if (liste1.isEmpty()) {
//                    MSLog.fehlerMeldung(495623014, "keine XML: ");
//                }
//                int count = 0;
//                for (String xml : liste1) {
//                    if (!MSConfig.loadLongMax() && count > 4) {
//                        break;
//                    }
//
//                    ret = true;
//                    ladenXml(xml, thema, false /*alle*/);
//                }
//            } catch (Exception ex) {
//                MSLog.fehlerMeldung(821012459, ex);
//            }
//            return ret;
//        }
        void loadAllVideo_1(String url) {
            ArrayList<String> liste = new ArrayList<>();
            try {
                seite2 = getUrlIo.getUri_Utf(sendername, url, seite2, "KiKa-Sendungen");
                loadAllVideo_2(seite2);
                if (Config.loadLongMax()) {
                    seite2.extractList("", "", "<div class=\"bundleNaviItem active\">\n<a href=\"/videos/allevideos/", "\"", "http://www.kika.de/videos/allevideos/", liste);
                    seite2.extractList("", "", "<div class=\"bundleNaviItem \">\n<a href=\"/videos/allevideos/", "\"", "http://www.kika.de/videos/allevideos/", liste);
                }
                for (String u : liste) {
                    if (Config.getStop()) {
                        break;
                    }
                    seite2 = getUrlIo.getUri_Utf(sendername, u, seite2, "KiKa-Sendungen");
                    loadAllVideo_2(seite2);
                }
            } catch (Exception ex) {
                Log.errorLog(825412369, ex);
            }
        }

        void loadAllVideo_2(MSStringBuilder sStringBuilder) {
            ArrayList<String> liste = new ArrayList<>();
            String thema;
            try {
                thema = sStringBuilder.extract("<h1 class=\"headline\">", "<").trim();
                if (thema.isEmpty()) {
                    thema = sStringBuilder.extract("<title>KiKA -", "<").trim();
                }

                sStringBuilder.extractList(".setup({dataURL:'", "'", liste);
                for (String s : liste) {
                    if (Config.getStop()) {
                        break;
                    }
                    ladenXml(s /* url */, thema, true /*nur neue URLs*/);
                }
            } catch (Exception ex) {
                Log.errorLog(201036987, ex);
            }
        }

        void ladenXml(String xmlWebsite, String thema, boolean urlPruefen) {
            try {
                seite3 = getUrlIo.getUri_Utf(sendername, xmlWebsite, seite3, "" /* Meldung */);
                if (thema.isEmpty()) {
                    thema = sendername;
                }
                // manuelle Anpassung, Notlösung!!
                if (thema.equals("ABC-Bär")){
                    thema = "ABC Bär";
                }
                //Test <channelName>ABC Bär</channelName>
//                String th = seite3.extract("<channelName>", "<");
//                String thh = seite3.extract("<broadcastName>", "<");
//                String thhh = seite3.extract("<topline>", "<");
//                if (!thhh.equals(thema)) {
//                    System.out.println(" thhh: " + thhh + " thema: " + thema + " URL: " + xmlWebsite);
//                }
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
                        datum = date.substring(0, date.indexOf(" ")).trim();
                        zeit = date.substring(date.indexOf(" ")).trim() + ":00";
                    }
                }
                String urlSendung = seite3.extract("<broadcastURL>", "<");
                if (urlSendung.isEmpty()) {
                    urlSendung = seite3.extract("<htmlUrl>", "<");
                }
                long duration = 0;
                try {
                    //<duration>00:03:07</duration>
                    String dauer = seite3.extract("<duration>", "<");
                    if (!dauer.equals("")) {
                        String[] parts = dauer.split(":");
                        long power = 1;
                        for (int i = parts.length - 1; i >= 0; i--) {
                            duration += Long.parseLong(parts[i]) * power;
                            power *= 60;
                        }
                    }
                } catch (NumberFormatException ex) {
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

                if (!urlMp4.equals("")) {
                    meldung(urlMp4);
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, urlMp4, ""/*rtmpUrl*/, datum, zeit, duration, beschreibung);
                    film.addUrlKlein(urlMp4_klein, "");
                    film.addUrlHd(urlHD, "");
                    addFilm(film, urlPruefen);
                } else {
                    Log.errorLog(963215478, " xml: " + xmlWebsite);
                }
            } catch (Exception ex) {
                Log.errorLog(784512365, ex);
            }
        }

        private String convertDatum(String datum) {
            //<broadcastDate>2014-12-12T09:45:00.000+0100</broadcastDate>
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
                Date filmDate = sdf.parse(datum);
                datum = sdfOutDay.format(filmDate);
            } catch (ParseException ex) {
                Log.errorLog(731025789, ex, "Datum: " + datum);
            }
            return datum;
        }

        private String convertTime(String zeit) {
            //<broadcastDate>2014-12-12T09:45:00.000+0100</broadcastDate>
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            try {
                Date filmDate = sdf.parse(zeit);
                zeit = sdfOutTime.format(filmDate);
            } catch (ParseException ex) {
                Log.errorLog(915423687, ex, "Time: " + zeit);
            }
            return zeit;
        }
    }

}
