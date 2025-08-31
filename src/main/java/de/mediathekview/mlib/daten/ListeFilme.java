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
package de.mediathekview.mlib.daten;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Duration;
import de.mediathekview.mlib.tool.FileSize;
import de.mediathekview.mlib.tool.Functions;
import de.mediathekview.mlib.tool.Log;
import org.apache.commons.lang3.time.FastDateFormat;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

@SuppressWarnings("serial")
public class ListeFilme extends ArrayList<de.mediathekview.mlib.daten.DatenFilm> {
    public static final String THEMA_LIVE = "Livestream";
    public static final String FILMLISTE = "Filmliste";
    public static final String FILMLISTE_DATUM = "Filmliste-Datum";
    public static final int FILMLISTE_DATUM_NR = 0;
    public static final String FILMLISTE_DATUM_GMT = "Filmliste-Datum-GMT";
    public static final int FILMLISTE_DATUM_GMT_NR = 1;
    public static final String FILMLISTE_VERSION = "Filmliste-Version";
    public static final int FILMLISTE_VERSION_NR = 2;
    public static final String FILMLISTE_PROGRAMM = "Filmliste-Programm";
    public static final int FILMLISTE_PRGRAMM_NR = 3;
    public static final String FILMLISTE_ID = "Filmliste-Id";
    public static final int FILMLISTE_ID_NR = 4;
    public static final int MAX_ELEM = 5;
    public static final String[] COLUMN_NAMES = {FILMLISTE_DATUM, FILMLISTE_DATUM_GMT, FILMLISTE_VERSION, FILMLISTE_PROGRAMM, FILMLISTE_ID};
    public int nr = 1;
    public String[] metaDaten = new String[]{"", "", "", "", ""};
    private final static String DATUM_ZEIT_FORMAT = "dd.MM.yyyy, HH:mm";
    private final SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
    public String[] sender = {""};
    public String[][] themenPerSender = {{""}};
    public boolean neueFilme = false;

    private final Collection<String> indexes;

    public ListeFilme()
    {
        super();
        indexes = new ConcurrentSkipListSet<>();
    }


    public synchronized boolean importFilmliste(de.mediathekview.mlib.daten.DatenFilm film) {
        // hier nur beim Laden aus einer fertigen Filmliste mit der GUI
        // die Filme sind schon sortiert, nur die Nummer muss noch ergänzt werden
        film.nr = nr++;
        return addInit(film);
    }

    public synchronized boolean addFilmVomSender(de.mediathekview.mlib.daten.DatenFilm film) {
        // Filme die beim Sender gesucht wurden (und nur die) hier eintragen, nur für die MediathekReader!!
        // ist: "Sender-Thema-URL" schon vorhanden, wird sie verworfen

        Functions.unescape(film);

        return !indexes.contains(film.getIndex()) && addInit(film);
    }

