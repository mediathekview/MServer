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

import java.text.SimpleDateFormat;
import java.util.Date;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

public class Mediathek3Sat extends MediathekReader {

    public final static String SENDERNAME = Const.DREISAT;
    private final static String[] QU_WIDTH_HD = {"1280"};
    private final static String[] QU_WIDTH = {"1024", "852", "720", "688", "480", "432", "320"};
    private final static String[] QU_WIDTH_KL = {"688", "480", "432", "320"};
    private final static String BESCHREIBUNG = "<detail>";
    private final static String LAENGE_SEC = "<lengthSec>";
    private final static String LAENGE = "<length>";
    private final static String DATUM = "<airtime>";
    private final static String THEMA = "<originChannelTitle>";
    private static final String checkUrlHD_String = "http://www.metafilegenerator.de/ondemand/zdf/hbbtv/";
    private static final String URL_ANFANG = "<formitaet basetype=\"h264_aac_mp4_http_na_na\"";
    private static final String URL_ENDE = "</formitaet>";
    private static final String URL = "<url>";
    private static final String WIDTH = "<width>";

    public Mediathek3Sat(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 200, startPrio);
    }

    @Override
   protected void addToList() {
        listeThemen.clear();
        meldungStart();
        sendungenLaden();
        tageLaden();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            listeSort(listeThemen, 1);
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void tageLaden() {
        // https://www.3sat.de/mediathek/index.php?datum=20160303&cx=134
        String date;
        for (int i = 0; i < (CrawlerTool.loadLongMax() ? 21 : 7); ++i) {
            date = new SimpleDateFormat("yyyyMMdd").format(new Date().getTime() - i * (1000 * 60 * 60 * 24));
            String url = "https://www.3sat.de/mediathek/index.php?datum=" + date + "&cx=134";
            listeThemen.add(new String[]{url, ""});
        }
    }

    private void sendungenLaden() {
        // ><a class="SubItem" href="?red=kulturzeit">Kulturzeit</a>
        final String ADRESSE = "http://www.3sat.de/mediathek/";
        final String MUSTER_URL = "<a class=\"SubItem\" href=\"//www.3sat.de/mediathek/?red=";

        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri_Utf(SENDERNAME, ADRESSE, seite, "");
        int pos1 = 0;
        int pos2;
        String url = "", thema = "";
        while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
            try {
                pos1 += MUSTER_URL.length();
                if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (url.isEmpty()) {
                    continue;
                }
                if ((pos1 = seite.indexOf(">", pos1)) != -1) {
                    pos1 += 1;
                    if ((pos2 = seite.indexOf("<", pos1)) != -1) {
                        thema = seite.substring(pos1, pos2);
                    }
                }
                // in die Liste eintragen
                // http://www.3sat.de/mediathek/?red=nano&type=1
                // type=1 => nur ganze Sendungen
                String[] add = new String[]{"http://www.3sat.de/mediathek/?red=" + url + "&type=1", thema};
                listeThemen.addUrl(add);
            } catch (Exception ex) {
                Log.errorLog(915237874, ex);
            }
        }

    }

    private String extractBeschreibung(MSStringBuilder strBuffer, final String urlId) {
        String beschreibung = strBuffer.extract(BESCHREIBUNG, "<");
        if (beschreibung.isEmpty()) {
            beschreibung = strBuffer.extract(BESCHREIBUNG, "</");
            beschreibung = beschreibung.replace("<![CDATA[", "");
            beschreibung = beschreibung.replace("]]>", "");
            if (beschreibung.isEmpty()) {
                Log.errorLog(945123074, "url: " + urlId);
            }
        }
        return beschreibung;
    }

    private long extractLaenge(MSStringBuilder strBuffer) {
        long laengeL;

        String laenge = strBuffer.extract(LAENGE_SEC, "<");
        if (!laenge.isEmpty()) {
            laengeL = extractDurationSec(laenge);
        } else {
            laenge = strBuffer.extract(LAENGE, "<");
            if (laenge.contains(".")) {
                laenge = laenge.substring(0, laenge.indexOf('.'));
            }
            laengeL = extractDuration(laenge);
        }
        return laengeL;
    }

    private String extractSubtitle(MSStringBuilder strBuffer) {
        String subtitle = strBuffer.extract("<caption>", "<url>http://", "<", "http://");
        if (subtitle.isEmpty()) {
            subtitle = strBuffer.extract("<caption>", "<url>https://", "<", "https://");
            //            if (!subtitle.isEmpty()) {
            //                System.out.println("Hallo");
            //            }
        }
        return subtitle;
    }

    private DatenFilm filmHolenId(MSStringBuilder strBuffer, String sender, String thema, String titel, String filmWebsite, String urlId) {
        //<teaserimage alt="Harald Lesch im Studio von Abenteuer Forschung" key="298x168">http://www.zdf.de/ZDFmediathek/contentblob/1909108/timg298x168blob/8081564</teaserimage>
        //<detail>Möchten Sie wissen, was Sie in der nächsten Sendung von Abenteuer Forschung erwartet? Harald Lesch informiert Sie.</detail>
        //<length>00:00:34.000</length>
        //<airtime>02.07.2013 23:00</airtime>
        long laengeL;

        String zeit = "";

        final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        strBuffer = getUrl.getUri_Utf(sender, urlId, strBuffer, "URL-Filmwebsite: " + filmWebsite);
        if (strBuffer.length() == 0) {
            Log.errorLog(398745601, "url: " + urlId);
            return null;
        }

        String subtitle = extractSubtitle(strBuffer);

        String beschreibung = extractBeschreibung(strBuffer, urlId);

        if (thema.isEmpty()) {
            thema = strBuffer.extract(THEMA, "<");
        }

        laengeL = extractLaenge(strBuffer);

        String datum = strBuffer.extract(DATUM, "<");
        if (datum.contains(" ")) {
            zeit = datum.substring(datum.lastIndexOf(' ')).trim() + ":00";
            datum = datum.substring(0, datum.lastIndexOf(' ')).trim();
        }

        //============================================================================
        // und jetzt die FilmURLs
        String url, urlKlein, urlHd;

        urlHd = getUrl(strBuffer, QU_WIDTH_HD, true);
        url = getUrl(strBuffer, QU_WIDTH, true);
        urlKlein = getUrl(strBuffer, QU_WIDTH_KL, false);

        if (url.equals(urlKlein)) {
            urlKlein = "";
        }
        if (url.isEmpty()) {
            url = urlKlein;
            urlKlein = "";
        }

        //===================================================
        //if (urlHd.isEmpty())
        //{
        //            MSLog.fehlerMeldung(912024587, "keine URL: " + filmWebsite);
        //}
        //if (urlKlein.isEmpty())
        //{
        //            MSLog.fehlerMeldung(310254698, "keine URL: " + filmWebsite);
        //}
        if (url.isEmpty()) {
            Log.errorLog(397002891, "keine URL: " + filmWebsite);
            return null;
        } else {
            DatenFilm film = new DatenFilm(sender, thema, filmWebsite, titel, url, "" /*urlRtmp*/, datum, zeit,
                    laengeL, beschreibung);
            if (!subtitle.isEmpty()) {
                CrawlerTool.addUrlSubtitle(film, subtitle);
            }
            CrawlerTool.addUrlKlein(film, urlKlein, "");
            CrawlerTool.addUrlHd(film, urlHd, "");
            return film;
        }
    }

    private String getUrl(MSStringBuilder strBuffer, String[] arr, boolean hd) {
        String ret = "";
        String tmp;
        int posAnfang, posEnde;
        mainloop:
        for (String qual : arr) {
            posAnfang = 0;
            while (true) {
                if ((posAnfang = strBuffer.indexOf(URL_ANFANG, posAnfang)) == -1) {
                    break;
                }
                posAnfang += URL_ANFANG.length();
                if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1) {
                    break;
                }

                tmp = strBuffer.extract(URL, "<", posAnfang, posEnde);
                if (strBuffer.extract(WIDTH, "<", posAnfang, posEnde).equals(qual)) {
                    if (hd) {
                        ret = checkUrlHD(tmp);
                    } else {
                        ret = checkUrl(tmp);
                    }
                    if (!ret.isEmpty()) {
                        break mainloop;
                    }
                }
            }
        }
        if (ret.startsWith("http://tvdl.zdf.de")) {
            ret = ret.replace("http://tvdl.zdf.de", "http://nrodl.zdf.de");
        }
        return ret;
    }

    private String checkUrlHD(String url) {
        String ret = "";
        if (url.startsWith("http") && url.endsWith("mp4")) {
            ret = url;
            if (ret.startsWith(checkUrlHD_String)) {
                ret = ret.replaceFirst(checkUrlHD_String, "http://nrodl.zdf.de/");
            }
        }
        return ret;
    }

    private String checkUrl(String url) {
        String ret = "";
        if (url.startsWith("http") && url.endsWith("mp4")) {
            if (!url.startsWith("http://www.metafilegenerator.de/")) {
                ret = url;
            }
        }
        return ret;
    }

    private class ThemaLaden extends Thread {

        private final MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1] /* Thema */, true);
                }
            } catch (Exception ex) {
                Log.errorLog(987452384, ex);
            }
            meldungThreadUndFertig();
        }

        private void laden(String urlThema, String thema, boolean weiter) {

            final String MUSTER_START = "<div class=\"BoxPicture MediathekListPic\">";
            String url;
            for (int i = 0; i < (CrawlerTool.loadLongMax() ? 40 : 5); ++i) {
                //http://www.3sat.de/mediathek/?type=1&red=nano&mode=verpasst3
                if (thema.isEmpty()) {
                    // dann ist es aus "TAGE"
                    // und wird auch nur einmanl durchlaufen
                    url = urlThema;
                    i = 9999;
                } else {
                    weiter = false;
                    url = urlThema + "&mode=verpasst" + i;
                }
                meldung(url);
                final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrlIo.getUri_Utf(SENDERNAME, url, seite1, "");
                if (seite1.indexOf(MUSTER_START) == -1) {
                    // dann gibts keine weiteren
                    break;
                }
                int pos1 = 0;
                boolean ok;
                String titel, urlId, urlFilm;
                while ((pos1 = seite1.indexOf(MUSTER_START, pos1)) != -1) {
                    pos1 += MUSTER_START.length();
                    ok = false;
                    // <a class="MediathekLink"  title='Video abspielen: nano vom 8. Januar 2014' href="?mode=play&amp;obj=40860">
                    titel = seite1.extract("<a class=\"MediathekLink\"  title='Video abspielen:", "'", pos1).trim();
                    // ID
                    // http://www.3sat.de/mediathek/?mode=play&obj=40860
                    // href="http://www.3sat.de/mediathek/?mode=play&amp;obj=54458"
                    urlId = seite1.extract("href=\"//www.3sat.de/mediathek/?mode=play&amp;obj=", "\"", pos1);
                    if (urlId.isEmpty()) {
                        //href="?obj=24138"
                        urlId = seite1.extract("href=\"?obj=", "\"", pos1);
                    }
                    urlFilm = "http://www.3sat.de/mediathek/?mode=play&obj=" + urlId;
                    if (!urlId.isEmpty()) {
                        //http://www.3sat.de/mediathek/xmlservice/web/beitragsDetails?ak=web&id=40860
                        urlId = "http://www.3sat.de/mediathek/xmlservice/web/beitragsDetails?ak=web&id=" + urlId;
                        //meldung(id);
                        DatenFilm film = filmHolenId(seite2, SENDERNAME, thema, titel, urlFilm, urlId);
                        if (film != null) {
                            // dann wars gut
                            // jetzt noch manuell die Auflösung hochsetzen
                            MediathekZdf.urlTauschen(film, url, mlibFilmeSuchen);
                            addFilm(film);
                            ok = true;
                        }
                    }
                    if (!ok) {
                        // dann mit der herkömmlichen Methode versuchen
                        Log.errorLog(462313269, "Thema: " + url);
                    }
                }
            }
            if (weiter && seite1.indexOf("mode=verpasst1") != -1) {
                // dann gibts eine weitere Seite
                laden(urlThema + "&mode=verpasst1", thema, false);
            }
        }
    }
}
