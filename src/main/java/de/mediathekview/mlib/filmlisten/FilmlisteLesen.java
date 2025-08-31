/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.mediathekview.mlib.filmlisten;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLaden;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLadenEvent;
import de.mediathekview.mlib.tool.InputStreamProgressMonitor;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mlib.tool.ProgressMonitorInputStream;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.time.FastDateFormat;
import org.tukaani.xz.XZInputStream;

import javax.swing.event.EventListenerList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

public class FilmlisteLesen {
    private static final int PROGRESS_MAX = 100;
    private static WorkMode workMode = WorkMode.NORMAL; // die Klasse wird an verschiedenen Stellen benutzt, klappt sonst nicht immer, zB. FilmListe zu alt und neu laden
    private final EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;
    private long milliseconds = 0;

    /**
     * Set the specific work mode for reading film list.
     * In FASTAUTO mode, no film descriptions will be read into memory.
     *
     * @param mode The mode in which to operate when reading film list.
     */
    public static void setWorkMode(WorkMode mode) {
        workMode = mode;
    }

    public void addAdListener(ListenerFilmeLaden listener) {
        listeners.add(ListenerFilmeLaden.class, listener);
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception {
        if (source.endsWith(Const.FORMAT_XZ)) {
            in = new XZInputStream(in);
        } else if (source.endsWith(Const.FORMAT_ZIP)) {
            ZipInputStream zipInputStream = new ZipInputStream(in);
            zipInputStream.getNextEntry();
            in = zipInputStream;
        }
        return in;
    }

    private void readData(JsonParser jp, ListeFilme listeFilme) throws IOException {
        JsonToken jsonToken;
        String sender = "", thema = "";

        if (jp.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected data to start with an Object");
        }

        while ((jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                for (int k = 0; k < ListeFilme.MAX_ELEM; ++k) {
                    listeFilme.metaDaten[k] = jp.nextTextValue();
                }
                break;
            }
        }
        while ((jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                // sind nur die Feldbeschreibungen, brauch mer nicht
                jp.nextToken();
                break;
            }
        }
        while (!Config.getStop() && (jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                DatenFilm datenFilm = new DatenFilm();
                for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                    //if we are in FASTAUTO mode, we don´t need film descriptions.
                    //this should speed up loading on low end devices...
                    if (workMode == WorkMode.FASTAUTO) {
                        if (DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_BESCHREIBUNG
                                || DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_WEBSEITE
                                || DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_GEO) {
                            jp.nextToken();
                            continue;
                        }
                    }
                    if (DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_NEU) {
                        final String value = jp.nextTextValue();
                        //This value is unused...
                        //datenFilm.arr[DatenFilm.FILM_NEU_NR] = value;
                        datenFilm.setNew(Boolean.parseBoolean(value));
                    } else {
                        datenFilm.arr[DatenFilm.JSON_NAMES[i]] = jp.nextTextValue();
                    }

                    /// für die Entwicklungszeit
                    if (datenFilm.arr[DatenFilm.JSON_NAMES[i]] == null) {
                        datenFilm.arr[DatenFilm.JSON_NAMES[i]] = "";
                    }
                }
                if (datenFilm.arr[DatenFilm.FILM_SENDER].isEmpty()) {
                    datenFilm.arr[DatenFilm.FILM_SENDER] = sender;
                } else {
                    sender = datenFilm.arr[DatenFilm.FILM_SENDER];
                }
                if (datenFilm.arr[DatenFilm.FILM_THEMA].isEmpty()) {
                    datenFilm.arr[DatenFilm.FILM_THEMA] = thema;
                } else {
                    thema = datenFilm.arr[DatenFilm.FILM_THEMA];
                }

                listeFilme.importFilmliste(datenFilm);
                if (milliseconds > 0) {
                    // muss "rückwärts" laufen, da das Datum sonst 2x gebaut werden muss
                    // wenns drin bleibt, kann mans noch ändern
                    if (!checkDate(datenFilm)) {
                        listeFilme.remove(datenFilm);
                    }
                }
            }
        }
    }

