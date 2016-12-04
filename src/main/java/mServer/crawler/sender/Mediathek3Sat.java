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
import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.CrawlerTool;

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
                if (url.equals("")) {
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
                        DatenFilm film = MediathekZdf.filmHolenId(getUrl, seite2, SENDERNAME, thema, titel, urlFilm, urlId);
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
}
