/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Hash;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;

import java.util.*;

import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.orf.OrfVideoInfoDTO;
import mServer.tool.MserverDaten;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

public class AddToFilmlist {

  /**
   * Minimum size of films in MiB to be included in new list.
   */
  private static final int MIN_SIZE_ADD_OLD = 5;
  private final static int NUMBER_OF_THREADS = 32;//(Runtime.getRuntime().availableProcessors() * Runtime.getRuntime().availableProcessors()) / 2;
  private final ListeFilme vonListe;
  private final ListeFilme listeEinsortieren;
  private final BannedFilmFilter bannedFilmFilter;
  /**
   * List of all locally started import threads.
   */
  private final ArrayList<ImportOldFilmlistThread> threadList = new ArrayList<>();
  private AtomicInteger threadCounter = new AtomicInteger(0);

  public AddToFilmlist(ListeFilme vonListe, ListeFilme listeEinsortieren) {
    this.vonListe = vonListe;
    this.listeEinsortieren = listeEinsortieren;
    this.bannedFilmFilter = new BannedFilmFilter();
  }

  public synchronized void addLiveStream() {
    if (listeEinsortieren.size() <= 0) {
      return;
    }

    vonListe.removeIf(f -> f.arr[DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE));
    listeEinsortieren.forEach(vonListe::add);
  }

