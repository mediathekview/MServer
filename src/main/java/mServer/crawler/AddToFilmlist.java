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

    final int COUNTER_MAX = 25;
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

        Log.sysLog("===== Liste einsortieren Thema-Titel =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + listeEinsortieren.size());
        Log.sysLog("Liste einsortieren, entfernt: " + (listeEinsortieren.size() - filteredTopicTitle.size()));
        Log.sysLog("");

        // ==============================================
        // nach "URL" suchen
        Collection<String> filmUrls = vonListe.parallelStream()
                .map(DatenFilm::getUrl)
                .collect(Collectors.toList());

        int size = filteredTopicTitle.size();
        ListeFilme filteredUrl = new ListeFilme();

        filteredUrl.addAll(filteredTopicTitle.parallelStream()
                .filter(film -> !filmUrls.contains(DatenFilm.getUrl(film)))
                .collect(Collectors.toList()));

        Log.sysLog("===== Liste einsortieren URL =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - filteredUrl.size()));
        Log.sysLog("");

        int count = 0;
        final int COUNT_MAX = 450; // 15 Minuten
        stopOld = false;
        size = filteredUrl.size();

        // Rest nehmen wir wenn noch online
        for (int i = 0; i < COUNTER_MAX; ++i) {
            new Thread(new AddOld(filteredUrl)).start();
        }
        while (!Config.getStop() && counter > 0) {
            try {
                System.out.println("s: " + 2 * (count++) + "  Liste: " + filteredUrl.size() + "  Treffer: " + treffer.get() + "   Threads: " + counter);
                if (count > COUNT_MAX) {
                    // dann haben wir mehr als 10 Minuten und: Stop
                    Log.sysLog("===== Liste einsortieren: ABBRUCH =====");
                    Log.sysLog("COUNT_MAX erreicht [s]: " + COUNT_MAX * 2);
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
            ++counter;
        }

        @Override
        public void run() {
            while (!stopOld && (film = popOld(listeOld)) != null) {
                long size = FileSize.laengeLong(film.arr[DatenFilm.FILM_URL]);
                if (size > MIN_SIZE_ADD_OLD) {
                    addOld(film);
                }
            }
            --counter;
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
