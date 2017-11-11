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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

public class MediathekSr extends MediathekReader
{

    public final static Sender SENDER = Sender.SR;
    private static final String SUBTITLE_PREFIX = "http://sr-mediathek.sr-online.de/";

    /**
     * @param ssearch
     * @param startPrio
     */
    public MediathekSr(final FilmeSuchen ssearch, final int startPrio)
    {
        super(ssearch, SENDER.getName(), /* threads */ 2, /* urlWarten */ 100, startPrio);
    }

    /**
     *
     */
    @Override
    public void addToList()
    {
        meldungStart();
        // seite1:
        // http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=1&o=d
        // seite2:
        // http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=2&o=d
        // seite3:
        // http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=3&o=d
        int maxSeiten = 15;
        if (CrawlerTool.loadLongMax())
        {
            maxSeiten = 120;
        }
        for (int i = 1; i < maxSeiten; ++i)
        {
            // http://www.sr-mediathek.de/index.php?seite=2&f=v&s=1&o=d
            final String[] add = new String[]
            { "http://www.sr-mediathek.de/index.php?seite=2&f=v&s=" + i + "&o=d",
                    ""/* thema */ };
            listeThemen.add(add);
        }

        if (Config.getStop())
        {
            meldungThreadUndFertig();
        }
        else if (listeThemen.isEmpty())
        {
            meldungThreadUndFertig();
        }
        else
        {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < getMaxThreadLaufen(); ++t)
            {
                final Thread th = new ThemaLaden();
                th.setName(SENDER.getName() + t);
                th.start();
            }
        }
    }

    private class ThemaLaden extends Thread
    {

        private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> erg = new ArrayList<>();

        @Override
        public void run()
        {
            try
            {
                meldungAddThread();
                final Iterator<String[]> themaIterator = listeThemen.iterator();
                while (!Config.getStop() && themaIterator.hasNext())
                {
                    final String[] thema = themaIterator.next();
                    meldungProgress(thema[0] /* url */);
                    bearbeiteTage(thema[0]/* url */);
                }
            }
            catch (final Exception ex)
            {
                Log.errorLog(951236547, ex);
            }
            meldungThreadUndFertig();
        }

        private void bearbeiteTage(final String urlSeite)
        {
            final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
            seite1 = getUrlIo.getUri_Utf(SENDER.getName(), urlSeite, seite1, "");
            seite1.extractList("<h3 class=\"teaser__text__header\">", "<a href=\"index.php?seite=", "\"", erg);
            for (final String url : erg)
            {
                if (Config.getStop())
                {
                    break;
                }
                addFilme("http://www.sr-mediathek.de/index.php?seite=" + url);
            }
            erg.clear();
        }

        private void addFilme(final String urlSeite)
        {
            meldung(urlSeite);
            seite2 = getUrl.getUri_Utf(SENDER.getName(), urlSeite, seite2, "");
            try
            {
                String url;
                String datum;
                String titel, thema = SENDER.getName();
                long duration = 0;
                String description;

                final String d = seite2.extract("| Dauer: ", "|").trim();
                try
                {
                    if (!d.isEmpty())
                    {
                        duration = 0;
                        final String[] parts = d.split(":");
                        long power = 1;
                        for (int i = parts.length - 1; i >= 0; i--)
                        {
                            duration += Long.parseLong(parts[i]) * power;
                            power *= 60;
                        }
                    }
                }
                catch (final Exception ex)
                {
                    Log.errorLog(732012546, "d: " + d);
                }
                description = seite2.extract("<meta property=\"og:description\" content=\"", "\"");
                datum = seite2.extract("Video | ", "|").trim();
                // <title>SR-Mediathek.de: Wir im Saarland – Warten aufs
                // Christkind</title>
                // <title>SR-Mediathek.de: mags spezial:
                // Innewennzisch-Ausewennzisch (08.10.2015)</title>
                // <title>SR-Mediathek.de: Wir im Saarland – Ein Kommissar als
                // Zeitschenker (20.11.2015)</title>
                titel = seite2.extract("<title>", "<");
                if (titel.startsWith("SR-Mediathek.de:"))
                {
                    titel = titel.replaceFirst("SR-Mediathek.de:", "");
                }
                if (titel.contains(" - "))
                {
                    thema = titel.substring(0, titel.indexOf(" - ")).trim();
                    titel = titel.substring(titel.indexOf(" - ") + 3).trim();
                }
                else if (titel.contains(": "))
                {
                    thema = titel.substring(0, titel.indexOf(": ")).trim();
                    titel = titel.substring(titel.indexOf(": ") + 2).trim();
                }
                else if (titel.contains(" – "))
                {
                    thema = titel.substring(0, titel.indexOf(" – ")).trim();
                    titel = titel.substring(titel.indexOf(" – ") + 3).trim();
                }
                else if (titel.contains("("))
                {
                    thema = titel.substring(0, titel.indexOf('(')).trim();
                    // titel = titel.substring(titel.indexOf("(") + 1).trim();
                    // titel = titel.replace(")", "");
                }
                String subtitle = seite2.extract("http_get.utPath", "= '", "'"); // http_get.utPath
                                                                                 // =
                                                                                 // 'ut/AB_20150228.xml';
                url = seite2.extract("var mediaURLs = ['", "'");
                if (url.isEmpty())
                {
                    Log.errorLog(301245789, "keine URL für: " + urlSeite);
                }
                else
                {
                    if (!subtitle.isEmpty() && !subtitle.startsWith(SUBTITLE_PREFIX))
                    {
                        subtitle = SUBTITLE_PREFIX + subtitle;
                    }
                    final Film film = CrawlerTool.createFilm(SENDER, url, titel, thema, datum, "", duration, urlSeite,
                            description, "", "");
                    if (!subtitle.isEmpty())
                    {
                        film.addSubtitle(new URL(subtitle));
                    }
                    addFilm(film);
                }
            }
            catch (final Exception ex)
            {
                Log.errorLog(402583366, ex);
            }
        }

    }
}