  private void performTitleSearch(HashSet<Hash> hash, final int size) {
    vonListe.parallelStream().forEach(f -> {
      synchronized (hash) {
        hash.add(f.getHashValueIndexAddOld());
      }
    });

    listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueIndexAddOld()));
    hash.clear();

    Log.sysLog("===== Liste einsortieren Title =====");
    Log.sysLog("Liste einsortieren, Anzahl: " + size);
    Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
    Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
    Log.sysLog("");
  }

  private void performUrlSearch(HashSet<Hash> hash, final int size) {
    vonListe.parallelStream().forEach(f -> {
      synchronized (hash) {
        hash.add(f.getHashValueUrl());
      }
    });

    listeEinsortieren.removeIf((f) -> hash.contains(f.getHashValueUrl()));
    hash.clear();

    Log.sysLog("===== Liste einsortieren URL =====");
    Log.sysLog("Liste einsortieren, Anzahl: " + size);
    Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
    Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
    Log.sysLog("");
  }

  /**
   * Remove links which don´t start with http. - * Remove old film entries which are smaller than
   * MIN_SIZE_ADD_OLD.
   * remove time from mdr aktuell and orf topics
   * replace topic and title of audio description entries for orf+srf
   */
  private void performInitialCleanup() {
    listeEinsortieren.removeIf(f -> !f.arr[DatenFilm.FILM_URL].toLowerCase().startsWith("http"));
    listeEinsortieren.removeIf(f -> f.arr[DatenFilm.FILM_SENDER].equals(Const.ORF) && f.arr[DatenFilm.FILM_URL]
        .matches(OrfVideoInfoDTO.FILTER_JUGENDSCHUTZ));
    listeEinsortieren.removeIf(f -> {
      String groesse = f.arr[DatenFilm.FILM_GROESSE];
      if (groesse.isEmpty()) {
        return false;
      } else {
        return Long.parseLong(groesse) < MIN_SIZE_ADD_OLD;
      }

    });
    removeTimeFromMdrAktuell(listeEinsortieren);
    removeTimeFromOrf(listeEinsortieren);
    updateAudioDescriptionOrf(listeEinsortieren);
    updateAudioDescriptionSrf(listeEinsortieren);
    updateArdWebsite(listeEinsortieren);
    updateFunkMissingHost(listeEinsortieren);
  }

  private void updateFunkMissingHost(ListeFilme listeEinsortieren) {
    final List<DatenFilm> list = listeEinsortieren.parallelStream()
            .filter(film -> film.arr[DatenFilm.FILM_SENDER].equals("Funk.net") && film.arr[DatenFilm.FILM_URL].matches("https:\\/\\/[0-9]*\\/.*"))
            .collect(Collectors.toList());
    Log.sysLog("FUNK: add missing host für " + list.size() + " Einträge.");

    list.forEach(film -> film.arr[DatenFilm.FILM_URL] = film.arr[DatenFilm.FILM_URL].replace("https://", "https://funk-02.akamaized.net/").trim());
    list.forEach(film -> film.arr[DatenFilm.FILM_URL_KLEIN] = film.arr[DatenFilm.FILM_URL_KLEIN].replace("https://", "https://funk-02.akamaized.net/").trim());
    list.forEach(film -> film.arr[DatenFilm.FILM_URL_HD] = film.arr[DatenFilm.FILM_URL_HD].replace("https://", "https://funk-02.akamaized.net/").trim());
  }

  private void updateArdWebsite(ListeFilme listeEinsortieren) {
    final List<DatenFilm> list = listeEinsortieren.parallelStream()
            .filter(film -> film.arr[DatenFilm.FILM_SENDER].equals(Const.ARD) && !film.arr[DatenFilm.FILM_WEBSEITE].startsWith("https://www.ardmediathek.de/video/"))
            .collect(Collectors.toList());
    Log.sysLog("ARD: update webseite für " + list.size() + " Einträge.");

    list.forEach(film -> film.arr[DatenFilm.FILM_WEBSEITE] = film.arr[DatenFilm.FILM_WEBSEITE].replace("/ard/player/", "/video/").trim());
  }


  private void updateAudioDescriptionOrf(ListeFilme listeEinsortieren) {
    final List<DatenFilm> list = listeEinsortieren.parallelStream()
            .filter(
                    film -> film.arr[DatenFilm.FILM_SENDER].equals(Const.ORF) && film.arr[DatenFilm.FILM_THEMA]
                            .startsWith("AD |"))
            .collect(Collectors.toList());
    Log.sysLog("ORF: update Thema/Titel für " + list.size() + " Einträge mit Audiodeskription.");
    if (!list.isEmpty()) {
      list.forEach(film -> {
        film.arr[DatenFilm.FILM_THEMA] = film.arr[DatenFilm.FILM_THEMA].replace("AD |", "").trim();
        film.arr[DatenFilm.FILM_TITEL] = film.arr[DatenFilm.FILM_TITEL].replace("AD |", "").trim() + " (Audiodeskription)";
      });
    }
  }

  private void updateAudioDescriptionSrf(ListeFilme listeEinsortieren) {
    final List<DatenFilm> list = listeEinsortieren.parallelStream()
            .filter(
                    film -> film.arr[DatenFilm.FILM_SENDER].equals(Const.SRF) && film.arr[DatenFilm.FILM_THEMA]
                            .contains("mit Audiodeskription"))
            .collect(Collectors.toList());
    Log.sysLog("SRF: update Thema/Titel für " + list.size() + " Einträge mit Audiodeskription.");
    if (!list.isEmpty()) {
      list.forEach(film -> {
        film.arr[DatenFilm.FILM_THEMA] = film.arr[DatenFilm.FILM_THEMA].replace("mit Audiodeskription", "").trim();
        film.arr[DatenFilm.FILM_TITEL] = film.arr[DatenFilm.FILM_TITEL].replace(" mit Audiodeskription", "").trim() + " (Audiodeskription)";
      });
    }
  }

  private void removeTimeFromOrf(ListeFilme listeEinsortieren) {
    final List<DatenFilm> list = listeEinsortieren.parallelStream()
        .filter(
            film -> film.arr[DatenFilm.FILM_SENDER].equals(Const.ORF) && film.arr[DatenFilm.FILM_THEMA]
                .matches(".*[0-9]{1,2}:[0-9][0-9]$"))
        .collect(Collectors.toList());
    Log.sysLog("ORF: update Thema für " + list.size() + " Einträge.");
    if (!list.isEmpty()) {
      list.forEach(film -> film.arr[DatenFilm.FILM_THEMA] = film.arr[DatenFilm.FILM_THEMA].replaceAll("[0-9]{1,2}:[0-9][0-9]$", "").trim());
    }
  }

  private void removeTimeFromMdrAktuell(ListeFilme listeEinsortieren) {
    final String topic = "MDR aktuell";
    final List<DatenFilm> list = listeEinsortieren.parallelStream()
        .filter(film -> film.arr[DatenFilm.FILM_THEMA].startsWith(topic))
        .collect(Collectors.toList());
    Log.sysLog("MDR aktuell: update Thema für " + list.size() + " Einträge.");
    if (!list.isEmpty()) {
      list.forEach(film -> film.arr[DatenFilm.FILM_THEMA] = topic);
    }
  }

  private void startThreads() {
    final OkHttpClient client = MVHttpClient.getInstance().getReducedTimeOutClient();

    Queue<DatenFilm> syncList = new LinkedBlockingQueue<>(listeEinsortieren);
    for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
      ImportOldFilmlistThread t = new ImportOldFilmlistThread(syncList, client);
      t.setName("ImportOldFilmlistThread Thread-" + i);
      threadList.add(t);
      t.start();
    }
  }

  private void stopThreads() {
    if (Config.getStop()) {
      for (ImportOldFilmlistThread t : threadList) {
        t.interrupt();
      }
      for (ImportOldFilmlistThread t : threadList) {
        try {
          t.join();
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  /*
   * Diese Methode sortiert eine vorhandene Liste in eine andere Filmliste ein,
   * dabei werden nur nicht vorhandene Filme einsortiert.
   */
  public int addOldList() {
    threadCounter = new AtomicInteger(0);
    final HashSet<Hash> hash = new HashSet<>(vonListe.size() + 1);

    performInitialCleanup();

    int size = listeEinsortieren.size();

    performTitleSearch(hash, size);

    size = listeEinsortieren.size();
    performUrlSearch(hash, size);

    size = listeEinsortieren.size();
    long oldSize = size;

    startThreads();

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
  private int retrieveThreadResults() {
    int treffer = 0;
    for (ImportOldFilmlistThread t : threadList) {
      final ArrayList<DatenFilm> localList = t.getLocalAddList();
      if (MserverDaten.debug) {
        Log.sysLog("Thread " + t.getName() + " list size: " + localList.size());
      }
      vonListe.addAll(localList);
      localList.clear();
      treffer += t.getTreffer();
    }
    return treffer;
  }

  private class ImportOldFilmlistThread extends Thread {

    private final Queue<DatenFilm> listeOld;
    private final ArrayList<DatenFilm> localAddList = new ArrayList<>(
        (vonListe.size() / NUMBER_OF_THREADS) + 500);
    private int treffer = 0;
    private OkHttpClient client = null;

    public ImportOldFilmlistThread(Queue<DatenFilm> listeOld, OkHttpClient client) {
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

    private void addOld(DatenFilm film) {
      if (bannedFilmFilter.isBanned(film)) {
        Log.sysLog("Blacklist Treffer im import Old (" + film.arr[DatenFilm.FILM_TITEL] + ")");
        return;
      }

      treffer++;
      film.init();

      localAddList.add(film);
    }

    @Override
    public void run() {

      DatenFilm film;
      while (!isInterrupted() && (film = listeOld.poll()) != null) {
        final String url = film.arr[DatenFilm.FILM_URL];
        if (film.arr[DatenFilm.FILM_GROESSE].isEmpty()) {
          Request request = createOnlineCheckRequest(url);
          try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
              long respLength = determineContentLength(response);

              if (isRelevantContentType(response) && !removedVideo(film, response) &&
                  // ignore file length of m3u8-files because it is always too small
                  (isM3u8File(url) || respLength > MIN_SIZE_ADD_OLD)) {
                addOld(film);
              } else {
                Log.sysLog("film removed: code: " + response.code() + ": " + url);
              }
            } else {
              Log.sysLog("film removed: code: " + response.code() + ": " + url);
            }
          } catch (Exception ex) {
            Log.errorLog(12834738, ex, "exception online check film: " + url);
            // add film to list, because online check failed
            addOld(film);
          }
        } else {
          if (Long.parseLong(film.arr[DatenFilm.FILM_GROESSE]) > MIN_SIZE_ADD_OLD) {
            Request request = createOnlineCheckRequest(url);
            try (Response response = client.newCall(request).execute()) {
              if (response.isSuccessful() && isRelevantContentType(response) && !removedVideo(film, response)) {
                addOld(film);
              } else {
                Log.sysLog("film removed: code: " + response.code() + ": " + url);
              }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          }
        }
      }
      threadCounter.decrementAndGet();
    }

    private boolean removedVideo(DatenFilm film, Response response) {
      return orfRemovedVideo(film, response) || arteRemovedVideo(film, response);
    }

    private boolean arteRemovedVideo(DatenFilm film, Response response) {
      if (film.arr[DatenFilm.FILM_SENDER].equals(Const.ARTE_DE)) {
        String path = response.request().url().encodedPath();
        return path.contains("_EXTRAIT_");
      }

      return false;
    }

    private boolean orfRemovedVideo(DatenFilm film, Response response) {
      if (film.arr[DatenFilm.FILM_SENDER].equals(Const.ORF)) {
        String path = response.request().url().encodedPath();
        return path.contains("/bearbeitung_") || path.contains("/geoprotection");
      }

      return false;
    }

    @NotNull
    private Request createOnlineCheckRequest(String url) {
      Builder builder = new Builder().url(url);
      if (isM3u8File(url)) {
        // head request of m3u8 files always returns 405 => use get instead
        return builder.get().build();
      }

      return builder.head().build();
    }

    private boolean isM3u8File(String url) {
      final Optional<String> fileType = UrlUtils.getFileType(url);
      if (fileType.isPresent() && fileType.get().equalsIgnoreCase("m3u8")) {
        return true;
      }

      return false;
    }

    private boolean isRelevantContentType(Response response) {
      final String contentType = response.header(CONTENT_TYPE, "");

      // html reponses indicate a redirect
      // this is used for offline films
      return !contentType.contains("text/html");
    }

    private long determineContentLength(Response response) {
      final String contentLength = response.header("Content-Length", "-1");
      long respLength = Long.parseLong(contentLength);
      if (respLength < 1_000_000) {
        respLength = -1;
      } else if (respLength > 1_000_000) {
        respLength /= 1_000_000;
      }
      return respLength;
    }
  }
}
