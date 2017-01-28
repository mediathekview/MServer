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
package mServer.crawler.sender;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import mSearch.Config;
import mSearch.daten.DatenFilm;
import mSearch.tool.GermanStringSorter;
import mSearch.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.RunSender;

public class MediathekReader implements Runnable {

    private String sendername; // ist der Name, den der Mediathekreader hat, der ist eindeutig
    private int maxThreadLaufen; //4; // Anzahl der Thread die parallel Suchen
    private int wartenSeiteLaden; //ms, Basiswert zu dem dann der Faktor multipliziert wird, Wartezeit zwischen 2 Websiten beim Absuchen der Sender
    private int threads; // aktuelle Anz. laufender Threads
    private int max; // Anz. zu suchender Themen
    private int progress; // Prograss eben
    private int startPrio; // es gibt die Werte: 0->startet sofort, 1->später und 2->zuletzt
    protected GetUrl getUrlIo;
    protected LinkedListUrl listeThemen;
    protected FilmeSuchen mSearchFilmeSuchen;

    public MediathekReader(FilmeSuchen aMSearchFilmeSuchen, String aSendername, int aSenderMaxThread, int aSenderWartenSeiteLaden, int aStartPrio) {
        mSearchFilmeSuchen = aMSearchFilmeSuchen;
        getUrlIo = new GetUrl(aSenderWartenSeiteLaden);

        maxThreadLaufen = aSenderMaxThread;
        wartenSeiteLaden = aSenderWartenSeiteLaden;
        startPrio = aStartPrio;
        sendername = aSendername;

        threads = 0;
        max = 0;
        progress = 0;
        listeThemen = new LinkedListUrl();
    }

    public String getSendername() {
        return sendername;
    }

    public int getMaxThreadLaufen() {
        return maxThreadLaufen;
    }

    public int getWartenSeiteLaden() {
        return wartenSeiteLaden;
    }

    public int getMax() {
        return max;
    }

    public int getProgress() {
        return progress;
    }

    //===================================
    // public 
    //===================================
    class LinkedListUrl extends LinkedList<String[]> {

        private static final long serialVersionUID = 1L;

        // Hilfsklasse die das einfügen/entnehmen bei mehreren Threads unterstützt
        synchronized boolean addUrl(String[] e) {
            // e[0] ist immer die URL
            if (!istInListe(this, e[0], 0)) {
                return super.add(e);
            }
            return false;
        }

        synchronized String[] getListeThemen() {
            return this.pollFirst();
        }
    }

    public int getStartPrio() {
        return startPrio;
    }

    public int getThreads() {
        return getMaxThreadLaufen();
    }

    public long getWaitTime() {
        return getWartenSeiteLaden();
    }

    public boolean checkNameSenderFilmliste(String name) {
        // ist der Name der in der Tabelle Filme angezeigt wird
        return getSendername().equalsIgnoreCase(name);
    }

    public String getNameSender() {
        return getSendername();
    }

    public void delSenderInAlterListe(String sender) {
        mSearchFilmeSuchen.listeFilmeAlt.deleteAllFilms(sender);
    }

    public void clear() {
        //aufräumen
    }

    @Override
    public void run() {
        //alles laden
        try {
            threads = 0;
            addToList();
        } catch (Exception ex) {
            Log.errorLog(397543600, ex, getSendername());
        }
    }

    void addToList() {
        //wird überschrieben, hier werden die Filme gesucht
    }

    void addFilm(DatenFilm film, boolean urlPruefen) {
        // es werden die gefundenen Filme in die Liste einsortiert
        if (urlPruefen) {
            if (mSearchFilmeSuchen.listeFilmeNeu.getFilmByUrl(film.arr[DatenFilm.FILM_URL]) == null) {
                addFilm(film);
            }
        } else {
            addFilm(film);
        }
    }

    void addFilm(DatenFilm film) {
        // es werden die gefundenen Filme in die Liste einsortiert
        if (film.arr[DatenFilm.FILM_GROESSE].isEmpty()) {
            film.arr[DatenFilm.FILM_GROESSE] = mSearchFilmeSuchen.listeFilmeAlt.getFileSizeUrl(film.arr[DatenFilm.FILM_URL], film.arr[DatenFilm.FILM_SENDER]);
        }

        upgradeUrl(film);
        switchHttps(film);

        film.setUrlHistory();
        CrawlerTool.setGeo(film);
        if (mSearchFilmeSuchen.listeFilmeNeu.addFilmVomSender(film)) {
            // dann ist er neu
            FilmeSuchen.listeSenderLaufen.inc(film.arr[DatenFilm.FILM_SENDER], RunSender.Count.FILME);
        }
    }

    DatenFilm istInFilmListe(String sender, String thema, String titel) {
        return mSearchFilmeSuchen.listeFilmeNeu.istInFilmListe(sender, thema, titel);
    }

