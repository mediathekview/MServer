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
import mSearch.tool.Hash;
import mSearch.tool.Log;
import mSearch.tool.MVHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emil
 */
public class AddToFilmlist {

    private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();
    private static final int MIN_SIZE_ADD_OLD = 5; //REST eh nur Trailer
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
//        EtmPoint performancePointThemaTitel = etmMonitor.createPoint("AddToFilmlist:performTitleSearch");
        vonListe.parallelStream().forEach(f -> {
            synchronized (hash) {
                hash.add(f.getHashValueIndexAddOld());
            }
        });

        listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueIndexAddOld()));
        hash.clear();
//        performancePointThemaTitel.collect();
    }

    private void performUrlSearch(HashSet<Hash> hash) {
        // nach "URL" suchen
//        EtmPoint performancePointUrlSuchen = etmMonitor.createPoint("AddToFilmlist:performUrlSearch");
        vonListe.parallelStream().forEach(f -> {
            synchronized (hash) {
                hash.add(f.getHashValueUrl());
            }
        });

        listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueUrl()));
        hash.clear();
//        performancePointUrlSuchen.collect();
    }

    public int addOldList() {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        threadCounter = new AtomicInteger(0);

        listeEinsortieren.removeIf(f -> !f.arr[DatenFilm.FILM_URL].toLowerCase().startsWith("http"));
        listeEinsortieren.removeIf(f -> {
            String groesse = f.arr[DatenFilm.FILM_GROESSE];
            if (groesse.isEmpty())
                return false;
            else {
                return Long.parseLong(groesse) < MIN_SIZE_ADD_OLD;
            }

        });
        int size = listeEinsortieren.size();

        final HashSet<Hash> hash = new HashSet<>(vonListe.size() + 1);

        performTitleSearch(hash);

        Log.sysLog("===== Liste einsortieren Title =====");
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

        size = listeEinsortieren.size();
        long oldSize = size;

        final ArrayList<AddOld> threadList = new ArrayList<>();
        final OkHttpClient copyClient = MVHttpClient.getInstance().getHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS).build();
        for (int i = 0; i < numberOfCpuCores; ++i) {
            AddOld t = new AddOld(listeEinsortieren, copyClient);
            t.setName("AddOld Thread-" + i);
            threadList.add(t);
            t.start();
        }

        int treffer = 0;
        int count = 0;
        while (!Config.getStop() && threadCounter.get() > 0) {
            try {
                count++;
                if (count % 5 == 0) {
                    long curSize = listeEinsortieren.size();
                    System.out.println("Liste: " + curSize);
                    System.out.println("Entfernte Einträge: " + ((oldSize - curSize)));
                    oldSize = curSize;
                }
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ex) {
                Log.errorLog(978451205, ex, "Fehler beim Import Old");
            }
        }

        try {
            if (Config.getStop()) {
                for (AddOld t : threadList)
                    t.interrupt();
                for (AddOld t : threadList)
                    t.join();
            }

            treffer = 0;
            //add all local thread entries to the filmlist
            for (AddOld t : threadList) {
                final ArrayList<DatenFilm> localList = t.getLocalAddList();
                //Log.sysLog("Thread " + t.getName() + " list size: " + localList.size());
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

        private final ListeFilme listeOld;
        private final ArrayList<DatenFilm> localAddList = new ArrayList<>((vonListe.size() / numberOfCpuCores) + 500);
        private int treffer = 0;
        private OkHttpClient client = null;

        public AddOld(ListeFilme listeOld, OkHttpClient client) {
            this.listeOld = listeOld;
            threadCounter.incrementAndGet();
            this.client = client;
        }

        public int getTreffer() {
            return treffer;
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
//                EtmPoint performancePoint = etmMonitor.createPoint("AddToFilmlist.popOld:remove");
                DatenFilm res = listeOld.remove(0);
//                performancePoint.collect();
                return res;
            } else
                return null;
        }

        @Override
        public void run() {

            DatenFilm film;
            while (!isInterrupted() && (film = popOld(listeOld)) != null) {
//                EtmPoint performancePoint = etmMonitor.createPoint("AddOld:run");

                final String url = film.arr[DatenFilm.FILM_URL];
                if (film.arr[DatenFilm.FILM_GROESSE].isEmpty()) {
                    Request request = new Request.Builder().url(url).head().build();
                    try (Response response = client.newCall(request).execute();
                         ResponseBody body = response.body()) {
                        if (response.isSuccessful()) {
                            long respLength = body.contentLength();
                            if (respLength < 1_000_000) {
                                respLength = -1;
                            } else if (respLength > 1_000_000) {
                                respLength /= 1_000_000;
                            }

                            if (respLength > MIN_SIZE_ADD_OLD)
                                addOld(film);
                        }
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    if (Long.parseLong(film.arr[DatenFilm.FILM_GROESSE]) > MIN_SIZE_ADD_OLD) {
                        Request request = new Request.Builder().url(url).head().build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful())
                                addOld(film);
                        } catch (SocketTimeoutException ignored) {
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
//                performancePoint.collect();
            }
            threadCounter.decrementAndGet();
        }
    }
}
