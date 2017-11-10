/*
 *    MediathekView
 *    Copyright (C) 2008 - 2012     W. Xaver
 *                              &   thausherr
 * 
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
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

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class MediathekRbb extends MediathekReader
{

    public final static Sender SENDER = Sender.RBB;
    private static final String HTTP_PREFIX = "http://";
    //final static String ROOTADR = "http://mediathek.rbb-online.de";

    public MediathekRbb(FilmeSuchen ssearch, int startPrio)
    {
        super(ssearch, SENDER.getName(),/* threads */ 2, /* urlWarten */ 100, startPrio);
    }

    @Override
    protected void addToList()
    {
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        // <a href="/tv/kurz-vor-5/Sendung?documentId=16272574&amp;bcastId=16272574" class="textLink">
        ArrayList<String> liste = new ArrayList<>();
        final String ADRESSE_1 = "http://mediathek.rbb-online.de/tv/sendungen-a-z?cluster=a-k";
        final String ADRESSE_2 = "http://mediathek.rbb-online.de/tv/sendungen-a-z?cluster=l-z";
        final String URL = "<a href=\"/tv/";
        meldungStart();
        try
        {
            GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite = getUrlIo.getUri(SENDER.getName(), ADRESSE_1, StandardCharsets.UTF_8, 5 /* versuche */, seite, "" /* Meldung */);
            seite.extractList("", "", URL, "\"", "", liste);
            seite = getUrlIo.getUri(SENDER.getName(), ADRESSE_2, StandardCharsets.UTF_8, 5 /* versuche */, seite, "" /* Meldung */);
            seite.extractList("", "", URL, "\"", "", liste);
            for (String s : liste)
            {
                if (s.isEmpty() || !s.contains("documentId="))
                {
                    continue;
                }
                s = "http://mediathek.rbb-online.de/tv/" + s;
                listeThemen.add(new String[]{s});
            }
        } catch (Exception ex)
        {
            Log.errorLog(398214058, ex);
        }
        if (Config.getStop())
        {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty())
        {
            meldungThreadUndFertig();
        } else
        {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t)
            {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden(false /*addTage*/));
                th.setName(SENDER.getName() + t);
                th.start();
            }
            meldungAddMax(7 /* Tage */);
            Thread th = new ThemaLaden(true /*addTage*/);
            th.setName(SENDER.getName() + "_Tage");
            th.start();
        }
    }

    private class ThemaLaden extends Thread
    {

        private boolean addTage = false;
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        public ThemaLaden(boolean addTage)
        {
            super();
            this.addTage = addTage;
        }

        @Override
        public void run()
        {
            try
            {
                meldungAddThread();
                if (addTage)
                {
                    addTage();
                } else
                {
                    final Iterator<String[]> themaIterator = listeThemen.iterator();
                    while (!Config.getStop() && themaIterator.hasNext())
                    {
                        final String[] thema = themaIterator.next();
                        meldungProgress(thema[0]);
                        addThema(thema[0] /* url */, true);
                    }
                }
            } catch (Exception ex)
            {
                Log.errorLog(794625882, ex);
            }
            meldungThreadUndFertig();
        }

        private void addTage()
        {
            // http://mediathek.rbb-online.de/tv/sendungVerpasst?topRessort=tv&kanal=5874&tag=0
            final String MUSTER_START = "<h2 class=\"modHeadline\">7 Tage Rückblick</h2>";
            final String MUSTER_URL = "<div class=\"media mediaA\">";
            final String URL = "<a href=\"/tv/";
            String urlTage;
            for (int i = 0; i <= 6; ++i)
            {
                urlTage = "http://mediathek.rbb-online.de/tv/sendungVerpasst?topRessort=tv&tag=" + i;
                meldungProgress(urlTage);
                GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrlIo.getUri_Utf(SENDER.getName(), urlTage, seite1, "");
                int pos1 = seite1.indexOf(MUSTER_START);
                while (!Config.getStop() && (pos1 = seite1.indexOf(MUSTER_URL, pos1)) != -1)
                {
                    pos1 += MUSTER_URL.length();
                    String urlSeite = seite1.extract(URL, "\"", pos1);
                    if (!urlSeite.isEmpty())
                    {
                        urlSeite = urlSeite.replaceAll("&amp;", "&");
                        urlSeite = "http://mediathek.rbb-online.de/tv/" + urlSeite;
                        addFilme(urlSeite);
                    } else
                    {
                        Log.errorLog(751203697, "keine URL für: " + urlSeite);
                    }
                }

            }
        }

        private void addThema(String url, boolean weiter)
        {
            try
            {
                final String URL = "<a href=\"/tv/";
                final String MUSTER_URL = "<div class=\"media mediaA\">";
                GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrlIo.getUri_Utf(SENDER.getName(), url, seite1, "");
                int pos1 = seite1.indexOf("<div class=\"entry\">");
                while (!Config.getStop() && (pos1 = seite1.indexOf(MUSTER_URL, pos1)) != -1)
                {
                    pos1 += MUSTER_URL.length();
                    String urlSeite = seite1.extract(URL, "\"", pos1);
                    if (!urlSeite.isEmpty())
                    {
                        urlSeite = urlSeite.replaceAll("&amp;", "&");
                        urlSeite = "http://mediathek.rbb-online.de/tv/" + urlSeite;
                        addFilme(urlSeite);
                    } else
                    {
                        Log.errorLog(751203697, "keine URL für: " + url);
                    }
                }

                // noch nach weiteren Seiten suchen
                if (weiter && CrawlerTool.loadLongMax())
                {
                    for (int i = 2; i < 10; ++i)
                    {
                        if (seite1.indexOf("mcontents=page." + i) != -1)
                        {
                            // dann gibts weiter Seiten
                            addThema(url + "&mcontents=page." + i, false);
                        }
                    }
                }
            } catch (Exception ex)
            {
                Log.errorLog(541236987, ex);
            }
        }

        private void addFilme(String urlSeite)
        {
            try
            {
                meldung(urlSeite);
                String datum = "", zeit = "", thema, title, description, durationInSeconds;
                GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
                seite2 = getUrlIo.getUri_Utf(SENDER.getName(), urlSeite, seite2, "");
                description = seite2.extract("<meta name=\"description\" content=\"", "\"");
                durationInSeconds = seite2.extract("<meta property=\"video:duration\" content=\"", "\"");
                long duration = 0;
                if (!durationInSeconds.isEmpty())
                {
                    try
                    {
                        duration = Long.parseLong(durationInSeconds);
                    } catch (Exception ex)
                    {
                        Log.errorLog(200145787, ex);
                        duration = 0;
                    }
                }
                title = seite2.extract("<meta name=\"dcterms.title\" content=\"", "\"");
                thema = seite2.extract("<meta name=\"dcterms.isPartOf\" content=\"", "\"");
                String sub = seite2.extract("<p class=\"subtitle\">", "<");
                if (sub.contains("|"))
                {
                    datum = sub.substring(0, sub.indexOf('|') - 1);
                    datum = datum.substring(datum.indexOf(' ')).trim();
                    zeit = datum.substring(datum.indexOf(' ')).trim();
                    if (zeit.length() == 5)
                    {
                        zeit = zeit + ":00";
                    }
                    datum = datum.substring(0, datum.indexOf(' ')).trim();
                    if (datum.length() == 8)
                    {
                        datum = datum.substring(0, 6) + "20" + datum.substring(6);
                    }
                }


                String urlFilm = urlSeite.substring(urlSeite.indexOf("documentId=") + "documentId=".length());
                // http://mediathek.rbb-online.de/play/media/24938774?devicetype=pc&features=hls
                urlFilm = "http://mediathek.rbb-online.de/play/media/" + urlFilm + "?devicetype=pc&features=hls";
                seite3 = getUrlIo.getUri_Utf(SENDER.getName(), urlFilm, seite3, "");
                String urlNormal = "", urlLow = "";


                urlLow = getUrlLow("https");
                if(urlLow.isEmpty())
                {
                    urlLow =getUrlLow("http");
                    if(!urlLow.isEmpty())
                    {
                        urlLow = "http://" + urlLow;
                    }
                }else {
                    urlLow = "https://" + urlLow;
                }

                urlNormal = getUrlNormal("https");
                if(urlNormal.isEmpty())
                {
                    urlNormal =getUrlNormal("http");
                    if(!urlNormal.isEmpty())
                    {
                        urlNormal = "http://" + urlNormal;
                    }
                }else {
                    urlNormal = "https://" + urlNormal;
                }
                //http://http-stream.rbb-online.de/rbb/rbbreporter/rbbreporter_20151125_solange_ich_tanze_lebe_ich_WEB_L_16_9_960x544.mp4?url=5
                if (urlLow.contains("?url="))
                {
                    urlLow = urlLow.substring(0, urlLow.indexOf("?url="));
                }
                if (urlNormal.contains("?url="))
                {
                    urlNormal = urlNormal.substring(0, urlNormal.indexOf("?url="));
                }
                if (urlNormal.isEmpty())
                {
                    if (!urlLow.isEmpty())
                    {
                        urlNormal = urlLow;
                        urlLow = "";
                    }
                }

                // ,"_subtitleUrl":"/subtitle/19088","_subtitleOffset":0,
                // http://mediathek.rbb-online.de/subtitle/19088
                String subtitle = seite3.extract("subtitleUrl\":\"", "\"");
                if (!subtitle.isEmpty())
                {
                    if (!subtitle.startsWith("http"))
                    {
                        subtitle = "http://mediathek.rbb-online.de" + subtitle;
                    }
                }
                if (datum.isEmpty() || zeit.isEmpty() || thema.isEmpty() || title.isEmpty() || description.isEmpty() || durationInSeconds.isEmpty())
                {
                    Log.errorLog(912012036, "empty für: " + urlSeite);
                }
                if (!urlNormal.isEmpty())
                {

                    Film film = CrawlerTool.createFilm(SENDER,
                            urlNormal,
                            title,
                            thema,
                            datum,
                            zeit,
                            duration,
                            urlSeite,
                            description,
                            "",
                            urlLow);
                    if (!subtitle.isEmpty())
                    {
                        film.addSubtitle(new URI(subtitle));
                    }
                    addFilm(film);
                } else
                {
                    Log.errorLog(302014569, "keine URL für: " + urlSeite);
                }
            } catch (Exception ex)
            {
                Log.errorLog(541236987, ex);
            }
        }

        private String getUrlNormal(String aProtocol)
        {
            String urlNormal;
            urlNormal = seite3.extract("\"_quality\":3,\"_server\":\"\",\"_cdn\":\"akamai\",", "\"_stream\":\""+aProtocol+"://", "\"");
            if (urlNormal.isEmpty()) {
                urlNormal = seite3.extract("\"_quality\":3,\"_server\":\"\",\"_cdn\":\"default\"", "\"_stream\":\""+aProtocol+"://", "\"");
            }
            if (urlNormal.isEmpty()) {
                urlNormal = seite3.extract("\"_quality\":3,\"_server\":\"\",\"_cdn\":\"default\",\"_stream\":\""+aProtocol+"://", "\"");
            }
            return urlNormal;
        }

        private String getUrlLow(String aProtocol)
        {
            String urlLow;
            urlLow = seite3.extract("\"_quality\":1,\"_server\":\"\",\"_cdn\":\"akamai\",\"_stream\":\""+aProtocol+"://", "\"");
            if (urlLow.isEmpty()) {
                urlLow = seite3.extract("\"_quality\":1,\"_server\":\"\",\"_cdn\":\"default\",\"_stream\":\""+aProtocol+"://", "\"");
            }
            return urlLow;
        }

    }
}
