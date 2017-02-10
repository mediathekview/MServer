/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

    private static final int MIN_SIZE_ADD_OLD = 5;

    final int COUNTER_MAX = 20;
    int counter = 0;
    AtomicInteger treffer = new AtomicInteger(0);
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
        counter = 0;
        treffer = new AtomicInteger(0);

        // ==============================================
        // nach "Thema-Titel" suchen
        Collection<DatenFilm> filteredTopicTitle = new CopyOnWriteArrayList<>();
        filteredTopicTitle.addAll(listeEinsortieren.parallelStream()
                .filter(film -> (null == vonListe.istInFilmListe(film.arr[DatenFilm.FILM_SENDER], film.arr[DatenFilm.FILM_THEMA], film.arr[DatenFilm.FILM_TITEL])))
                .collect(Collectors.toList()));

        Log.sysLog("===== Liste einsortieren Hash =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + listeEinsortieren.size());
        Log.sysLog("Liste einsortieren, entfernt: " + (listeEinsortieren.size() - filteredTopicTitle.size()));
        Log.sysLog("");

        // ==============================================
        // nach "URL" suchen
        Collection<DatenFilm> filteredUrl = new CopyOnWriteArrayList<>();

        Collection<String> filmUrls = vonListe.parallelStream()
                .map(DatenFilm::getUrl)
                .collect(Collectors.toList());

        int size = filteredTopicTitle.size();

        filteredUrl.addAll(filteredTopicTitle.parallelStream()
                .filter(film -> !filmUrls.contains(DatenFilm.getUrl(film)))
                .collect(Collectors.toList()));

        Log.sysLog("===== Liste einsortieren URL =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - filteredUrl.size()));
        Log.sysLog("");

        // Rest nehmen wir wenn noch online
        // Prüfung auf online erst am Ende durchführen, damit jeder Film nur einmalig geprüft wird
        laufen = true;
        new Thread(new Ti()).start();

        Collection<SizeAction> actions = new ArrayList<>();
        filteredUrl.forEach((video) -> {
            SizeAction newTask = new SizeAction(video);
            actions.add(newTask);
        });

        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            actions.forEach(e -> executor.execute(e));
        } catch (Exception ex) {
            Log.sysLog("ExecutorService");
        }

        vonListe.addAll(filteredOnline);
        laufen = false;
        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + filteredOnline.size());
        Log.sysLog("Liste einsortieren, entfernt: " + (filteredOnline.size() - treffer.get()));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer.get());
        Log.sysLog("");
        return treffer.get();
    }

    private void initFilm(DatenFilm film) {
        treffer.getAndIncrement();
        film.init();
    }

    private class SizeAction implements Runnable {

        DatenFilm f;

        public SizeAction(DatenFilm f) {
            this.f = f;
        }

        public void run() {
            if (!Config.getStop()) {
                addCount();
                if (FileSize.laengeLong(f.arr[DatenFilm.FILM_URL]) > MIN_SIZE_ADD_OLD) {
                    initFilm(f);
                    filteredOnline.add(f);
                }
            }
        }
    }

    public synchronized int addOldList_() {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        counter = 0;
        treffer = new AtomicInteger(0);

        // ==============================================
        // nach "Thema-Titel" suchen
        Collection<DatenFilm> filteredTopicTitle = new CopyOnWriteArrayList<>();
        filteredTopicTitle.addAll(listeEinsortieren.parallelStream()
                .filter(film -> (null == vonListe.istInFilmListe(film.arr[DatenFilm.FILM_SENDER], film.arr[DatenFilm.FILM_THEMA], film.arr[DatenFilm.FILM_TITEL])))
                .collect(Collectors.toList()));

        Log.sysLog("===== Liste einsortieren Hash =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + listeEinsortieren.size());
        Log.sysLog("Liste einsortieren, entfernt: " + (listeEinsortieren.size() - filteredTopicTitle.size()));
        Log.sysLog("");

        // ==============================================
        // nach "URL" suchen
        Collection<DatenFilm> filteredUrl = new CopyOnWriteArrayList<>();

        Collection<String> filmUrls = vonListe.parallelStream()
                .map(DatenFilm::getUrl)
                .collect(Collectors.toList());

        int size = filteredTopicTitle.size();

        filteredUrl.addAll(filteredTopicTitle.parallelStream()
                .filter(film -> !filmUrls.contains(DatenFilm.getUrl(film)))
                .collect(Collectors.toList()));

        Log.sysLog("===== Liste einsortieren URL =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - filteredUrl.size()));
        Log.sysLog("");

        count = 0;
        laufen = true;
        new Thread(new Ti()).start();

        // Rest nehmen wir wenn noch online
        // Prüfung auf online erst am Ende durchführen, damit jeder Film nur einmalig geprüft wird
        Collection<DatenFilm> filteredOnline = new CopyOnWriteArrayList<>();
        filteredOnline.addAll(filteredUrl.parallelStream().filter(f
                -> addCount() && !Config.getStop() && FileSize.laengeLong(f.arr[DatenFilm.FILM_URL]) > MIN_SIZE_ADD_OLD
        )
                .collect(Collectors.toList()));
        filteredOnline.parallelStream().forEach(f -> {
            if (!Config.getStop()) {
                initFilm(f);
            }
        });
        vonListe.addAll(filteredOnline);
        laufen = false;

        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + filteredOnline.size());
        Log.sysLog("Liste einsortieren, entfernt: " + (filteredOnline.size() - treffer.get()));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer.get());
        Log.sysLog("");
        return treffer.get();
    }

    static int count = 0;
    static boolean laufen = true;

    private synchronized boolean addCount() {
        ++count;
        return true;
    }

    class Ti implements Runnable {

        // nur zum Testen
        int t = 0;

        @Override
        public synchronized void run() {
            while (laufen) {
                ++t;
                System.out.println(t + " Count : " + count);
                try {
                    wait(1000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
