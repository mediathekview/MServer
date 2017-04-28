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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

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

public class MediathekBr extends MediathekReader {

    public final static String SENDERNAME = Const.BR;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.ENGLISH);//08.11.2013, 18:00
    private final SimpleDateFormat sdfOutTime = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat sdfOutDay = new SimpleDateFormat("dd.MM.yyyy");
    private final LinkedListUrl listeTage = new LinkedListUrl();
    private static final String PATTERN_VERY_SMALL = "<asset type=\"STANDARD\">";
    private static final String PATTERN_SMALL = "<asset type=\"LARGE\">";
    private static final String PATTERN_NORMAL = "<asset type=\"PREMIUM\">";
    private static final String PATTERN_HD = "<asset type=\"HD\">";
    private static final String PATTERN_DLURL = "<downloadUrl>";
    private static final String PATTERN_END = "<";
    private final LinkedList<String> listeAlleThemen = new LinkedList<>();
    private final LinkedList<String> listeAlleThemenCount = new LinkedList<>();
    private final LinkedList<String> listeAlleThemenCount_ = new LinkedList<>();

    public MediathekBr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, 4, 100, startPrio);
    }

    private void startArchiveThreads() {
        if (CrawlerTool.loadLongMax()) {
            // Archiv durchsuchen
            Thread thArchiv;
            thArchiv = new ArchivLaden(1, 50);
            thArchiv.start();
            thArchiv = new ArchivLaden(51, 100);
            thArchiv.start();
            thArchiv = new ArchivLaden(101, 150);
            thArchiv.start();
            thArchiv = new ArchivLaden(151, 200);
            thArchiv.start();
        }
        if (CrawlerTool.loadMax()) {
            // Archiv durchsuchen
            Thread thArchiv;
            thArchiv = new ArchivLaden(201, 250);
            thArchiv.start();
            thArchiv = new ArchivLaden(251, 300);
            thArchiv.start();
            thArchiv = new ArchivLaden(301, 350);
            thArchiv.start();
            thArchiv = new ArchivLaden(351, 400);
            thArchiv.start();
        }
    }

    private void startKlassikThread() {
        Thread thKlassik = new KlassikLaden();
        thKlassik.setName(SENDERNAME + "-Klassik");
        thKlassik.start();
    }

    private void startCrawlerThreads() {
        for (int t = 0; t <= getMaxThreadLaufen(); ++t) {
            Thread th = new ThemaLaden();
            th.setName(SENDERNAME + t);
            th.start();
        }
    }

    @Override
   protected void addToList() {
        mlibFilmeSuchen.listeFilmeAlt.getThema(getSendername(), listeAlleThemenCount_);
        meldungStart();
        getTheman(); // Themen suchen
        getTage(); // Programm der letzten Tage absuchen
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty() && listeTage.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeTage.size());
            // erst hier starten (Archiv, Klassik), sonst beendet er sich/und sucht doch!
            startArchiveThreads();
            startKlassikThread();
            startCrawlerThreads();
        }
    }

    private void getTheman() {
        final String ADRESSE = "http://www.br.de/mediathek/video/sendungen/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/";
        final String MUSTER_URL_1 = "sendungen/";
        final String MUSTER_URL_2 = "video/";
        listeThemen.clear();
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri(SENDERNAME, ADRESSE, StandardCharsets.UTF_8, 5 /* versuche */, seite, "");
        int pos1;
        int pos2;
        String url = "";
        if ((pos1 = seite.indexOf("<ul class=\"clearFix\">")) != -1) {
            while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
                if (Config.getStop()) {
                    break;
                }

                try {
                    pos1 += MUSTER_URL.length();
                    if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                        url = seite.substring(pos1, pos2);
                    }
                    String thema = seite.extract("<span>", "<", pos1);
                    thema = StringEscapeUtils.unescapeXml(thema.trim());
                    thema = StringEscapeUtils.unescapeHtml4(thema.trim());
                    if (!listeAlleThemen.contains(thema)) {
                        listeAlleThemen.add(thema);
                    }
                    if (url.isEmpty()
                            || (!url.startsWith(MUSTER_URL_1) && !url.startsWith(MUSTER_URL_2))) {
                        continue;
                    }
                    /// der BR ist etwas zu langsam dafür????
//                    // in die Liste eintragen
//                    String[] add;
//                    if (MSearchConfig.senderAllesLaden) {
//                        add = new String[]{"http://www.br.de/mediathek/video/sendungen/" + url + "#seriesMoreCount=10", ""};
//                    } else {
//                        add = new String[]{"http://www.br.de/mediathek/video/sendungen/" + url, ""};
//                    }
                    // in die Liste eintragen
                    String[] add = new String[]{"http://www.br.de/mediathek/video/" + url, thema};
                    listeThemen.addUrl(add);
                } catch (Exception ex) {
                    Log.errorLog(821213698, ex);
                }
            }
        }
    }

    private void getTage() {
        // <a href="/mediathek/video/programm/mediathek-programm-100~_date-2014-01-05_-fc34efea1ee1bee90b0dc7888e292676f347679c.html" class="dayChange link_indexPage contenttype_epg mediathek-programm-100" data-
        // <a href="/mediathek/video/stadtlapelle-frankenland-100.html" title="zur Video-Detailseite" class="link_video contenttype_standard stadtlapelle-frankenland-100">
        String date;
        final String ADRESSE = "http://www.br.de/mediathek/video/programm/index.html";
        final String MUSTER = "http://www.br.de/mediathek/video/programm/mediathek-programm-100~_date-";
        listeTage.clear();
        MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        ArrayList<String> al = new ArrayList<>();
        try {
            //seite1 = getUrlIo.getUri_Utf(SENDERNAME, ADRESSE, seite1, "");
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite1 = getUrlIo.getUri(SENDERNAME, ADRESSE, StandardCharsets.UTF_8, 5 /* versuche */, seite1, "");
            String url;
            int max_;
            if (CrawlerTool.loadLongMax()) {
                max_ = 21;
            } else {
                max_ = 7;
            }
            for (int i = 0; i < max_; ++i) {
                if ((Config.getStop())) {
                    break;
                }
                date = new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime() - i * (1000 * 60 * 60 * 24));
                url = seite1.extract("/mediathek/video/programm/mediathek-programm-100~_date-" + date, "\"");
                if (url.isEmpty()) {
                    continue;
                }
                // in die Liste eintragen
                url = MUSTER + date + url;
                seite2 = getUrlIo.getUri_Utf(SENDERNAME, url, seite2, "");
                //      public void extractList(String abMuster, String bisMuster, String musterStart, String musterEnde, String addUrl, ArrayList<String> result) {
                seite2.extractList("<div class=\"epgContainer\"", "<h3>Legende</h3>", "<a href=\"/mediathek/video/", "\"", "http://www.br.de/mediathek/video/", al);
            }
            for (String s : al) {
                String[] add = new String[]{s, ""};
                if (!istInListe(listeTage, add[0], 0)) {
                    listeTage.add(add);
                }
            }
        } catch (Exception ex) {
            Log.errorLog(821213698, ex);
        }
    }

    @Override
    protected synchronized void meldungThreadUndFertig() {
        if (getThreads() <= 1) {
            //wird erst ausgeführt wenn alle Threads beendet sind
            mlibFilmeSuchen.listeFilmeNeu.checkThema(getSendername(), listeAlleThemenCount_, getSendername());
        }
        super.meldungThreadUndFertig();
    }

    private class ThemaLaden extends Thread {

        GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private final MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seiteXml = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1]/*thema*/, seite1, true);
                }
                while (!Config.getStop() && (link = listeTage.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1]/*thema*/, seite1, false);
                }
            } catch (Exception ex) {
                Log.errorLog(989632147, ex);
            }
            meldungThreadUndFertig();
        }

        private void laden(String urlThema, String thema, MSStringBuilder seite, boolean weitersuchen) {
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite = getUrlIo.getUri_Utf(SENDERNAME, urlThema, seite, "");
            if (seite.length() == 0) {
                return;
            }
            String urlXml;
            String thema_;
            String datum;
            String zeit = "";
            String description;
            String titel;

            if (seite.indexOf("<p class=\"noVideo\">Zur Sendung \"") != -1
                    && seite.indexOf("\" liegen momentan keine Videos vor</p>") != -1) {
                // dann gibts keine Videos
                // MSearchLog.fehlerMeldung(-120364780, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine Videos: " + urlThema);
                return;
            }
            thema_ = seite.extract("<h3>", "<").trim(); //<h3>Abendschau</h3>, <h3>Film und Serie  </h3>
            if (thema.isEmpty()) {
                thema = checkThema(thema_);
            }
            if (!thema.equals(thema_)) {
                // dann wird das Thema der Titel
                titel = thema_;
            } else {
                titel = seite.extract("<li class=\"title\">", "<"); //<li class="title">Spionageabwehr auf Bayerisch! - Folge 40</li>
            }            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            datum = seite.extract("<time class=\"start\" datetime=\"", ">", "<");
            datum = datum.replace("Uhr", "").trim();
            if (!datum.isEmpty()) {
                zeit = convertTime(datum);
                datum = convertDatum(datum);
            }
            String urlSeite = seite.extract("id=\"fieldLink\" value=\"", "\"");
            if (urlSeite.isEmpty()) {
                Log.errorLog(912030145, "urlSeite leer: " + urlThema);
                urlSeite = urlThema;
            }
            //<meta property="og:description" content="Aktuelle Berichte aus Bayern, Hintergründe zu brisanten Themen, Geschichten, die unter die Haut gehen - das ist die Abendschau. Sie sehen uns montags bis freitags von 18.00 bis 18.45 Uhr im Bayerischen Fernsehen."/>
            description = seite.extract("<meta property=\"og:description\" content=\"", "\"");
            if (description.isEmpty()) {
                description = seite.extract("<div class=\"bcastContent\">", "<p>", "</p>");
            }
            //<a href="#" onclick="return BRavFramework.register(BRavFramework('avPlayer_3f097ee3-7959-421b-b3f0-c2a249ad7c91').setup({dataURL:'/mediathek/video/sendungen/abendschau/der-grosse-max-spionageabwehr-100~meta_xsl-avtransform100_-daa09e70fbea65acdb1929dadbd4fc6cdb955b63.xml'}));" id="avPlayer_3f097ee3-7959-421b-b3f0-c2a249ad7c91">
            urlXml = seite.extract("{dataURL:'", "'");
            if (urlXml.isEmpty()) {
                Log.errorLog(915263478, "keine URL: " + urlThema);
            } else {
                urlXml = "http://www.br.de" + urlXml;
                loadXml(seiteXml, urlXml, urlSeite, thema, titel, description, datum, zeit);
            }
            if (!weitersuchen) {
                return;
            }
            // und jetzt noch nach weiteren Videos auf der Seite suchen
            // <h3>Mehr von <strong>Abendschau</strong></h3>
            // <a href="/mediathek/video/sendungen/abendschau/der-grosse-max-spionageabwehr-100.html" class="teaser link_video contenttype_podcast der-grosse-max-spionageabwehr-100" title="zur Detailseite">
            int pos1, count = 0;
            int max = (CrawlerTool.loadLongMax() ? 20 : 0);
            final String STOP = "<h3>Besucher, die dieses Video angesehen haben, sahen auch</h3>";
            int stop = seite.indexOf(STOP);
            if (max > 0) {
                // einzelne Themen verlängern
                if (urlThema.equals("http://www.br.de/mediathek/video/sendungen/spielfilme-im-br/spielfilme-im-br110.html")
                        || urlThema.equals("http://www.br.de/mediathek/video/sendungen/alpha-centauri/alpha-centauri104.html")) {
                    max = 60;
                }
                // dann mit der ganzen Seite arbeiten
                String u = seite.extract("<a class=\"button large\" href=\"", "\"", 0, stop);
                if (!u.isEmpty()) {
                    u = "http://www.br.de" + u;
                    seite3 = getUrlIo.getUri_Utf(SENDERNAME, u, seite, "");
                    if (seite3.length() != 0) {
                        seite = seite3;
                        stop = seite.indexOf(STOP);
                    }
                }
            }
            final String MUSTER_URL = "<a href=\"/mediathek/video/";
            if ((pos1 = seite.indexOf("<h3>Mehr von <strong>")) != -1) {
                while (!Config.getStop() && (pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
                    if (stop > 0 && pos1 > stop) {
                        break;
                    }
                    String urlWeiter = seite.extract(MUSTER_URL, "\"", pos1);
                    pos1 += MUSTER_URL.length();
                    if (!urlWeiter.isEmpty()) {
                        urlWeiter = "http://www.br.de/mediathek/video/" + urlWeiter;
                        if (urlWeiter.contains("livestream")) {
                            continue; // ist der Livestream
                        }
                        ++count;
                        if (count > max) {
//                            MSearchLog.debugMeldung("MediathekBr.laden" + " ------> count max erreicht: " + urlThema);
                            break;
                        }
                        laden(urlWeiter, thema, seite2, false);
                    }
                }
            }
        }

        private String convertDatum(String datum) {
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            try {
                Date filmDate = sdf.parse(datum);
                datum = sdfOutDay.format(filmDate);
            } catch (ParseException ex) {
                if (MserverDaten.debug)
                    Log.errorLog(915364789, ex, "Datum: " + datum);
            }
            return datum;
        }

        private String convertTime(String zeit) {
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            try {
                Date filmDate = sdf.parse(zeit);
                zeit = sdfOutTime.format(filmDate);
            } catch (ParseException ex) {
                if (MserverDaten.debug)
                    Log.errorLog(312154879, ex, "Time: " + zeit);
            }
            return zeit;
        }
    }

    private class KlassikLaden extends Thread {

        private final MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seiteXml = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                laden();
            } catch (Exception ex) {
                Log.errorLog(954123458, ex);
            }
            meldungThreadUndFertig();
        }

        private void laden() {
            MSStringBuilder seite = seite1;
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            getUrlIo.getUri_Utf(SENDERNAME, "http://www.br.de/mediathek/video/br-klassik-mediathek-100.html", seite, "");
            if (seite.length() == 0) {
                return;
            }
            String urlXml;
            final String thema = "BR-Klassik";
            String datum;
            String zeit = "";
            String description1, description2;
            String titel;

            String u = seite.extract("<a class=\"button large\" href=\"", "\"");
            if (!u.isEmpty()) {
                u = "http://www.br.de" + u;
                getUrlIo.getUri_Utf(SENDERNAME, u, seite2, "");
                if (seite2.length() != 0) {
                    seite = seite2;
                }
            }

            ArrayList<String> result = new ArrayList<>();
            seite.extractList("<h2 id=\"inhalt\" class=\"hidden\">Inhalt</h2>" /*abMuster*/,
                    "<div class=\"teaserBundleMore\">" /*bisMuster*/,
                    "<a href=\"/mediathek/video/" /*musterStart*/, "\"" /*musterEnde*/,
                    "http://www.br.de/mediathek/video/"/*addUrl*/, result);
            meldungAddMax(result.size());
            int count = 0;
            for (String url : result) {
                if (Config.getStop()) {
                    break;
                }
                if (!CrawlerTool.loadLongMax()) {
                    ++count;
                    if (count > 20) {
                        break;
                    }
                }
                meldungProgress(url);
                getUrlIo.getUri_Utf(SENDERNAME, url, seite3, url);
                titel = seite3.extract("<h3>", "<"); //<h3>U21-VERNETZT</h3>
                datum = seite3.extract("<time class=\"start\" datetime=\"", ">", "<");
                datum = datum.replace("Uhr", "").trim();
                if (!datum.isEmpty()) {
                    zeit = convertTime(datum);
                    datum = convertDatum(datum);
                }
                description1 = seite3.extract("<li class=\"title\">", "<");
                description1 += "\n";
                description2 = seite3.extract("<div class=\"bcastContent\">", "</p>");
                description2 = description2.replaceFirst("\n", "");
                description2 = description2.replaceFirst("<p>", "");
                description2 = description2.replaceAll("<br/>", "\n");

                urlXml = seite3.extract("{dataURL:'", "'");
                if (urlXml.isEmpty()) {
                    Log.errorLog(815263987, "keine URL: " + url);
                } else {
                    urlXml = "http://www.br.de" + urlXml;
                    loadXml(seiteXml, urlXml, url, thema, titel, description1 + description2, datum, zeit);
                }
            }

        }

        private String convertDatum(String datum) {
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            try {
                Date filmDate = sdf.parse(datum);
                datum = sdfOutDay.format(filmDate);
            } catch (ParseException ex) {
                Log.errorLog(915364789, ex, "Datum: " + datum);
            }
            return datum;
        }

        private String convertTime(String zeit) {
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            try {
                Date filmDate = sdf.parse(zeit);
                zeit = sdfOutTime.format(filmDate);
            } catch (ParseException ex) {
                Log.errorLog(312154879, ex, "Time: " + zeit);
            }
            return zeit;
        }
    }

    private void loadXml(MSStringBuilder seite, String urlXml, String urlThema, String thema, String titel, String description, String datum, String zeit) {
        String dauer;
        long duration = 0;
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri_Utf(SENDERNAME, urlXml, seite, "");
        if (seite.length() == 0) {
            Log.errorLog(820139701, urlXml);
            return;
        }

        try {
            //<duration>00:03:07</duration>
            dauer = seite.extract("<duration>", "<");
            if (!dauer.isEmpty()) {
                String[] parts = dauer.split(":");
                duration = 0;
                long power = 1;
                for (int i = parts.length - 1; i >= 0; i--) {
                    duration += Long.parseLong(parts[i]) * power;
                    power *= 60;
                }
            }
        } catch (NumberFormatException ex) {
            Log.errorLog(735216703, ex, urlThema);
        }
        if (description.isEmpty()) {
            description = seite.extract("<desc>", "</desc>");
        }
        String subtitle = seite.extract("<dataTimedText url=\"", "\""); // <dataTimedText url="/mediathek/video/untertitel-2528~default.xml" offset="0.0"/>
        String urlVerySmall = getUrl(seite, PATTERN_VERY_SMALL);
        String urlSmall = getUrl(seite, PATTERN_SMALL);
        String urlNormal = getUrl(seite, PATTERN_NORMAL);
        String urlHd = getUrl(seite, PATTERN_HD);

        if (urlNormal.isEmpty()) {
            if (!urlSmall.isEmpty()) {
                urlNormal = urlSmall;
                urlSmall = "";
            } else if (!urlVerySmall.isEmpty()) {
                urlNormal = urlVerySmall;
                urlVerySmall = "";
            }
        }
        if (urlVerySmall.isEmpty()) {
            urlVerySmall = urlSmall;
        }
        if (!urlNormal.isEmpty()) {

            if (thema.equals(SENDERNAME)) {
                thema = seite.extract("<broadcast>", "<");
                if (thema.equals(titel)) {
                    titel = seite.extract("<title>", "<");
                }
            }

            if (!thema.isEmpty()) {
                if (!listeAlleThemenCount.contains(thema)) {
                    listeAlleThemenCount.add(thema);
                } else if (!listeAlleThemenCount_.contains(thema)) {
                    // gibts dann mind. 2x
                    listeAlleThemenCount_.add(thema);
                }
            }

            DatenFilm film = new DatenFilm(SENDERNAME, thema, urlThema, titel, urlNormal, "" /*urlRtmp*/,
                    datum, zeit,
                    duration, description);
            if (!urlVerySmall.isEmpty()) {
                CrawlerTool.addUrlKlein(film, urlVerySmall, "");
            }
            if (!urlHd.isEmpty()) {
                CrawlerTool.addUrlHd(film, urlHd, "");
            }
            if (!subtitle.isEmpty()) {
                subtitle = "http://www.br.de" + subtitle;
                CrawlerTool.addUrlSubtitle(film, subtitle);
            }
            addFilm(film);
            meldung(film.arr[DatenFilm.FILM_URL]);
        } else {
            Log.errorLog(612136978, "keine URL: " + urlXml);
        }
    }

    /**
     * gets the url for the specified pattern
     *
     * @param seiteXml The xml site where to extract the url
     * @param pattern The pattern used to identify the url type
     * @return The extracted url
     */
    private String getUrl(MSStringBuilder seiteXml, String pattern) {
        return seiteXml.extract(pattern, PATTERN_DLURL, PATTERN_END);
    }

    private String checkThema(String thema) {
        thema = StringEscapeUtils.unescapeXml(thema.trim());
        thema = StringEscapeUtils.unescapeHtml4(thema.trim());
        for (String s : listeAlleThemen) {
            if (thema.equalsIgnoreCase(s)) {
                return s;
            }
        }
        for (String s : listeAlleThemen) {
            if (thema.toLowerCase().startsWith(s.toLowerCase())) {
                return s;
            }
        }
        return getSendername();
    }

    private class ArchivLaden extends Thread {

        private final int anfang, ende;
        private final MSStringBuilder seiteXml = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        public ArchivLaden(int aanfang, int eende) {
            anfang = aanfang;
            ende = eende;
            setName(SENDERNAME + "_Archiv_Laden_" + aanfang + '_' + eende);
        }

        @Override
        public void run() {
            meldungAddMax(ende - anfang);
            meldungAddThread();
            try {
                archivSuchen(anfang, ende);
            } catch (Exception ex) {
                Log.errorLog(203069877, ex);
            }
            meldungThreadUndFertig();
        }

        private static final String MUSTER_ADRESSE_1 = "http://www.br.de/service/suche/archiv102.html?documentTypes=video&page=";
        private static final String MUSTER_ADRESSE_2 = "&sort=date";
        private static final String MUSTER_START = "<div class=\"teaser search_result\">";
        private void archivSuchen(int start, int ende) {
            // http://www.br.de/service/suche/archiv102.html?documentTypes=video&page=1&sort=date
            MSStringBuilder seiteArchiv1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
            MSStringBuilder seiteArchiv2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
            GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            for (int i = start; i <= ende; ++i) {
                if (Config.getStop()) {
                    break;
                }
                String adresse = MUSTER_ADRESSE_1 + i + MUSTER_ADRESSE_2;
                meldungProgress(adresse);
                seiteArchiv1 = getUrl.getUri(SENDERNAME, adresse, StandardCharsets.UTF_8, 2 /* versuche */, seiteArchiv1, "" /* Meldung */);

                int pos = 0, stop;
                String url, titel, thema, datum, beschreibung;
                while (!Config.getStop() && (pos = seiteArchiv1.indexOf(MUSTER_START, pos)) != -1) {
                    pos += MUSTER_START.length();
                    stop = seiteArchiv1.indexOf(MUSTER_START, pos);
                    url = seiteArchiv1.extract("<a href=\"", "\"", pos, stop);
                    thema = seiteArchiv1.extract("teaser_overline\">", "<", pos, stop).trim();
                    if (thema.endsWith(":")) {
                        thema = thema.substring(0, thema.lastIndexOf(':'));
                    }
                    thema = checkThema(thema);
                    titel = seiteArchiv1.extract("teaser_title\">", "<", pos, stop);
                    // <p class="search_date">23.08.2013 | BR-alpha</p>
                    datum = seiteArchiv1.extract("search_date\">", "<", pos, stop);
                    if (datum.contains("|")) {
                        datum = datum.substring(0, datum.indexOf('|')).trim();
                    }
                    beschreibung = seiteArchiv1.extract("<p>", "<", pos, stop);
                    if (!url.isEmpty()) {
                        url = "http://www.br.de" + url;
                        archivAdd1(getUrl, seiteArchiv2, url, thema, titel, datum, beschreibung);
                    }
                }
            }
        }

        private void archivAdd1(GetUrl getUrl, MSStringBuilder seiteArchiv2, String urlThema, String thema, String titel, String datum, String beschreibung) {
            // http://www.br.de/service/suche/archiv102.html?documentTypes=video&page=1&sort=date
            meldung(urlThema);
            seiteArchiv2 = getUrl.getUri(SENDERNAME, urlThema, StandardCharsets.UTF_8, 1 /* versuche */, seiteArchiv2, "" /* Meldung */);
            String xmlUrl;

            xmlUrl = seiteArchiv2.extract("setup({dataURL:'", "'");
            if (beschreibung.isEmpty()) {
                beschreibung = seiteArchiv2.extract("<meta property=\"og:description\" content=\"", "\"");
            }
            if (beschreibung.isEmpty()) {
                beschreibung = seiteArchiv2.extract("<div class=\"bcastContent\">", "<p>", "</p>");
            }

            if (!xmlUrl.isEmpty()) {
                xmlUrl = "http://www.br.de" + xmlUrl;
                loadXml(seiteXml, xmlUrl, urlThema, thema, titel, beschreibung, datum, "");
            }
        }
    }
}
