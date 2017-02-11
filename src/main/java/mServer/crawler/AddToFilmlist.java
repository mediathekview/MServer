/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import mSearch.Config;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.tool.FileSize;
import mSearch.tool.Log;

/**
 *
 * @author emil
 */
public class AddToFilmlist {

    final int COUNTER_MAX = 25;
    private AtomicInteger executing = new AtomicInteger(0);
    private AtomicInteger treffer = new AtomicInteger(0);
    ListeFilme vonListe;
    ListeFilme listeEinsortieren;
    Collection<DatenFilm> filteredOnline = new ArrayList<>();

    public AddToFilmlist(ListeFilme vonListe, ListeFilme listeEinsortieren) {
        this.vonListe = vonListe;
        this.listeEinsortieren = listeEinsortieren;
    }

    public synchronized void addLiveStream() {
        // live-streams einfügen, es werde die vorhandenen ersetzt!

        if (listeEinsortieren.size() <= 0) {
            //dann wars wohl nix
            return;
        }

        Iterator<DatenFilm> it = vonListe.iterator();
        while (it.hasNext()) {
            DatenFilm f = it.next();
            if (f.arr[DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE)) {
                it.remove();
            }
        }
        listeEinsortieren.forEach(vonListe::add);
    }

    public synchronized int addOldList() {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        executing = new AtomicInteger(0);
        treffer = new AtomicInteger(0);

        mSearch.tool.Duration.staticPing("AddOld-1");

        int size = listeEinsortieren.size();
        HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);

        // ==============================================
        // nach "Thema-Titel" suchen
        vonListe.stream().forEach((f) -> hash.add(f.getIndexAddOld()));
        listeEinsortieren.removeIf((f) -> hash.contains(f.getIndexAddOld()));
        hash.clear();

        Log.sysLog("===== Liste einsortieren Hash =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");
        size = listeEinsortieren.size();

        // ==============================================
        // nach "URL" suchen
        vonListe.stream().forEach((f) -> hash.add(DatenFilm.getUrl(f)));
        listeEinsortieren.removeIf((f) -> hash.contains(DatenFilm.getUrl(f)));
        hash.clear();

        Log.sysLog("===== Liste einsortieren URL =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");
        size = listeEinsortieren.size();
        mSearch.tool.Duration.staticPing("AddOld-2");

        executing = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(COUNTER_MAX);
        try {
            listeEinsortieren.stream().forEach(film -> executorService.execute(new SorterCallable(film)));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        int count = 0;
        final int COUNT_MAX = 450; // 15 Minuten
        //final int COUNT_MAX = 5; // 15 Minuten
        while (!Config.getStop() && executing.get() > 0) {
            try {
                System.out.println("s: " + 2 * (count++) + "  Liste: " + listeEinsortieren.size() + "  Treffer: " + treffer.get() + "   Threads: " + executing.toString());
                if (count > COUNT_MAX) {
                    // dann haben wir mehr als 15 Minuten und: Stop
                    Log.sysLog("===== Liste einsortieren: ABBRUCH =====");
                    Log.sysLog("COUNT_MAX erreicht [s]: " + COUNT_MAX * 2);
                    Log.sysLog("");
                    break;
                }
                wait(2000);
            } catch (Exception ex) {
                Log.errorLog(978451205, ex, "Fehler beim Import Old");
            }
        }

        executorService.shutdownNow();
        vonListe.addAll(filteredOnline);

        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer.get()));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer.get());
        Log.sysLog("");
        return treffer.get();
    }

    class SorterCallable implements Runnable {

        private DatenFilm film;
        private final int MIN_SIZE_ADD_OLD = 5; //REST eh nur Trailer

        SorterCallable(DatenFilm film) {
            this.film = film;
            executing.incrementAndGet();
        }

        @Override
        public void run() {
            long size = FileSize.laengeLong(film.arr[DatenFilm.FILM_URL]);
            if (size > MIN_SIZE_ADD_OLD) {
                addOld(film);
            }
            executing.decrementAndGet();
        }
    }

    private synchronized boolean addOld(DatenFilm film) {
        treffer.getAndIncrement();
        film.init();
        return filteredOnline.add(film);
    }
}
