/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import mSearch.Config;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.tool.FileSize;
import mSearch.tool.Hash;
import mSearch.tool.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emil
 */
public class AddToFilmlist {

    private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
    private final ListeFilme vonListe;
    private final ListeFilme listeEinsortieren;
    private final int numberOfCpuCores = 32;//(Runtime.getRuntime().availableProcessors() * Runtime.getRuntime().availableProcessors()) / 2;
    private AtomicInteger threadCounter = new AtomicInteger(0);

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

        vonListe.removeIf(f -> f.arr[DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE));
        listeEinsortieren.forEach(vonListe::add);
        performancePoint.collect();
    }

    private void performTitleSearch(HashSet<Hash> hash) {
        // nach "Thema-Titel" suchen
        EtmPoint performancePointThemaTitel = etmMonitor.createPoint("AddToFilmlist:performTitleSearch");
        vonListe.parallelStream().forEach(f -> {
            synchronized (hash) {
                hash.add(f.getHashValueIndexAddOld());
            }
        });

        listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueIndexAddOld()));
        hash.clear();
        performancePointThemaTitel.collect();
    }

    private void performUrlSearch(HashSet<Hash> hash) {
        // nach "URL" suchen
        EtmPoint performancePointUrlSuchen = etmMonitor.createPoint("AddToFilmlist:performUrlSearch");
        vonListe.parallelStream().forEach(f -> {
            synchronized (hash) {
                hash.add(f.getHashValueUrl());
            }
        });

        listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueUrl()));
        hash.clear();
        performancePointUrlSuchen.collect();
    }

    public int addOldList() {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        threadCounter = new AtomicInteger(0);
        int size = listeEinsortieren.size();
        final HashSet<Hash> hash = new HashSet<>(vonListe.size() + 1);

        performTitleSearch(hash);

        Log.sysLog("===== Liste einsortieren Hash =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");
        size = listeEinsortieren.size();

        performUrlSearch(hash);

        Log.sysLog("===== Liste einsortieren URL =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
        Log.sysLog("");

        int count = 0;
        size = listeEinsortieren.size();

        Log.sysLog("AddOld number of Threads: " + numberOfCpuCores);
        // Rest nehmen wir wenn noch online
        final ArrayList<AddOld> threadList = new ArrayList<>();
        for (int i = 0; i < numberOfCpuCores; ++i) {
            AddOld t = new AddOld(listeEinsortieren);
            t.setName("AddOld Thread-" + i);
            threadList.add(t);
            t.start();
        }

        int treffer = 0;
        while (!Config.getStop() && threadCounter.get() > 0) {
            try {
                if (Config.getStop()) {
                    for (AddOld t : threadList)
                        t.interrupt();
                }
                Log.sysLog("sek.: " + 2 * (count++) + "  Liste: " + listeEinsortieren.size());
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ex) {
                Log.errorLog(978451205, ex, "Fehler beim Import Old");
            }
        }

        //add all local thread entries to the filmlist
        try {
            treffer = 0;
            for (AddOld t : threadList) {
                final ArrayList<DatenFilm> localList = t.getLocalAddList();
                Log.sysLog("Thread " + t.getName() + " list size: " + localList.size());
                vonListe.addAll(localList);
                localList.clear();
                treffer += t.getTreffer();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer);
        Log.sysLog("");
        return treffer;
    }

    private class AddOld extends Thread {

        private static final int MIN_SIZE_ADD_OLD = 5; //REST eh nur Trailer
        private final ListeFilme listeOld;
        private final ArrayList<DatenFilm> localAddList = new ArrayList<>((vonListe.size() / numberOfCpuCores) + 500);
        private int treffer = 0;

        public int getTreffer() {
            return treffer;
        }

        public AddOld(ListeFilme listeOld) {
            this.listeOld = listeOld;
            threadCounter.incrementAndGet();
        }

        public ArrayList<DatenFilm> getLocalAddList() {
            return localAddList;
        }

        private boolean addOld(DatenFilm film) {
            treffer++;
            film.init();

            return localAddList.add(film);
        }

        private synchronized DatenFilm popOld(ListeFilme listeOld) {
            if (!listeOld.isEmpty()) {
                EtmPoint performancePoint = etmMonitor.createPoint("AddToFilmlist.popOld:remove");
                DatenFilm res = listeOld.remove(0);
                performancePoint.collect();
                return res;
            } else
                return null;
        }

        @Override
        public void run() {

            DatenFilm film;
            while (!isInterrupted() && (film = popOld(listeOld)) != null) {
                EtmPoint performancePoint = etmMonitor.createPoint("AddOld:run");
                if (FileSize.getFileSizeInMByteFromUrl(film.arr[DatenFilm.FILM_URL]) > MIN_SIZE_ADD_OLD) {
                    addOld(film);
                }
                performancePoint.collect();
            }
            threadCounter.decrementAndGet();
        }
    }
}
