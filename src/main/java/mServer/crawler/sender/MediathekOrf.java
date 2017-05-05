/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 *
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
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

public class MediathekOrf extends MediathekReader {

    public final static String SENDERNAME = Const.ORF;
    private static final String THEMA_TAG = "-1";
    private static final String THEMA_SENDUNGEN = "-2";

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekOrf(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 100, startPrio);
    }

    @Override
    protected void addToList() {
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
        if (CrawlerTool.loadLongMax()) {
            bearbeiteAdresseSendung(seite);
        }
        listeSort(listeThemen, 1);
        int maxTage = CrawlerTool.loadLongMax() ? 9 : 2;
        for (int i = 0; i < maxTage; ++i) {
            String vorTagen = getGestern(i).toLowerCase();
            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/" + vorTagen, seite);
        }
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void bearbeiteAdresseTag(String adresse, MSStringBuilder seite) {
        // <a href="http://tvthek.orf.at/program/Kultur-heute/3078759/Kultur-Heute/7152535" class="item_inner clearfix">
        GetUrl getUrl = new GetUrl(100);
        seite = getUrl.getUri(SENDERNAME, adresse, StandardCharsets.UTF_8, 2, seite, "");
        ArrayList<String> al = new ArrayList<>();
        seite.extractList("", "", "<a href=\"http://tvthek.orf.at/profile/", "\"", "http://tvthek.orf.at/profile/", al);
        for (String s : al) {
            String[] add = new String[]{s, THEMA_TAG}; // werden extra behandelt
            if (!istInListe(listeThemen, add[0], 0)) {
                listeThemen.add(add);
            }
        }
    }

    private void bearbeiteAdresseSendung(MSStringBuilder seite) {
        final String URL = "http://tvthek.orf.at/profiles";
        GetUrl getUrl = new GetUrl(100);
        seite = getUrl.getUri(SENDERNAME, URL, StandardCharsets.UTF_8, 3, seite, "");
        ArrayList<String> al = new ArrayList<>();
        try {
            seite.extractList("", "", "<a href=\"/profiles/letter/", "\"", "http://tvthek.orf.at/profiles/letter/", al);
            for (String s : al) {
                String[] add = new String[]{s, THEMA_SENDUNGEN};
                if (!istInListe(listeThemen, add[0], 0)) {
                    listeThemen.add(add);
                }
            }
        } catch (Exception ex) {
            Log.errorLog(826341789, ex);
        }
    }

    private class ThemaLaden extends Thread {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> alSendung = new ArrayList<>();
        //private final ArrayList<String> alThemen = new ArrayList<>();
        private final ArrayList<String> urlList = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    try {
                        meldungProgress(link[0]);
                        switch (link[1]) {
                            case THEMA_TAG:
                                // dann ist von "Tage zurück"
                                feedEinerSeiteSuchen(link[0], true /*nurUrlPruefen*/);
                                break;
                            case THEMA_SENDUNGEN:
                                sendungen(link[0]);
                                break;
//                            default:
//                                themen(link[0] /* url */);
//                                break;
                        }
                    } catch (Exception ex) {
                        Log.errorLog(795633581, ex);
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(554012398, ex);
            }
            meldungThreadUndFertig();
        }

        private void sendungen(String url) {
            GetUrl getUrl = new GetUrl(100);
            seite1 = getUrl.getUri(SENDERNAME, url, StandardCharsets.UTF_8, 2, seite1, "");
            alSendung.clear();
            //int start = "http://tvthek.orf.at/profile/".length();
            seite1.extractList("", "", "<a href=\"http://tvthek.orf.at/profile/", "\"", "http://tvthek.orf.at/profile/", alSendung);
            for (String s : alSendung) {
                try {
                    if (Config.getStop()) {
                        break;
                    }
                    if (s.startsWith("http://tvthek.orf.at/profile/Archiv/")) {
                        break; // vorerst mal weglassen, sind zu viele
                    }
                    feedEinerSeiteSuchen(s, false /*nurUrlPruefen*/);
                } catch (Exception ex) {
                    Log.errorLog(702095478, ex);
                }
            }
        }

        private void feedEinerSeiteSuchen(String strUrlFeed, boolean nurUrlPruefen) {
            //<title> ORF TVthek: a.viso - 28.11.2010 09:05 Uhr</title>
            seite2 = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, seite2, "");
            String datum;
            String zeit;
            long duration = 0;
            String description;
            String tmp;
            String urlRtmpKlein = "", urlRtmp = "", url, urlKlein, urlHD;
            String titel, thema;
            String subtitle;
            int posStart, posStopAlles, posStopEpisode, pos = 0;
            meldung(strUrlFeed);
            thema = seite2.extract("<title>", "vom"); //<title>ABC Bär vom 17.11.2013 um 07.35 Uhr / ORF TVthek</title>

            datum = seite2.extract("<span class=\"meta meta_date\">", "<");
            if (datum.contains(",")) {
                datum = datum.substring(datum.indexOf(',') + 1).trim();
            }
            zeit = seite2.extract("<span class=\"meta meta_time\">", "<");
            zeit = zeit.replace("Uhr", "").trim();
            if (zeit.length() == 5) {
                zeit = zeit.replace(".", ":") + ":00";
            }
            boolean onlyOne = false;
            posStart = seite2.indexOf("<!-- start playlist -->");
            posStopAlles = seite2.indexOf("<!-- ende: playlist -->", posStart);
            if (posStart < 0 || posStopAlles < 0) {
                posStart = seite2.indexOf("<!-- start: player -->");
                posStopAlles = seite2.indexOf("<div class=\"service_footer\">", posStart);
                onlyOne = true;
            }

            final String MUSTER_SUCHEN = "<li class=\"base_list_item segment_";
            while (onlyOne || (pos = seite2.indexOf(MUSTER_SUCHEN, pos)) != -1) {
                if (onlyOne) {
                    posStopEpisode = posStopAlles;
                    onlyOne = false;
                    titel = seite2.extract("<h3 class=\"video_headline\">", "<", pos, posStopEpisode);
                    if (!titel.equals(StringEscapeUtils.unescapeJava(titel))) {
                        titel = StringEscapeUtils.unescapeJava(titel).trim();
                    }
                } else {
                    posStopEpisode = seite2.indexOf("</footer>", pos);
                    if (posStopEpisode == -1 || posStopEpisode > posStopAlles) {
                        break;
                    }
                    if (pos > posStopAlles) {
                        break;
                    }
                    titel = seite2.extract("<h4 class=\"base_list_item_headline\">", "<", pos, posStopEpisode);
                    if (!titel.equals(StringEscapeUtils.unescapeJava(titel))) {
                        titel = StringEscapeUtils.unescapeJava(titel).trim();
                    }
                }
                pos += MUSTER_SUCHEN.length();

                tmp = seite2.extract("&quot;duration&quot;:", ",", pos, posStopEpisode);
                try {
                    duration = Long.parseLong(tmp) / 1000; // time in milliseconds
                } catch (Exception ignored) {
                }

                subtitle = seite2.extract("{&quot;src&quot;:&quot;", "&quot", pos, posStopEpisode);
                if (!subtitle.isEmpty()) {
                    // "srt_file_url":"http:\/\/tvthek.orf.at\/dynamic\/get_asset.php?a=orf_episodes%2Fsrt_file%2F9346995.srt"
                    subtitle = subtitle.replace("\\/", "/");
                    subtitle = subtitle.replace("%2F", "/");
                }

                description = seite2.extract("<div class=\"details\">", "</div>", pos, posStopEpisode).trim();
                if (description.isEmpty()) {
                    description = seite2.extract("<div class=\"details_description\">", "</div>", pos, posStopEpisode).trim();
                }
                if (description.isEmpty()) {
                    description = seite2.extract("&quot;description&quot;:&quot;", "&quot", pos, posStopEpisode).trim();
                }
                if (!description.equals(StringEscapeUtils.unescapeJava(description))) {
                    description = StringEscapeUtils.unescapeJava(description).trim();
                }
                if (description.isEmpty()) {
                    Log.errorLog(989532147, "keine Beschreibung: " + strUrlFeed);
                }
                url = "";
                urlHD = "";
                urlKlein = "";
                final String MUSTER_URL = "{&quot;quality&quot;:&quot;Q6A&quot;,&quot;quality_string&quot;:&quot;hoch&quot;,&quot;src&quot;:&quot;http";
                final String MUSTER_URL_HD = "quality&quot;:&quot;Q8C&quot;,&quot;quality_string&quot;:&quot;sehr hoch (HD)&quot;,&quot;src&quot;:&quot;http";
                final String MUSTER_URL_KLEIN = "quality&quot;:&quot;Q4A&quot;,&quot;quality_string&quot;:&quot;mittel&quot;,&quot;src&quot;:&quot;http";

                // =======================================================
                // url
                urlList.clear();
                seite2.extractList(pos, posStopEpisode, MUSTER_URL, "", "&quot;", "http", urlList);
                for (String u : urlList) {
                    if (u.endsWith(".mp4")) {
                        url = u.replace("\\/", "/");
                        break;
                    }
                }
                // =======================================================
                // urlHD
                urlList.clear();
                seite2.extractList(pos, posStopEpisode, MUSTER_URL_HD, "", "&quot;", "http", urlList);
                for (String u : urlList) {
                    if (u.endsWith(".mp4")) {
                        urlHD = u.replace("\\/", "/");
                        break;
                    }
                }
                // =======================================================
                // urlKlein
                urlList.clear();
                seite2.extractList(pos, posStopEpisode, MUSTER_URL_KLEIN, "", "&quot;", "http", urlList);
                for (String u : urlList) {
                    if (u.endsWith(".mp4")) {
                        urlKlein = u.replace("\\/", "/");
                        break;
                    }
                }

                if (!url.isEmpty()) {
                    if (thema.isEmpty()) {
                        thema = SENDERNAME;
                    }
                    if (titel.isEmpty()) {
                        titel = SENDERNAME;
                    }
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, strUrlFeed, titel, url, urlRtmp, datum, zeit, duration, description);
                    if (!urlKlein.isEmpty()) {
                        CrawlerTool.addUrlKlein(film, urlKlein, urlRtmpKlein);
                    }
                    if (!urlHD.isEmpty()) {
                        CrawlerTool.addUrlHd(film, urlHD, "");
                    }
                    if (!subtitle.isEmpty()) {
                        CrawlerTool.addUrlSubtitle(film, subtitle);
                    }
                    addFilm(film, nurUrlPruefen);
                } else {
                    Log.errorLog(989532147, "keine Url: " + strUrlFeed);
                }
            }
        }
    }

    private String getGestern(int tage) {
        try {
            //SimpleDateFormat sdfOut = new SimpleDateFormat("EEEE", Locale.US);
            FastDateFormat sdfOut = FastDateFormat.getInstance("dd.MM.yyyy");
            return sdfOut.format(new Date(new Date().getTime() - tage * (1000 * 60 * 60 * 24)));
        } catch (Exception ex) {
            return "";
        }
    }
}
