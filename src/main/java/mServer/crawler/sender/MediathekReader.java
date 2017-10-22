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

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.tool.GermanStringSorter;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class MediathekReader extends Thread
{
    private static final Logger LOG = LogManager.getLogger(MediathekReader.class);
    private final String sendername; // ist der Name, den der Mediathekreader hat, der ist eindeutig
    private final int maxThreadLaufen; //4; // Anzahl der Thread die parallel Suchen
    private final int wartenSeiteLaden; //ms, Basiswert zu dem dann der Faktor multipliziert wird, Wartezeit zwischen 2 Websiten beim Absuchen der Sender
    private final int startPrio; // es gibt die Werte: 0->startet sofort, 1->später und 2->zuletzt
    protected Set<String[]> listeThemen;
    protected FilmeSuchen mlibFilmeSuchen;
    private int threads; // aktuelle Anz. laufender Threads
    private int max; // Anz. zu suchender Themen
    private int progress; // Prograss eben

    public MediathekReader(FilmeSuchen aMSearchFilmeSuchen, String aSendername, int aSenderMaxThread, int aSenderWartenSeiteLaden, int aStartPrio)
    {
        mlibFilmeSuchen = aMSearchFilmeSuchen;

        maxThreadLaufen = aSenderMaxThread;
        wartenSeiteLaden = aSenderWartenSeiteLaden;
        startPrio = aStartPrio;
        sendername = aSendername;

        threads = 0;
        max = 0;
        progress = 0;
        listeThemen = Collections.synchronizedSet(new HashSet<>());
    }

    public static boolean urlExists(String url)
    {
        // liefert liefert true, wenn es die URL gibt
        // brauchts, um Filmurls zu prüfen
        if (!url.toLowerCase().startsWith("http"))
        {
            return false;
        } else
        {
            Request request = new Request.Builder().url(url).head().build();
            boolean result = false;

            try (Response response = MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute())
            {
                if (response.isSuccessful())
                {
                    result = true;
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
                result = false;
            }

            return result;
        }
    }

    protected static void listeSort(Set<String[]> aListe, int stelle)
    {
        List<String[]> liste = new ArrayList<>(aListe);
        //Stringliste alphabetisch sortieren
        GermanStringSorter sorter = GermanStringSorter.getInstance();
        if (liste != null)
        {
            String str1;
            String str2;
            for (int i = 1; i < liste.size(); ++i)
            {
                for (int k = i; k > 0; --k)
                {
                    str1 = liste.get(k - 1)[stelle];
                    str2 = liste.get(k)[stelle];
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (sorter.compare(str1, str2) > 0)
                    {
                        liste.add(k - 1, liste.remove(k));
                    } else
                    {
                        break;
                    }
                }
            }
        }
        aListe.clear();
        aListe.addAll(liste);
    }

    protected static long extractDuration(String dauer)
    {
        long dauerInSeconds = 0;
        if (dauer.isEmpty())
        {
            return 0;
        }
        try
        {
            if (dauer.contains("min"))
            {
                dauer = dauer.replace("min", "").trim();
                dauerInSeconds = Long.parseLong(dauer) * 60;
            } else
            {
                String[] parts = dauer.split(":");
                long power = 1;
                for (int i = parts.length - 1; i >= 0; i--)
                {
                    dauerInSeconds += Long.parseLong(parts[i]) * power;
                    power *= 60;
                }
            }
        } catch (Exception ex)
        {
            return 0;
        }
        return dauerInSeconds;
    }

    protected static long extractDurationSec(String dauer)
    {
        long dauerInSeconds;
        if (dauer.isEmpty())
        {
            return 0;
        }
        try
        {
            dauerInSeconds = Long.parseLong(dauer);
        } catch (Exception ex)
        {
            return 0;
        }
        return dauerInSeconds;
    }

    public String getSendername()
    {
        return sendername;
    }

    public int getMaxThreadLaufen()
    {
        return maxThreadLaufen;
    }

    public int getWartenSeiteLaden()
    {
        return wartenSeiteLaden;
    }

    public int getMax()
    {
        return max;
    }

    public int getProgress()
    {
        return progress;
    }

    public int getStartPrio()
    {
        return startPrio;
    }

    public int getThreads()
    {
        return threads;
    }

    public boolean checkNameSenderFilmliste(String name)
    {
        // ist der Name der in der Tabelle Filme angezeigt wird
        return getSendername().equalsIgnoreCase(name);
    }


    public void clear()
    {
        //aufräumen
    }

    @Override
    public void run()
    {
        //alles laden
        try
        {
            threads = 0;
            addToList();
        } catch (Exception ex)
        {
            Log.errorLog(397543600, ex, getSendername());
        }
    }

    protected abstract void addToList();

    protected void addFilm(Film film, boolean urlPruefen)
    {
        // es werden die gefundenen Filme in die Liste einsortiert
        if (urlPruefen)
        {
            if (mlibFilmeSuchen.listeFilmeNeu.getFilmByUrl(film.getUrl(Qualities.NORMAL).toString()) == null)
            {
                addFilm(film);
            }
        } else
        {
            addFilm(film);
        }
    }

    /**
     * Es werden die gefundenen Filme in die Liste einsortiert.
     *
     * @param aFilm der einzufügende Film
     */
    protected void addFilm(Film aFilm)
    {
        try
        {
            CrawlerTool.improveAufloesung(aFilm);
        } catch (URISyntaxException uriSyntaxEception)
        {
            LOG.error("Beim verbessern der Auflösung ist ein Fehler aufgetreten", uriSyntaxEception);
        }

        if (mlibFilmeSuchen.listeFilmeNeu.addFilmVomSender(aFilm))
        {
            // dann ist er neu
            FilmeSuchen.listeSenderLaufen.inc(aFilm.getSender().getName(), RunSender.Count.FILME);
        }
    }

    boolean istInListe(Set<String[]> liste, String str, int nr)
    {
        Optional<String[]> opt = liste.parallelStream().filter(f -> f[nr].equals(str)).findAny();

        return opt.isPresent();
    }

    boolean istInListe(LinkedList<String> liste, String str)
    {
        Optional<String> opt = liste.parallelStream().filter(f -> f.equals(str)).findAny();

        return opt.isPresent();
    }

    protected synchronized void meldungStart()
    {
        // meldet den Start eines Suchlaufs
        max = 0;
        progress = 0;
        Log.sysLog("===============================================================");
        Log.sysLog("Starten[" + ((CrawlerTool.loadLongMax()) ? "alles" : "update") + "] " + getSendername() + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        Log.sysLog("   maxThreadLaufen: " + getMaxThreadLaufen());
        Log.sysLog("   wartenSeiteLaden: " + getWartenSeiteLaden());
        Log.sysLog("");
        RunSender runSender = mlibFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
        runSender.maxThreads = getMaxThreadLaufen(); //runSender ist erst jetzt angelegt
        runSender.waitOnLoad = getWartenSeiteLaden();
    }

    protected synchronized void meldungAddMax(int mmax)
    {
        max = max + mmax;
        mlibFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
    }

    protected synchronized void meldungAddThread()
    {
        threads++;
        mlibFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
    }

    protected synchronized void meldungProgress(String text)
    {
        progress++;
        mlibFilmeSuchen.melden(getSendername(), getMax(), getProgress(), text);
    }

    protected synchronized void meldung(String text)
    {
        mlibFilmeSuchen.melden(getSendername(), getMax(), getProgress(), text);
    }

    protected synchronized void meldungThreadUndFertig()
    {
        // meldet das Ende eines!!! Threads
        // der MediathekReader ist erst fertig wenn alle gestarteten Threads fertig sind!!
        threads--;
        if (getThreads() <= 0)
        {
            //wird erst ausgeführt wenn alle Threads beendet sind
            mlibFilmeSuchen.meldenFertig(getSendername());
        } else
        {
            // läuft noch was
            mlibFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
        }
    }


    @SuppressWarnings("serial")
    class HashSetUrl extends HashSet<String[]>
    {
        public synchronized boolean addUrl(String[] e)
        {
            return add(e);
        }

        public synchronized String[] getListeThemen()
        {
            String[] res = null;

            Iterator<String[]> it = iterator();
            if (it.hasNext())
            {
                res = it.next();
                remove(res);
            }

            return res;
        }

    }


}
