/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.tool.MserverDaten;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static mServer.crawler.CrawlerTool.getFileSize;

public class AddToFilmlist
{
    /**
     * Minimum size of films in MiB to be included in new list.
     */
    private static final String THEMA_LIVE = "Livestream";
    private static final int MIN_SIZE_ADD_OLD = 5;
    private final static int NUMBER_OF_THREADS = 32;//(Runtime.getRuntime().availableProcessors() * Runtime.getRuntime().availableProcessors()) / 2;
    private final ListeFilme vonListe;
    private final ListeFilme listeEinsortieren;
    /**
     * List of all locally started import threads.
     */
    private final ArrayList<ImportOldFilmlistThread> threadList = new ArrayList<>();
    private AtomicInteger threadCounter = new AtomicInteger(0);


    public AddToFilmlist(ListeFilme vonListe, ListeFilme listeEinsortieren)
    {
        this.vonListe = vonListe;
        this.listeEinsortieren = listeEinsortieren;
    }

    public synchronized void addLiveStream()
    {
        if (listeEinsortieren.size() <= 0) return;

        vonListe.removeIf(f -> f.getThema().equals(THEMA_LIVE));
        listeEinsortieren.forEach(vonListe::add);
    }

    private void removeExisting(final int size)
    {
        listeEinsortieren.removeIf((f) -> vonListe.contains(f));

        Log.sysLog("===== Liste einsortieren Title =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");
    }


    /**
     * Remove links which don´t start with http.
     * -    * Remove old film entries which are smaller than MIN_SIZE_ADD_OLD.
     */
    private void performInitialCleanup()
    {
        listeEinsortieren.removeIf(f -> !f.getUrl(Qualities.NORMAL).toString().toLowerCase().startsWith("http"));
        listeEinsortieren.removeIf(f -> f.getFileSize(Qualities.NORMAL) != null && f.getFileSize(Qualities.NORMAL) < MIN_SIZE_ADD_OLD);
    }

    private void startThreads()
    {
        final OkHttpClient client = MVHttpClient.getInstance().getReducedTimeOutClient();

        List syncList = Collections.synchronizedList(listeEinsortieren);
        for (int i = 0; i < NUMBER_OF_THREADS; ++i)
        {
            ImportOldFilmlistThread t = new ImportOldFilmlistThread(syncList, client);
            t.setName("ImportOldFilmlistThread Thread-" + i);
            threadList.add(t);
            t.start();
        }
    }

    private void stopThreads()
    {
        if (Config.getStop())
        {
            for (ImportOldFilmlistThread t : threadList)
                t.interrupt();
            for (ImportOldFilmlistThread t : threadList)
            {
                try
                {
                    t.join();
                } catch (InterruptedException ignored)
                {
                }
            }
        }
    }

    /*
     * Diese Methode sortiert eine vorhandene Liste in eine andere Filmliste ein, 
     * dabei werden nur nicht vorhandene Filme einsortiert.
     */
    public int addOldList()
    {
        threadCounter = new AtomicInteger(0);
        performInitialCleanup();

        int size = listeEinsortieren.size();

        removeExisting(size);

        size = listeEinsortieren.size();
        long oldSize = size;

        startThreads();

        int count = 0;
        while (!Config.getStop() && threadCounter.get() > 0)
        {
            try
            {
                count++;
                if (count % 5 == 0)
                {
                    long curSize = listeEinsortieren.size();
                    System.out.println("Liste: " + curSize);
                    System.out.println("Entfernte Einträge: " + ((oldSize - curSize)));
                    oldSize = curSize;
                }
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ex)
            {
                Log.errorLog(978451205, ex, "Fehler beim Import Old");
            }
        }

        stopThreads();

        final int treffer = retrieveThreadResults();

        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer);
        Log.sysLog("");
        return treffer;
    }

    /**
     * Add all local thread results to the filmlist.
     *
     * @return the total number of entries found.
     */
    private int retrieveThreadResults()
    {
        int treffer = 0;
        for (ImportOldFilmlistThread t : threadList)
        {
            final ArrayList<Film> localList = t.getLocalAddList();
            if (MserverDaten.debug)
                Log.sysLog("Thread " + t.getName() + " list size: " + localList.size());
            vonListe.addAll(localList);
            localList.clear();
            treffer += t.getTreffer();
        }
        return treffer;
    }

    private class ImportOldFilmlistThread extends Thread
    {

        private final List<Film> listeOld;
        private final ArrayList<Film> localAddList = new ArrayList<>((vonListe.size() / NUMBER_OF_THREADS) + 500);
        private int treffer = 0;
        private OkHttpClient client = null;

        public ImportOldFilmlistThread(List<Film> listeOld, OkHttpClient client)
        {
            this.listeOld = listeOld;
            threadCounter.incrementAndGet();
            this.client = client;
        }

        public int getTreffer()
        {
            return treffer;
        }

        public ArrayList<Film> getLocalAddList()
        {
            return localAddList;
        }

        private void addOld(Film film)
        {
            treffer++;

            localAddList.add(film);
        }

        private synchronized Film popOld(List<Film> listeOld)
        {
            if (!listeOld.isEmpty())
            {
                return listeOld.remove(0);
            } else
                return null;
        }

        @Override
        public void run()
        {

            Film film;
            while (!isInterrupted() && (film = popOld(listeOld)) != null)
            {
                final String url = film.getUrl(Qualities.NORMAL).toString();
                if (film.getFileSize(Qualities.NORMAL) == null)
                {
                    long fileSize = CrawlerTool.getFileSize(film.getUrl(Qualities.NORMAL));

                    if (fileSize > MIN_SIZE_ADD_OLD)
                    {
                        addOld(film);
                    }
                } else
                {
                    if (film.getFileSize(Qualities.NORMAL) != null && film.getFileSize(Qualities.NORMAL) > MIN_SIZE_ADD_OLD)
                    {
                        Request request = new Request.Builder().url(url).head().build();
                        try (Response response = client.newCall(request).execute())
                        {
                            if (response.isSuccessful())
                                addOld(film);
                        } catch (SocketTimeoutException ignored)
                        {
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            threadCounter.decrementAndGet();
        }
    }
}
