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
import java.util.concurrent.atomic.AtomicInteger;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import mSearch.Config;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.tool.Duration;
import mSearch.tool.FileSize;
import mSearch.tool.Log;

/**
 *
 * @author emil
 */
public class AddToFilmlist {
    private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
    AtomicInteger threadCounter = new AtomicInteger(0);
    AtomicInteger treffer = new AtomicInteger(0);
    ListeFilme vonListe;
    ListeFilme listeEinsortieren;
    Collection<DatenFilm> filteredOnline = new ArrayList<>();

    public AddToFilmlist(ListeFilme vonListe, ListeFilme listeEinsortieren) {
        this.vonListe = vonListe;
        this.listeEinsortieren = listeEinsortieren;
    }

    public synchronized void addLiveStream() {
        EtmPoint performancePoint = etmMonitor.createPoint("AddToFilmlist:addLiveStream");
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
        performancePoint.collect();
    }

    public synchronized int addOldList() {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        threadCounter = new AtomicInteger(0);
        treffer = new AtomicInteger(0);
        int size = listeEinsortieren.size();
        HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);

        Duration.staticPing("AddOld-1");

        // ==============================================
        // nach "Thema-Titel" suchen
        EtmPoint performancePointThemaTitel = etmMonitor.createPoint("AddToFilmlist:addOldList#themaTitel");
        vonListe.stream().forEach((f) -> hash.add(f.getIndexAddOld()));
        listeEinsortieren.removeIf((f) -> hash.contains(f.getIndexAddOld()));
        hash.clear();
        performancePointThemaTitel.collect();

        Log.sysLog("===== Liste einsortieren Hash =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");
        size = listeEinsortieren.size();

        // ==============================================
        // nach "URL" suchen
        EtmPoint performancePointUrlSuchen = etmMonitor.createPoint("AddToFilmlist:addOldList#UrlSuchen");
        vonListe.stream().forEach((f) -> hash.add(DatenFilm.getUrl(f)));
        listeEinsortieren.removeIf((f) -> hash.contains(DatenFilm.getUrl(f)));
        hash.clear();
        performancePointUrlSuchen.collect();

        Log.sysLog("===== Liste einsortieren URL =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");

        Duration.staticPing("AddOld-2");

        int count = 0;
        final int MAX_THREAD = 30;
        final int MAX_WAIT_TIME = 450; // 450*2=900s -> 15 Minuten
        //final int MAX_WAIT_TIME = 10; // 10*2=20s
        stopOld = false;
        size = listeEinsortieren.size();

        // Rest nehmen wir wenn noch online
        for (int i = 0; i < MAX_THREAD; ++i) {
            new Thread(new AddOld(listeEinsortieren)).start();
        }

        while (!Config.getStop() && threadCounter.get() > 0) {
            try {
                System.out.println("sek.: " + 2 * (count++) + "  Liste: " + listeEinsortieren.size() + "  Treffer: " + treffer.get() + "   Threads: " + threadCounter);
                if (count > MAX_WAIT_TIME) {
                    // dann haben wir mehr als 10 Minuten und: Stop
                    Log.sysLog("===== Liste einsortieren: ABBRUCH =====");
                    Log.sysLog("COUNT_MAX erreicht [s]: " + MAX_WAIT_TIME * 2);
                    Log.sysLog("");
                    stopOld = true;
                }
                wait(2000);
            } catch (Exception ex) {
                Log.errorLog(978451205, ex, "Fehler beim Import Old");
            }
        }

        vonListe.addAll(filteredOnline);

        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer.get()));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer.get());
        Log.sysLog("");
        return treffer.get();
    }

    private boolean stopOld = false;

    private class AddOld implements Runnable {

        private DatenFilm film;
        private final ListeFilme listeOld;
        private final int MIN_SIZE_ADD_OLD = 5; //REST eh nur Trailer

        public AddOld(ListeFilme listeOld) {
            this.listeOld = listeOld;
            threadCounter.incrementAndGet();
        }

        @Override
        public void run() {
            EtmPoint performancePoint = etmMonitor.createPoint("AddOld:run");
            while (!stopOld && (film = popOld(listeOld)) != null) {
                long size = FileSize.laengeLong(film.arr[DatenFilm.FILM_URL]);
                if (size > MIN_SIZE_ADD_OLD) {
                    addOld(film);
                }
            }
            threadCounter.decrementAndGet();
            performancePoint.collect();
        }
    }

    private synchronized DatenFilm popOld(ListeFilme listeOld) {
        if (listeOld.size() > 0) {
            return listeOld.remove(0);
        }
        return null;
    }

    private synchronized boolean addOld(DatenFilm film) {
        treffer.getAndIncrement();
        film.init();
        return filteredOnline.add(film);
    }
}