    /**
     * Read a locally available filmlist.
     *
     * @param source     file path as string
     * @param listeFilme the list to read to
     */
    private void processFromFile(String source, ListeFilme listeFilme) {
        notifyProgress(source, PROGRESS_MAX);
        try (InputStream in = selectDecompressor(source, new FileInputStream(source));
             JsonParser jp = new JsonFactory().createParser(in)) {
            readData(jp, listeFilme);
        } catch (FileNotFoundException ex) {
            Log.errorLog(894512369, "FilmListe existiert nicht: " + source);
            listeFilme.clear();
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            listeFilme.clear();
        }
    }

    private void checkDays(long days) {
        if (days > 0) {
            milliseconds = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
        } else {
            milliseconds = 0;
        }
    }

    public void readFilmListe(String source, final ListeFilme listeFilme, int days) {
        try {
            Log.sysLog("Liste Filme lesen von: " + source);
            listeFilme.clear();
            this.notifyStart(source, PROGRESS_MAX); // für die Progressanzeige

            checkDays(days);

            if (!source.startsWith("http")) {
                processFromFile(source, listeFilme);
            } else {
                processFromWeb(new URL(source), listeFilme);
            }

            if (Config.getStop()) {
                Log.sysLog("--> Abbruch");
                listeFilme.clear();
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        notifyFertig(source, listeFilme);
    }

    /**
     * Download a process a filmliste from the web.
     *
     * @param source     source url as string
     * @param listeFilme the list to read to
     */
    private void processFromWeb(URL source, ListeFilme listeFilme) {
        Request.Builder builder = new Request.Builder().url(source);
        builder.addHeader("User-Agent", Config.getUserAgent());

        //our progress monitor callback
        InputStreamProgressMonitor monitor = new InputStreamProgressMonitor() {
            private int oldProgress = 0;

            @Override
            public void progress(long bytesRead, long size) {
                final int iProgress = (int) (bytesRead * 100 / size);
                if (iProgress != oldProgress) {
                    oldProgress = iProgress;
                    notifyProgress(source.toString(), iProgress);
                }
            }
        };

        try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(builder.build()).execute();
             ResponseBody body = response.body()) {
            if (response.isSuccessful()) {
                try (InputStream input = new ProgressMonitorInputStream(body.byteStream(), body.contentLength(), monitor)) {
                    try (InputStream is = selectDecompressor(source.toString(), input);
                         JsonParser jp = new JsonFactory().createParser(is)) {
                        readData(jp, listeFilme);
                    }
                }
            }
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            listeFilme.clear();
        }
    }

    private boolean checkDate(DatenFilm film) {
        // true wenn der Film angezeigt werden kann!
        try {
            if (film.datumFilm.getTime() != 0) {
                if (film.datumFilm.getTime() < milliseconds) {
                    return false;
                }
            }
        } catch (Exception ex) {
            Log.errorLog(495623014, ex);
        }
        return true;
    }

    private void notifyStart(String url, int mmax) {
        max = mmax;
        progress = 0;
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.start(new ListenerFilmeLadenEvent(url, "", max, 0, 0, false));
        }
    }

    private void notifyProgress(String url, int iProgress) {
        progress = iProgress;
        if (progress > max) {
            progress = max;
        }
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.progress(new ListenerFilmeLadenEvent(url, "Download", max, progress, 0, false));
        }
    }

    private void notifyFertig(String url, ListeFilme liste) {
        Log.sysLog("Liste Filme gelesen am: " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        Log.sysLog("  erstellt am: " + liste.genDate());
        Log.sysLog("  Anzahl Filme: " + liste.size());
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.fertig(new ListenerFilmeLadenEvent(url, "", max, progress, 0, false));
        }
    }

    public enum WorkMode {

        NORMAL, FASTAUTO
    }
}
