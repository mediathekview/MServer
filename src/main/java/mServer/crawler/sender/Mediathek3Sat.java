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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Mediathek3Sat extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.DREISAT;

    public Mediathek3Sat(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        listeThemen.clear();
        meldungStart();
        sendungenLaden();
        tageLaden();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            listeSort(listeThemen, 1);
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
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
        final String MUSTER_URL = "<a class=\"SubItem\" href=\"http://www.3sat.de/mediathek/?red=";
        
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
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
                Log.errorLog(915237874,  ex);
            }
        }

    }

    private class ThemaLaden implements Runnable {

        GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1] /* Thema */, true);
                }
            } catch (Exception ex) {
                Log.errorLog(987452384,  ex);
            }
            meldungThreadUndFertig();
        }

        void laden(String urlThema, String thema, boolean weiter) {

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
                GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
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
                    urlId = seite1.extract("href=\"http://www.3sat.de/mediathek/?mode=play&amp;obj=", "\"", pos1);
                    if (urlId.isEmpty()) {
                        //href="?obj=24138"
                        urlId = seite1.extract("href=\"?obj=", "\"", pos1);
                    }
                    urlFilm = "http://www.3sat.de/mediathek/?mode=play&obj=" + urlId;
                    if (!urlId.isEmpty()) {
                        //http://www.3sat.de/mediathek/xmlservice/web/beitragsDetails?ak=web&id=40860
                        urlId = "http://www.3sat.de/mediathek/xmlservice/web/beitragsDetails?ak=web&id=" + urlId;
                        //meldung(id);
                        DatenFilm film = filmHolenId(getUrl, seite2, SENDERNAME, thema, titel, urlFilm, urlId);
                        if (film != null) {
                            // dann wars gut
                            // jetzt noch manuell die Auflösung hochsetzen
                            MediathekZdf.urlTauschen(film, url, mSearchFilmeSuchen);
                            addFilm(film);
                            ok = true;
                        }
                    }
                    if (!ok) {
                        // dann mit der herkömmlichen Methode versuchen
                        Log.errorLog(462313269,  "Thema: " + url);
                    }
                }
            }
            if (weiter && seite1.indexOf("mode=verpasst1") != -1) {
                // dann gibts eine weitere Seite
                laden(urlThema + "&mode=verpasst1", thema, false);
            }
        }
    }
    
   public static DatenFilm filmHolenId(GetUrl getUrl, MSStringBuilder strBuffer, String sender, String thema, String titel, String filmWebsite, String urlId)
    {
        //<teaserimage alt="Harald Lesch im Studio von Abenteuer Forschung" key="298x168">http://www.zdf.de/ZDFmediathek/contentblob/1909108/timg298x168blob/8081564</teaserimage>
        //<detail>Möchten Sie wissen, was Sie in der nächsten Sendung von Abenteuer Forschung erwartet? Harald Lesch informiert Sie.</detail>
        //<length>00:00:34.000</length>
        //<airtime>02.07.2013 23:00</airtime>
        final String BESCHREIBUNG = "<detail>";
        final String LAENGE_SEC = "<lengthSec>";
        final String LAENGE = "<length>";
        final String DATUM = "<airtime>";
        final String THEMA = "<originChannelTitle>";
        long laengeL;

        String beschreibung, subtitle, laenge, datum, zeit = "";

        strBuffer = getUrl.getUri_Utf(sender, urlId, strBuffer, "URL-Filmwebsite: " + filmWebsite);
        if (strBuffer.length() == 0)
        {
            Log.errorLog(398745601, "url: " + urlId);
            return null;
        }

        subtitle = strBuffer.extract("<caption>", "<url>http://", "<", "http://");
        if (subtitle.isEmpty())
        {
            subtitle = strBuffer.extract("<caption>", "<url>https://", "<", "https://");
            //            if (!subtitle.isEmpty()) {
            //                System.out.println("Hallo");
            //            }
        }
        beschreibung = strBuffer.extract(BESCHREIBUNG, "<");
        if (beschreibung.isEmpty())
        {
            beschreibung = strBuffer.extract(BESCHREIBUNG, "</");
            beschreibung = beschreibung.replace("<![CDATA[", "");
            beschreibung = beschreibung.replace("]]>", "");
            if (beschreibung.isEmpty())
            {
                Log.errorLog(945123074, "url: " + urlId);
            }
        }
        if (thema.isEmpty())
        {
            thema = strBuffer.extract(THEMA, "<");
        }

        laenge = strBuffer.extract(LAENGE_SEC, "<");
        if (!laenge.isEmpty())
        {
            laengeL = extractDurationSec(laenge);
        } else
        {
            laenge = strBuffer.extract(LAENGE, "<");
            if (laenge.contains("."))
            {
                laenge = laenge.substring(0, laenge.indexOf("."));
            }
            laengeL = extractDuration(laenge);
        }

        datum = strBuffer.extract(DATUM, "<");
        if (datum.contains(" "))
        {
            zeit = datum.substring(datum.lastIndexOf(" ")).trim() + ":00";
            datum = datum.substring(0, datum.lastIndexOf(" ")).trim();
        }

        //============================================================================
        // und jetzt die FilmURLs
        final String[] QU_WIDTH_HD = {"1280"};
        final String[] QU_WIDTH = {"1024", "852", "720", "688", "480", "432", "320"};
        final String[] QU_WIDTH_KL = {"688", "480", "432", "320"};
        String url, urlKlein, urlHd, tmp = "";

        urlHd = getUrl(strBuffer, QU_WIDTH_HD, tmp, true);
        url = getUrl(strBuffer, QU_WIDTH, tmp, true);
        urlKlein = getUrl(strBuffer, QU_WIDTH_KL, tmp, false);

        if (url.equals(urlKlein))
        {
            urlKlein = "";
        }
        if (url.isEmpty())
        {
            url = urlKlein;
            urlKlein = "";
        }

        //===================================================
        if (urlHd.isEmpty())
        {
            //            MSLog.fehlerMeldung(912024587, "keine URL: " + filmWebsite);
        }
        if (urlKlein.isEmpty())
        {
            //            MSLog.fehlerMeldung(310254698, "keine URL: " + filmWebsite);
        }
        if (url.isEmpty())
        {
            Log.errorLog(397002891, "keine URL: " + filmWebsite);
            return null;
        } else
        {
            DatenFilm film = new DatenFilm(sender, thema, filmWebsite, titel, url, "" /*urlRtmp*/, datum, zeit,
                    laengeL, beschreibung);
            if (!subtitle.isEmpty())
            {
                CrawlerTool.addUrlSubtitle(film, subtitle);
            }
            CrawlerTool.addUrlKlein(film, urlKlein, "");
            CrawlerTool.addUrlHd(film, urlHd, "");
            return film;
        }
    }
   
       private static String getUrl(MSStringBuilder strBuffer, String[] arr, String tmp, boolean hd)
    {
        final String URL_ANFANG = "<formitaet basetype=\"h264_aac_mp4_http_na_na\"";
        final String URL_ENDE = "</formitaet>";
        final String URL = "<url>";
        final String WIDTH = "<width>";

        String ret = "";
        tmp = "";
        int posAnfang, posEnde;
        mainloop:
        for (String qual : arr)
        {
            posAnfang = 0;
            while (true)
            {
                if ((posAnfang = strBuffer.indexOf(URL_ANFANG, posAnfang)) == -1)
                {
                    break;
                }
                posAnfang += URL_ANFANG.length();
                if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1)
                {
                    break;
                }

                tmp = strBuffer.extract(URL, "<", posAnfang, posEnde);
                if (strBuffer.extract(WIDTH, "<", posAnfang, posEnde).equals(qual))
                {
                    if (hd)
                    {
                        ret = checkUrlHD(tmp);
                    } else
                    {
                        ret = checkUrl(tmp);
                    }
                    if (!ret.isEmpty())
                    {
                        break mainloop;
                    }
                }
            }
        }
        if (ret.startsWith("http://tvdl.zdf.de"))
        {
            ret = ret.replace("http://tvdl.zdf.de", "http://nrodl.zdf.de");
        }
        return ret;
    }
       
       private static String checkUrlHD(String url)
    {
        String ret = "";
        if (url.startsWith("http") && url.endsWith("mp4"))
        {
            ret = url;
            if (ret.startsWith("http://www.metafilegenerator.de/ondemand/zdf/hbbtv/"))
            {
                ret = ret.replaceFirst("http://www.metafilegenerator.de/ondemand/zdf/hbbtv/", "http://nrodl.zdf.de/");
            }
        }
        return ret;
    }

    private static String checkUrl(String url)
    {
        String ret = "";
        if (url.startsWith("http") && url.endsWith("mp4"))
        {
            if (!url.startsWith("http://www.metafilegenerator.de/"))
            {
                ret = url;
            }
        }
        return ret;
    }
}