    private void addHash(de.mediathekview.mlib.daten.DatenFilm f, HashSet<String> hash, boolean index) {
        if (f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
            // beim KIKA ändern sich die URLs laufend
            hash.add(f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] + f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL]);
        } else if (index) {
            hash.add(f.getIndex());
        } else {
            hash.add(f.getUrl());
        }
    }

    public synchronized void updateListe(ListeFilme listeEinsortieren, boolean index /* Vergleich über Index, sonst nur URL */, boolean ersetzen) {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        // "ersetzen": true: dann werden gleiche (index/URL) in der Liste durch neue ersetzt
        final HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);

        if (ersetzen) {
            listeEinsortieren.forEach((de.mediathekview.mlib.daten.DatenFilm f) -> addHash(f, hash, index));

            Iterator<de.mediathekview.mlib.daten.DatenFilm> it = this.iterator();
            while (it.hasNext()) {
                de.mediathekview.mlib.daten.DatenFilm f = it.next();
                if (f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
                    // beim KIKA ändern sich die URLs laufend
                    if (hash.contains(f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] + f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL])) {
                        it.remove();
                    }
                } else if (index) {
                    if (hash.contains(f.getIndex())) {
                        it.remove();
                    }
                } else if (hash.contains(f.getUrl())) {
                    it.remove();
                }
            }

            listeEinsortieren.forEach(this::addInit);
        } else {
            // ==============================================
            this.forEach(f -> addHash(f, hash, index));

            for (de.mediathekview.mlib.daten.DatenFilm f : listeEinsortieren) {
                if (f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
                    if (!hash.contains(f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] + f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL])) {
                        addInit(f);
                        addHash(f, hash, index);
                    }
                } else if (index) {
                    if (!hash.contains(f.getIndex())) {
                        addInit(f);
                        addHash(f, hash, index);
                    }
                } else if (!hash.contains(f.getUrl())) {
                    addInit(f);
                    addHash(f, hash, index);
                }
            }
        }
        hash.clear();
    }

    private boolean addInit(de.mediathekview.mlib.daten.DatenFilm film) {
        film.init();
        return add(film);
    }

    @Override
    public boolean add(de.mediathekview.mlib.daten.DatenFilm aFilm)
    {
        indexes.add(aFilm.getIndex());
        return super.add(aFilm);
    }

    @Override
    public synchronized void clear() {
        nr = 1;
        neueFilme = false;
        indexes.clear();
        super.clear();
    }

    public synchronized void check() {
        // zum Debuggen
        for (de.mediathekview.mlib.daten.DatenFilm film : this) {
            String s = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_BESCHREIBUNG];
            film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_BESCHREIBUNG] = Functions.removeHtml(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_BESCHREIBUNG]);
            if (!s.equals(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_BESCHREIBUNG])) {
                System.out.println("---------------------");
                System.out.println(s);
                System.out.println(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_BESCHREIBUNG]);
            }
            s = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA];
            film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] = Functions.removeHtml(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA]);
            if (!s.equals(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA])) {
                System.out.println("---------------------");
                System.out.println(s);
                System.out.println(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA]);
            }
            s = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL];
            film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL] = Functions.removeHtml(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL]);
            if (!s.equals(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL])) {
                System.out.println("---------------------");
                System.out.println(s);
                System.out.println(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL]);
            }
            if (film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL].contains(" ")) {
                System.out.println(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL]);
            }
        }
    }

    public synchronized void sort() {
        Collections.sort(this);
        // und jetzt noch die Nummerierung in Ordnung bringen
        int i = 1;
        for (de.mediathekview.mlib.daten.DatenFilm film : this) {
            film.nr = i++;
        }
    }

    public synchronized void setMeta(ListeFilme listeFilme) {
        System.arraycopy(listeFilme.metaDaten, 0, metaDaten, 0, MAX_ELEM);
    }

    public synchronized ListeFilme neueFilme(ListeFilme orgListe) {
        // Funktion liefert eine Liste mit Filmen
        // die im Vergleich zur Liste "orgListe"
        // neu sind, also ein Diff mit nur den neuen Filmen in DIESER Liste
        ListeFilme ret = new ListeFilme();
        final HashSet<String> hashSet = new HashSet<>(orgListe.size() + 1, 1);

        for (de.mediathekview.mlib.daten.DatenFilm film : orgListe) {
            final String s = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER] + film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] + film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL] + film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL];
            hashSet.add(s);
        }

        for (de.mediathekview.mlib.daten.DatenFilm film : this) {
            final String s = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER] + film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] + film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_TITEL] + film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL];
            if (!hashSet.contains(s)) {
                ret.add(film);
            }
        }

        hashSet.clear();

        ret.metaDaten = metaDaten;
        return ret;
    }

    /**
     * @param url the URL as String.
     * @return the determined size or -1.
     * @deprecated Move this someday to DatenFilm.
     */
    @Deprecated
    public String getFileSizeUrl(String url) {
        //FIXME bring to DatenFilm and reduce calculation
        String res;

        Optional<de.mediathekview.mlib.daten.DatenFilm> opt = this.parallelStream()
                .filter(f -> f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL].equals(url)).findAny();
        if (opt.isPresent()) {
            de.mediathekview.mlib.daten.DatenFilm film = opt.get();
            if (!film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_GROESSE].isEmpty())
                res = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_GROESSE];
            else
                res = FileSize.laengeString(url);
        } else
            res = FileSize.laengeString(url);

        return res;
    }

    /**
     * Delete all films from specified sender.
     *
     * @param sender Sender which films are to be deleted.
     */
    public synchronized void deleteAllFilms(String sender) {
        removeIf(film -> film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER].equalsIgnoreCase(sender));
    }


    public synchronized de.mediathekview.mlib.daten.DatenFilm getFilmByUrl(final String url) {
        Optional<de.mediathekview.mlib.daten.DatenFilm> opt = this.parallelStream().filter(f -> f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL].equalsIgnoreCase(url)).findAny();
        return opt.orElse(null);
    }

    public synchronized void checkThema(String sender, LinkedList<String> liste, String thema) {
        this.stream().filter(film -> film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER].equals(sender))
                .filter(film -> !film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE)
                        && !liste.contains(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA]))
                .forEach(film -> film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA] = thema);
    }

    public synchronized void getThema(String sender, LinkedList<String> liste) {
        this.stream().filter(film -> film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER].equals(sender))
                .filter(film -> !liste.contains(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA]))
                .forEach(film -> liste.add(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_THEMA]));
    }

    public synchronized de.mediathekview.mlib.daten.DatenFilm getFilmByUrl_klein_hoch_hd(String url) {
        // Problem wegen gleicher URLs
        // wird versucht, einen Film mit einer kleinen/Hoher/HD-URL zu finden
        de.mediathekview.mlib.daten.DatenFilm ret = null;
        for (de.mediathekview.mlib.daten.DatenFilm f : this) {
            if (f.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_URL].equals(url)) {
                ret = f;
                break;
            } else if (f.getUrlFuerAufloesung(de.mediathekview.mlib.daten.DatenFilm.AUFLOESUNG_HD).equals(url)) {
                ret = f;
                break;
            } else if (f.getUrlFuerAufloesung(de.mediathekview.mlib.daten.DatenFilm.AUFLOESUNG_KLEIN).equals(url)) {
                ret = f;
                break;
            }
        }

        return ret;
    }

    public synchronized String genDate() {
        // Tag, Zeit in lokaler Zeit wann die Filmliste erstellt wurde
        // in der Form "dd.MM.yyyy, HH:mm"
        String ret;
        String date;
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].isEmpty()) {
            // noch eine alte Filmliste
            ret = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            SimpleDateFormat sdf_ = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
            sdf_.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            Date filmDate = null;
            try {
                filmDate = sdf_.parse(date);
            } catch (ParseException ignored) {
            }
            if (filmDate == null) {
                ret = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            } else {
                FastDateFormat formatter = FastDateFormat.getInstance(DATUM_ZEIT_FORMAT);
                ret = formatter.format(filmDate);
            }
        }
        return ret;
    }

    public synchronized String getId() {
        // liefert die ID einer Filmliste
        return metaDaten[ListeFilme.FILMLISTE_ID_NR];
    }

    /**
     * Get the age of the film list.
     *
     * @return Age in seconds.
     */
    public int getAge() {
        int ret = 0;
        Date now = new Date(System.currentTimeMillis());
        Date filmDate = getAgeAsDate();
        if (filmDate != null) {
            ret = Math.round((now.getTime() - filmDate.getTime()) / (1000));
            if (ret < 0) {
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Get the age of the film list.
     *
     * @return Age as a {@link Date} object.
     */
    public Date getAgeAsDate() {
        String date;
        if (!metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].isEmpty()) {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        }
        Date filmDate = null;
        try {
            filmDate = sdf.parse(date);
        } catch (ParseException ignored) {
        }

        return filmDate;
    }

    /**
     * Check if available Filmlist is older than a specified value.
     *
     * @return true if too old or if the list is empty.
     */
    public synchronized boolean isTooOld() {
        return (isEmpty()) || (isOlderThan(Const.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE));
    }

    /**
     * Check if Filmlist is too old for using a diff list.
     *
     * @return true if empty or too old.
     */
    public synchronized boolean isTooOldForDiff() {
        if (isEmpty()) {
            return true;
        }
        try {
            final String dateMaxDiff_str = new SimpleDateFormat("yyyy.MM.dd__").format(new Date()) + Const.TIME_MAX_AGE_FOR_DIFF + ":00:00";
            final Date dateMaxDiff = new SimpleDateFormat("yyyy.MM.dd__HH:mm:ss").parse(dateMaxDiff_str);
            final Date dateFilmliste = getAgeAsDate();
            if (dateFilmliste != null) {
                return dateFilmliste.getTime() < dateMaxDiff.getTime();
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    /**
     * Check if list is older than specified parameter.
     *
     * @param sekunden The age in seconds.
     * @return true if older.
     */
    public boolean isOlderThan(int sekunden) {
        int ret = getAge();
        if (ret != 0) {
            Log.sysLog("Die Filmliste ist " + ret / 60 + " Minuten alt");
        }
        return ret > sekunden;
    }

    public synchronized void writeMetaData() {
        for (int i = 0; i < metaDaten.length; ++i) {
            metaDaten[i] = "";
        }
        metaDaten[ListeFilme.FILMLISTE_DATUM_NR] = getJetzt_ddMMyyyy_HHmm();
        metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR] = getJetzt_ddMMyyyy_HHmm_gmt();
        metaDaten[ListeFilme.FILMLISTE_ID_NR] = createChecksum(metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR]);
        metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = Const.VERSION_FILMLISTE;
        metaDaten[ListeFilme.FILMLISTE_PRGRAMM_NR] = Const.PROGRAMMNAME + Functions.getProgVersionString(); //  + " - Compiled: " + Functions.getCompileDate();
    }

    /**
     * Create a checksum string as a unique identifier.
     *
     * @param input The base string for the checksum.
     * @return MD5-hashed checksum string.
     */
    private String createChecksum(String input) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            for (byte b : digest) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception ignored) {
        }
        return sb.toString();
    }


    private String getJetzt_ddMMyyyy_HHmm() {
        FastDateFormat formatter = FastDateFormat.getInstance(DATUM_ZEIT_FORMAT);
        return formatter.format(new Date());
    }

    private String getJetzt_ddMMyyyy_HHmm_gmt() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return formatter.format(new Date());
    }

    public synchronized long countNewFilms() {
        return this.stream().filter(de.mediathekview.mlib.daten.DatenFilm::isNew).count();
    }

    private static final String THEME_SEARCH_TEXT = "Themen in Filmliste suchen";
    /**
     * Erstellt ein StringArray der Themen eines Senders oder wenn "sender" leer, aller Sender.
     * Ist für die Filterfelder in GuiFilme.
     */
    @SuppressWarnings("unchecked")
    public synchronized void themenLaden() {
        Duration.counterStart(THEME_SEARCH_TEXT);
        LinkedHashSet<String> senderSet = new LinkedHashSet<>(21);
        // der erste Sender ist ""
        senderSet.add("");

        for (de.mediathekview.mlib.daten.DatenFilm film : this) {
            senderSet.add(film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER]);
        }
        sender = senderSet.toArray(new String[senderSet.size()]);
        senderSet.clear();

        //für den Sender "" sind alle Themen im themenPerSender[0]
        final int senderLength = sender.length;
        themenPerSender = new String[senderLength][];
        TreeSet<String>[] tree = (TreeSet<String>[]) new TreeSet<?>[senderLength];
        HashSet<String>[] hashSet = (HashSet<String>[]) new HashSet<?>[senderLength];
        for (int i = 0; i < tree.length; ++i) {
            tree[i] = new TreeSet<>(de.mediathekview.mlib.tool.GermanStringSorter.getInstance());
            tree[i].add("");
            hashSet[i] = new HashSet<>();
        }

        //alle Themen
        String filmThema, filmSender;
        for (de.mediathekview.mlib.daten.DatenFilm film : this) {
            filmSender = film.arr[de.mediathekview.mlib.daten.DatenFilm.FILM_SENDER];
            filmThema = film.arr[DatenFilm.FILM_THEMA];
            //hinzufügen
            if (!hashSet[0].contains(filmThema)) {
                hashSet[0].add(filmThema);
                tree[0].add(filmThema);
            }
            for (int i = 1; i < senderLength; ++i) {
                if (filmSender.equals(sender[i])) {
                    if (!hashSet[i].contains(filmThema)) {
                        hashSet[i].add(filmThema);
                        tree[i].add(filmThema);
                    }
                }
            }
        }
        for (int i = 0; i < themenPerSender.length; ++i) {
            themenPerSender[i] = tree[i].toArray(new String[tree[i].size()]);
            tree[i].clear();
            hashSet[i].clear();
        }
        Duration.counterStop(THEME_SEARCH_TEXT);
    }
}
