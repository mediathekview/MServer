/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import java.util.HashSet;
import java.util.Iterator;
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

    final int COUNTER_MAX = 20;
    int counter = 0;
    int treffer = 0;
    ListeFilme vonListe;
    ListeFilme listeEinsortieren;

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
        treffer = 0;
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

        // Rest nehmen wir wenn noch online
        for (int i = 0; i < COUNTER_MAX; ++i) {
            new Thread(new AddOld(listeEinsortieren)).start();
        }
        int count = 0;
        final int COUNT_MAX = 300; // 10 Minuten
        stopOld = false;
        while (!Config.getStop() && counter > 0) {
            try {
                System.out.println("s: " + 2 * (count++) + "  Liste: " + listeEinsortieren.size() + "  Treffer: " + treffer + "   Threads: " + counter);
                if (count > COUNT_MAX) {
                    // dann haben wir mehr als 10 Minuten und: Stop
                    Log.sysLog("===== Liste einsortieren: ABBRUCH =====");
                    Log.sysLog("COUNT_MAX erreicht [s]: " + COUNT_MAX * 2);
                    Log.sysLog("");
                    stopOld = true;
                }
                wait(2000);
            } catch (InterruptedException ignored) {
            }
        }
        Log.sysLog("===== Liste einsortieren: Noch online =====");
        Log.sysLog("Liste einsortieren, Anzahl: " + size);
        Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer));
        Log.sysLog("");
        Log.sysLog("In Liste einsortiert: " + treffer);
        Log.sysLog("");
        return treffer;
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
        ++treffer;
        film.init();
        return vonListe.add(film);
    }

}