    boolean istInListe(LinkedList<String[]> liste, String str, int nr) {
        boolean ret = false;
        Iterator<String[]> it = liste.iterator();
        while (it.hasNext()) {
            if (it.next()[nr].equals(str)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    boolean istInListe(LinkedList<String> liste, String str) {
        boolean ret = false;
        Iterator<String> it = liste.iterator();
        while (it.hasNext()) {
            if (it.next().equals(str)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    synchronized void meldungStart() {
        // meldet den Start eines Suchlaufs
        max = 0;
        progress = 0;
        Log.sysLog("===============================================================");
        Log.sysLog("Starten[" + ((CrawlerTool.loadLongMax()) ? "alles" : "update") + "] " + getSendername() + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        Log.sysLog("   maxThreadLaufen: " + getMaxThreadLaufen());
        Log.sysLog("   wartenSeiteLaden: " + getWartenSeiteLaden());
        Log.sysLog("");
        RunSender runSender = mSearchFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
        runSender.maxThreads = getMaxThreadLaufen(); //runSender ist erst jetzt angelegt
        runSender.waitOnLoad = getWartenSeiteLaden();
    }

    synchronized void meldungAddMax(int mmax) {
        max = max + mmax;
        mSearchFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
    }

    synchronized void meldungAddThread() {
        threads++;
        mSearchFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
    }

    synchronized void meldungProgress(String text) {
        progress++;
        mSearchFilmeSuchen.melden(getSendername(), getMax(), getProgress(), text);
    }

    synchronized void meldung(String text) {
        mSearchFilmeSuchen.melden(getSendername(), getMax(), getProgress(), text);
    }

    synchronized void meldungThreadUndFertig() {
        // meldet das Ende eines!!! Threads
        // der MediathekReader ist erst fertig wenn alle gestarteten Threads fertig sind!!
        threads--;
        if (getThreads() <= 0) {
            //wird erst ausgeführt wenn alle Threads beendet sind
            mSearchFilmeSuchen.meldenFertig(getSendername());
        } else {
            // läuft noch was
            mSearchFilmeSuchen.melden(getSendername(), getMax(), getProgress(), "" /* text */);
        }
    }

    private void switchHttps(DatenFilm film) {
        // versuchen https nach http zu ändern
        if (film.arr[DatenFilm.FILM_URL].startsWith("https://")) {
            String from = film.arr[DatenFilm.FILM_URL];
            String to = film.arr[DatenFilm.FILM_URL].replaceFirst("https://", "http://");
            if (urlExists(to)) {
                film.arr[DatenFilm.FILM_URL] = to;
            } else {
                Log.errorLog(612125478, "change https: " + from);
            }
        }
        if (!film.arr[DatenFilm.FILM_URL_HD].isEmpty()) {
            String from = film.getUrlFuerAufloesung(DatenFilm.AUFLOESUNG_HD);
            if (from.startsWith("https://")) {
                String to = from.replaceFirst("https://", "http://");
                if (urlExists(to)) {
                    CrawlerTool.addUrlHd(film, to, "");
                } else {
                    Log.errorLog(915236445, "change https: " + from);
                }
            }
        }
        if (!film.arr[DatenFilm.FILM_URL_KLEIN].isEmpty()) {
            String from = film.getUrlFuerAufloesung(DatenFilm.AUFLOESUNG_KLEIN);
            if (from.startsWith("https://")) {
                String to = from.replaceFirst("https://", "http://");
                if (urlExists(to)) {
                    CrawlerTool.addUrlKlein(film, to, "");
                } else {
                    Log.errorLog(751230145, "change https: " + from);
                }
            }
        }
    }

    private void upgradeUrl(DatenFilm film) {
        // versuchen HD anhand der URL zu suchen, wo noch nicht vorhanden
        if (film.isHD()) {
            return;
        }

        // http://media.ndr.de/progressive/2016/0817/TV-20160817-1113-2300.hq.mp4
        // http://media.ndr.de/progressive/2016/0817/TV-20160817-1113-2300.hd.mp4 -> HD
        if (film.arr[DatenFilm.FILM_URL].startsWith("http://media.ndr.de") && film.arr[DatenFilm.FILM_URL].endsWith(".hq.mp4")) {
            String from = film.arr[DatenFilm.FILM_URL];
            String to = film.arr[DatenFilm.FILM_URL].replace(".hq.mp4", ".hd.mp4");
            updateHd(from, to, film);
        }

        // http://cdn-storage.br.de/iLCpbHJGNLT6NK9HsLo6s61luK4C_2rc571S/_AJS/_ArG_2bP_71S/583da0ef-3e92-4648-bb22-1b14d739aa91_C.mp4
        // http://cdn-storage.br.de/iLCpbHJGNLT6NK9HsLo6s61luK4C_2rc571S/_AJS/_ArG_2bP_71S/583da0ef-3e92-4648-bb22-1b14d739aa91_X.mp4 -> HD
        if (film.arr[DatenFilm.FILM_URL].startsWith("http://cdn-storage.br.de") && film.arr[DatenFilm.FILM_URL].endsWith("_C.mp4")) {
            String from = film.arr[DatenFilm.FILM_URL];
            String to = film.arr[DatenFilm.FILM_URL].replace("_C.mp4", "_X.mp4");
            updateHd(from, to, film);
        }

        // http://pd-ondemand.swr.de/das-erste/buffet/904278.l.mp4
        // http://pd-ondemand.swr.de/das-erste/buffet/904278.xl.mp4 -> HD
        if (film.arr[DatenFilm.FILM_URL].startsWith("http://pd-ondemand.swr.de") && film.arr[DatenFilm.FILM_URL].endsWith(".l.mp4")) {
            String from = film.arr[DatenFilm.FILM_URL];
            String to = film.arr[DatenFilm.FILM_URL].replace(".l.mp4", ".xl.mp4");
            updateHd(from, to, film);
        }
    }

    private void updateHd(String from, String to, DatenFilm film) {
        if (film.arr[DatenFilm.FILM_URL_HD].isEmpty() && film.arr[DatenFilm.FILM_URL].endsWith(from)) {
            String url_ = film.arr[DatenFilm.FILM_URL].substring(0, film.arr[DatenFilm.FILM_URL].lastIndexOf(from)) + to;
            // zum Testen immer machen!!
            if (urlExists(url_)) {
                CrawlerTool.addUrlHd(film, url_, "");
                //Log.sysLog("upgradeUrl: " + film.arr[DatenFilm.FILM_SENDER]);
            } else {
                Log.errorLog(945120347, "upgradeUrl: " + from);
            }
        }
    }

    final static int TIMEOUT = 3000; // ms //ToDo evtl. wieder kürzen!!

    public static boolean urlExists(String url) {
        // liefert liefert true, wenn es die URL gibt
        // brauchts, um Filmurls zu prüfen
        int retCode;
        if (!url.toLowerCase().startsWith("http")) {
            return false;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", Config.getUserAgent());
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            if ((retCode = conn.getResponseCode()) < 400) {
                return true;
            } else if (retCode == 403) {
                // aber sie gibt es :)
                return true;
            }
            conn.disconnect();
        } catch (Exception ignored) {
        }
        return false;
    }

    String addsUrl(String pfad1, String pfad2) {
        String ret = "";
        if (pfad1 != null && pfad2 != null) {
            if (!pfad1.equals("") && !pfad2.equals("")) {
                if (pfad1.charAt(pfad1.length() - 1) == '/') {
                    ret = pfad1.substring(0, pfad1.length() - 1);
                } else {
                    ret = pfad1;
                }
                if (pfad2.charAt(0) == '/') {
                    ret += pfad2;
                } else {
                    ret += '/' + pfad2;
                }
            }
        }
        if (ret.equals("")) {
            Log.errorLog(469872800, pfad1 + " " + pfad2);
        }
        return ret;
    }

    static void listeSort(LinkedList<String[]> liste, int stelle) {
        //Stringliste alphabetisch sortieren
        GermanStringSorter sorter = GermanStringSorter.getInstance();
        if (liste != null) {
            String str1;
            String str2;
            for (int i = 1; i < liste.size(); ++i) {
                for (int k = i; k > 0; --k) {
                    str1 = liste.get(k - 1)[stelle];
                    str2 = liste.get(k)[stelle];
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (sorter.compare(str1, str2) > 0) {
                        liste.add(k - 1, liste.remove(k));
                    } else {
                        break;
                    }
                }
            }
        }
    }

    static long extractDuration(String dauer) {
        long dauerInSeconds = 0;
        if (dauer.isEmpty()) {
            return 0;
        }
        try {
            if (dauer.contains("min")) {
                dauer = dauer.replace("min", "").trim();
                dauerInSeconds = Long.parseLong(dauer) * 60;
            } else {
                String[] parts = dauer.split(":");
                long power = 1;
                for (int i = parts.length - 1; i >= 0; i--) {
                    dauerInSeconds += Long.parseLong(parts[i]) * power;
                    power *= 60;
                }
            }
        } catch (Exception ex) {
            return 0;
        }
        return dauerInSeconds;
    }

    static long extractDurationSec(String dauer) {
        long dauerInSeconds = 0;
        if (dauer.isEmpty()) {
            return 0;
        }
        try {
            dauerInSeconds = Long.parseLong(dauer);
        } catch (Exception ex) {
            return 0;
        }
        return dauerInSeconds;
    }
}
