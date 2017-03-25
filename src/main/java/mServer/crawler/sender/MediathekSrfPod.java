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
import java.util.Locale;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

public class MediathekSrfPod extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.SRF_PODCAST;
    private MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    public MediathekSrfPod(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 2, /* urlWarten */ 200, startPrio);
    }

    @Override
    public void addToList() {
        //Liste von http://www.sf.tv/podcasts/index.php holen
        //http://www.podcast.sf.tv/Podcasts/al-dente
        // class="" href="/Podcasts/al-dente" rel="2" >
        final String MUSTER_1 = "value=\"http://feeds.sf.tv/podcast";
        final String MUSTER_2 = "value=\"http://pod.drs.ch/";
        String addr1 = "http://www.srf.ch/podcasts";
        listeThemen.clear();
        meldungStart();
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri_Utf(SENDERNAME, addr1, seite, "");
        int pos = 0;
        int pos1;
        int pos2;
        String url = "";
        while (!Config.getStop() && (pos = seite.indexOf(MUSTER_1, pos)) != -1) {
            pos += MUSTER_1.length();
            pos1 = pos;
            pos2 = seite.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1) {
                url = seite.substring(pos1, pos2);
                url = "http://feeds.sf.tv/podcast" + url;
            }
            if (url.isEmpty()) {
                Log.errorLog(698875503, "keine URL");
            } else {
                String[] add = new String[]{url, ""};
                listeThemen.addUrl(add);
            }
        }
        pos = 0;
        while (!Config.getStop() && (pos = seite.indexOf(MUSTER_2, pos)) != -1) {
            pos += MUSTER_2.length();
            pos1 = pos;
            pos2 = seite.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1) {
                url = seite.substring(pos1, pos2);
                url = "http://pod.drs.ch/" + url;
            }
            if (url.isEmpty()) {
                Log.errorLog(698875503,  "keine URL");
            } else {
                String[] add = new String[]{url, ""};
                listeThemen.addUrl(add);
            }
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

    private class ThemaLaden extends Thread {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addFilme(link[1], link[0] /* url */);
                }
            } catch (Exception ex) {
                Log.errorLog(286931004,   ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String thema, String strUrlFeed) {
            //<title>al dente - Podcasts - Schweizer Fernsehen</title>
            // <h2 class="sf-hl1">al dente vom 28.06.2010</h2>
            // <li><a href="
            // <pubDate>Wed, 21 Nov 2012 00:02:00 +0100</pubDate>
            final String MUSTER_THEMA_1 = "<title>";
            final String MUSTER_THEMA_2 = "</title>";
            final String MUSTER_URL_1 = "url=\"http://";
            final String MUSTER_DATE = "<pubDate>";
            final String MUSTER_DURATION = "<itunes:duration>";
            final String MUSTER_DESCRIPTION = "<itunes:summary>";
            final String MUSTER_IMAGE = "<itunes:image href=\"";
            final String MUSTER_KEYWORDS = "<itunes:keywords>";
            int pos = 0, pos1;
            int pos2;
            String titel;
            String url = "";
            String datum, zeit;
            long duration = 0;
            String description = "";
            String image = "";
            String[] keywords;
            try {
                meldung(strUrlFeed);
                seite = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, seite, "Thema: " + thema);
                if ((pos1 = seite.indexOf(MUSTER_THEMA_1)) != -1) {
                    pos1 = pos1 + MUSTER_THEMA_1.length();
                }
                if ((pos2 = seite.indexOf(MUSTER_THEMA_2, pos1)) != -1) {
                    thema = seite.substring(pos1, pos2).trim();
//                        if (thema.contains(" ")) {
//                            thema = thema.substring(0, thema.indexOf(" "));
//                        }
                }
                // Image of show (unfortunatly we do not have an custom image for each entry
                // <itunes:image href="http://api-internet.sf.tv/xmlservice/picture/1.0/vis/videogroup/c3d7c0d6-5250-0001-a1ac-edeb183b17d8/0003" />
                int pos3 = seite.indexOf(MUSTER_IMAGE);
                if (pos3 != -1) {
                    pos3 += MUSTER_IMAGE.length();
                    int pos4 = seite.indexOf("\"", pos3);
                    if (pos4 != -1) {
                        image = seite.substring(pos3, pos4);
                    }
                }
                while ((pos = seite.indexOf(MUSTER_THEMA_1, pos)) != -1) { //start der Einträge, erster Eintrag ist der Titel
                    pos += MUSTER_THEMA_1.length();
                    pos1 = pos;
                    int pos5;
                    String d = "";
                    if ((pos5 = seite.indexOf(MUSTER_DURATION, pos)) != -1) {
                        pos5 += MUSTER_DURATION.length();
                        if ((pos2 = seite.indexOf("</", pos5)) != -1) {
                            d = seite.substring(pos5, pos2);
                            if (!d.isEmpty()) {
                                try {
                                    if (d.contains(".")) {
                                        d = d.replace(".", ""); // sind dann ms
                                        duration = Long.parseLong(d);
                                        duration = duration / 1000;
                                    } else if (d.contains(":")) {
                                        duration = 0;
                                        String[] parts = d.split(":");
                                        long power = 1;
                                        for (int i = parts.length - 1; i >= 0; i--) {
                                            duration += Long.parseLong(parts[i]) * power;
                                            power *= 60;
                                        }
                                    } else {
                                        // unfortunately the duration tag can be empty :-(
                                        duration = Long.parseLong(d);
                                    }
                                } catch (Exception ex) {
                                    Log.errorLog(915263987,   "d: " + d);
                                }
                            }
                        }
                    }
                    if (duration == 0) {
                        if (!d.equals("0")) {
                            Log.errorLog(915159637,   "keine Dauer");
                        }
                    }
                    if ((pos5 = seite.indexOf(MUSTER_DESCRIPTION, pos)) != -1) {
                        pos5 += MUSTER_DESCRIPTION.length();
                        if ((pos2 = seite.indexOf("</", pos5)) != -1) {
                            description = seite.substring(pos5, pos2);
                        }
                    }
                    if ((pos5 = seite.indexOf(MUSTER_KEYWORDS, pos)) != -1) {
                        pos5 += MUSTER_KEYWORDS.length();
                        if ((pos2 = seite.indexOf("</", pos5)) != -1) {
                            String k = seite.substring(pos5, pos2);
                            if (!k.isEmpty()) {
                                keywords = k.split(",");
                                for (int i = 0; i < keywords.length; i++) {
                                    keywords[i] = keywords[i].trim();
                                }
                            }
                        }
                    }
                    if ((pos2 = seite.indexOf(MUSTER_THEMA_2, pos1)) != -1) {
                        titel = seite.substring(pos1, pos2).trim();
                        datum = "";
                        zeit = "";
                        int p1, p2;
                        if ((p1 = seite.indexOf(MUSTER_DATE, pos1)) != -1) {
                            p1 += MUSTER_DATE.length();
                            if ((p2 = seite.indexOf("<", p1)) != -1) {
                                String tmp = seite.substring(p1, p2);
                                datum = convertDatum(tmp);
                                zeit = convertTime(tmp);
                            }
                        }
                        if ((pos1 = seite.indexOf(MUSTER_URL_1, pos1)) != -1) {
                            pos1 += MUSTER_URL_1.length();
                            if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                                url = seite.substring(pos1, pos2);
                                url = "http://" + url;
                            }
                            if (url.isEmpty()) {
                                Log.errorLog(463820049,   "keine URL: " + strUrlFeed);
                            } else {
                                // public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String datum, String zeit,
                                //      long duration, String description, String thumbnailUrl, String imageUrl, String[] keywords) {
                                addFilm(new DatenFilm(SENDERNAME, thema, strUrlFeed, titel, url, ""/*rtmpURL*/, datum, zeit,
                                        duration, description));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(496352007,   ex);
            }
        }
    }

    private String convertDatum(String datum) {
        //<pubDate>Mon, 03 Jan 2011 17:06:16 +0100</pubDate>
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            Date filmDate = sdfIn.parse(datum);
            SimpleDateFormat sdfOut;
            sdfOut = new SimpleDateFormat("dd.MM.yyyy");
            datum = sdfOut.format(filmDate);
        } catch (Exception ex) {
            Log.errorLog(649600299,  ex);
        }
        return datum;
    }

    private String convertTime(String zeit) {
        //<pubDate>Mon, 03 Jan 2011 17:06:16 +0100</pubDate>
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            Date filmDate = sdfIn.parse(zeit);
            SimpleDateFormat sdfOut;
            sdfOut = new SimpleDateFormat("HH:mm:ss");
            zeit = sdfOut.format(filmDate);
        } catch (Exception ex) {
            Log.errorLog(663259004,  ex);
        }
        return zeit;
    }

}
