/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package mServer.crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.tool.MserverDaten;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddToFilmlist {
  private class ImportOldFilmlistThread extends Thread {

    private final List<Film> listeOld;
    private final ArrayList<Film> localAddList =
        new ArrayList<>(vonListe.size() / NUMBER_OF_THREADS + 500);
    private int treffer = 0;
    private OkHttpClient client = null;

    public ImportOldFilmlistThread(final List<Film> listeOld, final OkHttpClient client) {
      this.listeOld = listeOld;
      threadCounter.incrementAndGet();
      this.client = client;
    }

    public ArrayList<Film> getLocalAddList() {
      return localAddList;
    }

    public int getTreffer() {
      return treffer;
    }

    @Override
    public void run() {

      Film film;
      while (!isInterrupted() && (film = popOld(listeOld)) != null) {
        final String url = film.getUrl(Resolution.NORMAL).toString();
        if (film.getFileSize(Resolution.NORMAL) == null) {
          // long fileSize = CrawlerTool.getFileSize(film.getUrl(Resolution.NORMAL));

          // if (fileSize > MIN_SIZE_ADD_OLD) {
          // addOld(film);
          // }
        } else {
          if (film.getFileSize(Resolution.NORMAL) != null
              && film.getFileSize(Resolution.NORMAL) > MIN_SIZE_ADD_OLD) {
            final Request request = new Request.Builder().url(url).head().build();
            try (Response response = client.newCall(request).execute()) {
              if (response.isSuccessful()) {
                addOld(film);
              }
            } catch (final SocketTimeoutException ignored) {
            } catch (final IOException ex) {
              ex.printStackTrace();
            }
          }
        }
      }
      threadCounter.decrementAndGet();
    }

    private void addOld(final Film film) {
      treffer++;

      localAddList.add(film);
    }

    private synchronized Film popOld(final List<Film> listeOld) {
      if (!listeOld.isEmpty()) {
        return listeOld.remove(0);
      } else {
        return null;
      }
    }
  }

  /**
   * Minimum size of films in MiB to be included in new list.
   */
  private static final String THEMA_LIVE = "Livestream";
  private static final int MIN_SIZE_ADD_OLD = 5;
  private final static int NUMBER_OF_THREADS = 32;// (Runtime.getRuntime().availableProcessors() *
                                                  // Runtime.getRuntime().availableProcessors()) /
                                                  // 2;
  private final ListeFilme vonListe;
  private final ListeFilme listeEinsortieren;
  /**
   * List of all locally started import threads.
   */
  private final ArrayList<ImportOldFilmlistThread> threadList = new ArrayList<>();


  private AtomicInteger threadCounter = new AtomicInteger(0);

  public AddToFilmlist(final ListeFilme vonListe, final ListeFilme listeEinsortieren) {
    this.vonListe = vonListe;
    this.listeEinsortieren = listeEinsortieren;
  }

  public synchronized void addLiveStream() {
    if (listeEinsortieren.size() > 0) {
      vonListe.removeIf(f -> f.getThema().equals(THEMA_LIVE));
      vonListe.addAll(listeEinsortieren);
    }
  }


  /*
   * Diese Methode sortiert eine vorhandene Liste in eine andere Filmliste ein, dabei werden nur
   * nicht vorhandene Filme einsortiert.
   */
  public int addOldList() {
    threadCounter = new AtomicInteger(0);
    performInitialCleanup();

    int size = listeEinsortieren.size();

    removeExisting(size);

    size = listeEinsortieren.size();
    long oldSize = size;

    startThreads();

    int count = 0;
    while (!Config.getStop() && threadCounter.get() > 0) {
      try {
        count++;
        if (count % 5 == 0) {
          final long curSize = listeEinsortieren.size();
          System.out.println("Liste: " + curSize);
          System.out.println("Entfernte Einträge: " + (oldSize - curSize));
          oldSize = curSize;
        }
        TimeUnit.SECONDS.sleep(2);
      } catch (final Exception ex) {
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
   * Remove links which don´t start with http. - * Remove old film entries which are smaller than
   * MIN_SIZE_ADD_OLD.
   */
  private void performInitialCleanup() {
    listeEinsortieren
        .removeIf(f -> !f.getUrl(Resolution.NORMAL).toString().toLowerCase().startsWith("http"));
    listeEinsortieren.removeIf(f -> f.getFileSize(Resolution.NORMAL) != null
        && f.getFileSize(Resolution.NORMAL) < MIN_SIZE_ADD_OLD);
  }

  private void removeExisting(final int size) {
    final Set<Integer> oldHashes =
        vonListe.stream().map(Film::hashCode).collect(Collectors.toSet());
    final Map<Integer, Film> newList = listeEinsortieren.stream()
        .collect(Collectors.toMap(Film::hashCode, Function.identity(), (f1, f2) -> f1));

    listeEinsortieren.clear();
    newList.keySet().removeAll(oldHashes);
    listeEinsortieren.addAll(newList.values());

    Log.sysLog("===== Liste einsortieren Title =====");
    Log.sysLog("Liste einsortieren, Anzahl: " + size);
    Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
    Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
    Log.sysLog("");
  }

  /**
   * Add all local thread results to the filmlist.
   *
   * @return the total number of entries found.
   */
  private int retrieveThreadResults() {
    int treffer = 0;
    for (final ImportOldFilmlistThread t : threadList) {
      final ArrayList<Film> localList = t.getLocalAddList();
      if (MserverDaten.debug) {
        Log.sysLog("Thread " + t.getName() + " list size: " + localList.size());
      }
      vonListe.addAll(localList);
      localList.clear();
      treffer += t.getTreffer();
    }
    return treffer;
  }

  private void startThreads() {
    final OkHttpClient client = MVHttpClient.getInstance().getReducedTimeOutClient();

    final List syncList = Collections.synchronizedList(listeEinsortieren);
    for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
      final ImportOldFilmlistThread t = new ImportOldFilmlistThread(syncList, client);
      t.setName("ImportOldFilmlistThread Thread-" + i);
      threadList.add(t);
      t.start();
    }
  }

  private void stopThreads() {
    if (Config.getStop()) {
      for (final ImportOldFilmlistThread t : threadList) {
        t.interrupt();
      }
      for (final ImportOldFilmlistThread t : threadList) {
        try {
          t.join();
        } catch (final InterruptedException ignored) {
        }
      }
    }
  }
}
