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

public class MediathekZdfTivi extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.ZDF_TIVI;
    private final SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final SimpleDateFormat sdfOut_date = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat sdfOut_time = new SimpleDateFormat("HH:mm:ss");
    private final LinkedListUrl listeThemen_3 = new LinkedListUrl();

    public MediathekZdfTivi(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, 2 /* threads */, 500 /* urlWarten */, startPrio);
    }

    @Override
    public synchronized void addToList() {
        //Theman suchen
        listeThemen.clear();
        meldungStart();
        add_1();
        add_2();
        add_3();
        if (Config.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0 && listeThemen_3.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeThemen_3.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void add_1() {
        //<ns3:headline>Nachrichten</ns3:headline>
        //<ns3:image>/tiviVideos/contentblob/2063212/tivi9teaserbild/9050138</ns3:image>
        //<ns3:page>/tiviVideos/beitrag/pur%2B+Sendungen/895212/2063212?view=flashXml</ns3:page>
        //<ns3:text>Ich will die Wahrheit!</ns3:text>
        final String MUSTER_URL = "<ns3:page>/tiviVideos";
        MSStringBuilder seiteTivi_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrlIo.getUri(SENDERNAME, "http://www.tivi.de/tiviVideos/rueckblick?view=flashXml", Const.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            Log.errorLog(732323698, "Leere Seite");
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        try {
            while ((pos = seiteTivi_1.indexOf(MUSTER_URL, pos)) != -1) {
                url = "";
                pos += MUSTER_URL.length();
                pos1 = pos;
                if ((pos2 = seiteTivi_1.indexOf("<", pos1)) != -1) {
                    url = seiteTivi_1.substring(pos1, pos2);
                    if (url.contains("%2F")) {
                        url = url.replace("%2F", "/");
                    }
//                    url = URLDecoder.decode(url, "UTF-8");
                }
                if (url.equals("")) {
                    Log.errorLog(309075109, "keine URL");
                } else {
                    url = "http://www.tivi.de/tiviVideos" + url;
                    listeThemen.addUrl(new String[]{url});
                }
            }
        } catch (Exception ex) {
            Log.errorLog(302010498, ex);
        }
    }

    private void add_2() {
        //<ns3:headline>Nachrichten</ns3:headline>
        //<ns3:image>/tiviVideos/contentblob/2063212/tivi9teaserbild/9050138</ns3:image>
        //<ns3:page>/tiviVideos/beitrag/pur%2B+Sendungen/895212/2063212?view=flashXml</ns3:page>
        //<ns3:text>Ich will die Wahrheit!</ns3:text>
        final String MUSTER_URL = "<ns3:page>/tiviVideos/beitrag";
        MSStringBuilder seiteTivi_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrlIo.getUri(SENDERNAME, "http://www.tivi.de/tiviVideos/?view=flashXml", Const.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        ///seiteTivi_1 = getUrl.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/?view=xml", MSearchConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            Log.errorLog(645121326, "Leere Seite");
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        try {
            while ((pos = seiteTivi_1.indexOf(MUSTER_URL, pos)) != -1) {
                url = "";
                pos += MUSTER_URL.length();
                pos1 = pos;
                if ((pos2 = seiteTivi_1.indexOf("<", pos1)) != -1) {
                    url = seiteTivi_1.substring(pos1, pos2);
                    if (url.contains("%2F")) {
                        url = url.replace("%2F", "/");
                    }
//                    url = URLDecoder.decode(url, "UTF-8");
                }
                if (url.equals("")) {
                    Log.errorLog(915263985, "keine URL");
                } else {
                    url = "http://www.tivi.de/tiviVideos/beitrag" + url;
                    listeThemen.addUrl(new String[]{url});
                }
            }
        } catch (Exception ex) {
            Log.errorLog(730169702, ex);
        }
    }

    private void add_3() {
        final String MUSTER_URL = "type=\"broadcast\">";
        MSStringBuilder seiteTivi_1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrlIo.getUri(SENDERNAME, "http://www.tivi.de/tiviVideos/navigation?view=flashXml", Const.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            Log.errorLog(195623078, "Leere Seite");
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        try {
            while ((pos = seiteTivi_1.indexOf(MUSTER_URL, pos)) != -1) {
                url = "";
                pos += MUSTER_URL.length();
                pos1 = pos;
                if ((pos2 = seiteTivi_1.indexOf("<", pos1)) != -1) {
                    url = seiteTivi_1.substring(pos1, pos2);
                    if (url.contains("%2F")) {
                        url = url.replace("%2F", "/");
                    }
//                    url = URLDecoder.decode(url, "UTF-8");
                }
                if (url.equals("")) {
                    Log.errorLog(152378787, "keine URL");
                } else {
                    url = "http://www.tivi.de" + url;
                    listeThemen_3.addUrl(new String[]{url});
                }
            }
        } catch (Exception ex) {
            Log.errorLog(906037912, ex);
        }
    }

    private class ThemaLaden implements Runnable {

        GetUrl getUrl = new GetUrl(wartenSeiteLaden);
        MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    addTivi_(link[0] /* url */);
                    meldungProgress(link[0]);
                }
                while (!Config.getStop() && (link = listeThemen_3.getListeThemen()) != null) {
                    add_(link[0] /* url */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                Log.errorLog(731214569, ex);
            }
            meldungThreadUndFertig();
        }

        private void add_(String url_) {
            final String MUSTER_START = "<ns3:video-teaser>";
            seite2 = getUrlIo.getUri(SENDERNAME, url_, Const.KODIERUNG_UTF, 1 /* versuche */, seite2, "" /* Meldung */);
            if (seite2.length() == 0) {
                Log.errorLog(302010698, "Leere Seite");
            }
            int pos = 0;
            String url;
            try {
                while (!Config.getStop() && (pos = seite2.indexOf(MUSTER_START, pos)) != -1) {
                    pos += MUSTER_START.length();
                    url = seite2.extract("<ns3:page>", "<", pos);
//                    url = URLDecoder.decode(url, "UTF-8");
                    if (url.equals("")) {
                        Log.errorLog(732698720, "keine URL");
                    } else {
                        if (url.contains("%2F")) {
                            url = url.replace("%2F", "/");
                        }
                        url = "http://www.tivi.de" + url;
                        addTivi_(url);
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(701212145, ex);
            }
        }

        private void addTivi_(String url) {
            int pos3;
            long dauerL;
            String titel, thema, urlFilm, datum, zeit = "", bild, website, dauer, text;
            try {
                urlFilm = "";
                // Film laden
                meldung(url);
                seite1 = getUrl.getUri_Utf(SENDERNAME, url, seite1, "" /* Meldung */);
                if (seite1.length() == 0) {
                    Log.errorLog(301649897, "Leere Seite Tivi-2: " + url);
                    return;
                }
                thema = seite1.extract("<title>", "<");
                titel = seite1.extract("<subtitle>", "<");
                text = seite1.extract("<text>", "<");
                bild = seite1.extract("<image>", "<");
                if (!bild.isEmpty()) {
                    bild = "http://www.tivi.de" + bild;
                }
                website = seite1.extract("<link>", "<");
                dauer = seite1.extract("<ns3:duration>", "<"); //<ns3:duration>P0Y0M0DT0H24M9.000S</ns3:duration>
                if (dauer.isEmpty()) {
                    //<duration>P0Y0M0DT0H1M55.000S</duration>
                    dauer = seite1.extract("<duration>", "<"); //<duration>P0Y0M0DT0H11M0.000S</duration>
                }
                try {
                    dauer = dauer.replace("P0Y0M0DT", "");
                    String h = dauer.substring(0, dauer.indexOf("H"));
                    int ih = Integer.parseInt(h);
                    String m = dauer.substring(dauer.indexOf("H") + 1, dauer.indexOf("M"));
                    int im = Integer.parseInt(m);
                    String s = dauer.substring(dauer.indexOf("M") + 1, dauer.indexOf("."));
                    int is = Integer.parseInt(s);
                    dauerL = ih * 60 * 60 + im * 60 + is;
                } catch (Exception ex) {
                    dauerL = 0;
                    Log.errorLog(349761012, ex, "Dauer: " + url);
                }
                zeit = "";
                datum = seite1.extract("<airTime>", "<");
                //<airTime>2014-01-19T08:35:00.000+01:00</airTime>
                try {
                    Date filmDate = sdfIn.parse(datum);
                    datum = sdfOut_date.format(filmDate);
                    zeit = sdfOut_time.format(filmDate);
                } catch (Exception ex) {
                    Log.errorLog(649600299, ex, "Datum: " + url);
                }
                pos3 = 0;
                while ((pos3 = seite1.indexOf("<ns4:quality>veryhigh</ns4:quality>", pos3)) != -1) {
                    pos3 += 5;
                    urlFilm = seite1.extract("<ns4:url>", "<", pos3);
                    if (urlFilm.startsWith("http") && urlFilm.endsWith("mp4") && !urlFilm.contains("metafilegenerator")) {
                        break;
                    }
                }
                if (urlFilm.isEmpty()) {
                    Log.errorLog(159876234, "kein Film: " + url);
                } else {
                    if (urlFilm.startsWith("http://tvdl.zdf.de")) {
                        urlFilm = urlFilm.replace("http://tvdl.zdf.de", "http://nrodl.zdf.de");
                    }

                    // public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                    //        String datum, String zeit,
                    //        long dauerSekunden, String description, String imageUrl, String[] keywords) {
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, website, titel, urlFilm, "" /*urlRtmp*/,
                            datum, zeit,
                            dauerL, text);
                    // jetzt noch manuell die Auflösung hochsetzen
                    MediathekZdf.urlTauschen(film, url, mSearchFilmeSuchen);
                    addFilm(film);
                }
            } catch (Exception ex) {
                Log.errorLog(454123698, ex);
            }
        }

    }

}
