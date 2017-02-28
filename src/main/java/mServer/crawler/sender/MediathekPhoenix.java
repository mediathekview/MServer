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

import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MediathekPhoenix extends MediathekReader {

    public final static String SENDERNAME = Const.PHOENIX;
    private MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekPhoenix(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, 4 /* threads */, 250 /* urlWarten */, startPrio);
    }

    @Override
    public void addToList() {
        listeThemen.clear();
        meldungStart();
        addToList_();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            //alles auswerten
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void addToList_() {
        final String MUSTER = "<li><strong><a href=\"/content/";
        GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        seite = getUrl.getUri(SENDERNAME, "http://www.phoenix.de/content/78905", StandardCharsets.ISO_8859_1, 6 /* versuche */, seite, "" /* Meldung */);
        if (seite.length() == 0) {
            Log.errorLog(487512369, "Leere Seite für URL: ");
        }

        int pos = 0;
        int pos1;
        int pos2;
        String thema;
        while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
            pos += MUSTER.length();
            pos1 = pos;
            if ((pos2 = seite.indexOf("\"", pos)) != -1) {
                String url = seite.substring(pos1, pos2);
                if (!url.isEmpty()) {
                    url = "http://www.phoenix.de/content/" + url;
                    thema = seite.extract(">", "<", pos);
                    thema = thema.replace("\"", "");
                    listeThemen.addUrl(new String[]{url, thema});
                }
            }
        }
    }

    private class ThemaLaden extends Thread {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private final MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                String link[];
                meldungAddThread();
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    seite1.setLength(0);
                    addFilme1(link[0]/* url */, link[1]/* Thema */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                Log.errorLog(825263641, ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme1(String url, String thema) {
            try {
                getUrl.getUri_Iso(SENDERNAME, url, seite1, "Thema: " + thema);
                ArrayList<String> liste = new ArrayList<>();
                seite1.extractList("<div class=\"linkliste2\">", "", "<li><a href=\"/content/", "\"", "http://www.phoenix.de/content/", liste);
                for (String urlThema : liste) {
                    if (Config.getStop()) {
                        break;
                    }
                    meldung(urlThema);
                    addFilme2(thema, urlThema);
                }
            } catch (Exception ex) {
                Log.errorLog(741258410, ex, url);
            }
        }

        private void addFilme2(String thema, String filmWebsite) {
            // https://www.phoenix.de/php/mediaplayer/data/beitrags_details.php?ak=web&id=980552
            getUrl.getUri_Iso(SENDERNAME, filmWebsite, seite1, "" /* Meldung */);
            String urlId = seite1.extract("<div class=\"phx_vod\" id=\"phx_vod_", "", "\"", 0, 0, "http://www.phoenix.de/php/mediaplayer/data/beitrags_details.php?ak=web&id=");

            String title = seite1.extract("<title>", "<"); //<title>phoenix  - "Gysi geht - Was wird aus der Linken?"</title>
            title = title.replace("phoenix  -", "").trim();
            if (!urlId.isEmpty()) {
                filmHolenId(thema, filmWebsite, urlId, title);
            } else {
                Log.errorLog(912546987, filmWebsite);
            }
        }

        private void filmHolenId(String thema, String filmWebsite, String urlId, String title_) {
            if (Config.getStop()) {
                return;
            }
            meldung(urlId);
            getUrl.getUri_Utf(SENDERNAME, urlId, seite3, "" /* Meldung */);
            if (seite3.length() == 0) {
                Log.errorLog(825412874, "url: " + urlId);
                return;
            }

            String titel = seite3.extract("<title>", "<");
            if (titel.isEmpty()) {
                titel = title_;
            }
            titel = titel.replaceAll("", "-");
            if (titel.startsWith("\"") && titel.endsWith("\"")) {
                titel = titel.substring(1, titel.length() - 2);
            }
            String beschreibung = seite3.extract("<detail>", "<");
            if (beschreibung.startsWith(titel)) {
                beschreibung = beschreibung.replaceFirst(titel, "");
            }
            beschreibung = beschreibung.replaceAll("\n", "");
            beschreibung = beschreibung.replaceAll("", "-");

            String laenge = seite3.extract("<lengthSec>", "<");
            //<onlineairtime>19.09.2014 10:53</onlineairtime>
            //<airtime>01.01.1970 01:00</airtime>
            String datum = seite3.extract("<airtime>", "<");
            if (datum.startsWith("01.01.1970")) {
                datum = seite3.extract("<onlineairtime>", "<");
            }
            String zeit = "";
            if (datum.contains(" ")) {
                zeit = datum.substring(datum.lastIndexOf(' ')).trim() + ":00";
                datum = datum.substring(0, datum.lastIndexOf(' ')).trim();
            }

            String url = "", urlKlein = "", urlHd = "";
            int posAnfang = 0, posEnde, pos1;
            final String URL_ANFANG = "<formitaet basetype=\"h264_aac_mp4_http_na_na\"";
            final String URL_ENDE = "</formitaet>";
            final String URL = "<url>";
            final String URL_ANFANG_HD = "<formitaet basetype=\"wmv3_wma9_asf_mms_asx_http\"";

            while (true) {
                if ((posAnfang = seite3.indexOf(URL_ANFANG, posAnfang)) == -1) {
                    break;
                }
                posAnfang += URL_ANFANG.length();
                if ((posEnde = seite3.indexOf(URL_ENDE, posAnfang)) == -1) {
                    break;
                }
                if ((pos1 = seite3.indexOf("<quality>high</quality>", posAnfang)) != -1) {
                    if (pos1 < posEnde) {
                        if (!urlKlein.isEmpty() && !urlKlein.contains("metafilegenerator")) {
                            continue;
                        }
                        urlKlein = seite3.extract(URL, "<", posAnfang, posEnde);
                    }
                }
                if ((pos1 = seite3.indexOf("<quality>veryhigh</quality>", posAnfang)) != -1) {
                    if (pos1 < posEnde) {
                        if (!url.isEmpty() && !url.contains("metafilegenerator") && !url.contains("podfiles")) {
                            continue;
                        }
                        url = seite3.extract(URL, "<", posAnfang, posEnde);
                    }
                }
            }

            // und jetzt nochmal für HD
            posAnfang = 0;
            while (true) {
                if ((posAnfang = seite3.indexOf(URL_ANFANG_HD, posAnfang)) == -1) {
                    break;
                }
                posAnfang += URL_ANFANG_HD.length();
                if ((posEnde = seite3.indexOf(URL_ENDE, posAnfang)) == -1) {
                    break;
                }
                if ((pos1 = seite3.indexOf("<quality>hd</quality>", posAnfang)) != -1) {
                    if (pos1 > posEnde) {
                        break;
                    }
                    urlHd = seite3.extract(URL, "<", posAnfang, posEnde);
                    if (!urlHd.isEmpty()) {
                        break;
                    }
                }
            }
            if (url.isEmpty() && !urlKlein.isEmpty()) {
                url = urlKlein;
                urlKlein = "";
            }

            if (url.isEmpty()) {
                Log.errorLog(952102014, "keine URL: " + filmWebsite);
            } else {
                if (url.startsWith("http://tvdl.zdf.de")) {
                    url = url.replace("http://tvdl.zdf.de", "http://nrodl.zdf.de");
                }
                if (urlKlein.startsWith("http://tvdl.zdf.de")) {
                    urlKlein = urlKlein.replace("http://tvdl.zdf.de", "http://nrodl.zdf.de");
                }
                if (urlHd.startsWith("http://tvdl.zdf.de")) {
                    urlHd = url.replace("http://tvdl.zdf.de", "http://nrodl.zdf.de");
                }

                DatenFilm film = new DatenFilm(SENDERNAME, thema, filmWebsite, titel, url, "" /*urlRtmp*/, datum, zeit,
                        extractDuration(laenge), beschreibung);
                addFilm(film);
                CrawlerTool.addUrlKlein(film, urlKlein, "");
                CrawlerTool.addUrlHd(film, urlHd, "");
            }
        }

    }
}
