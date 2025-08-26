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

import de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl;

import java.util.*;

@SuppressWarnings("serial")
public class ListeFilmlistenUrls extends LinkedList<de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl> {
    // ist die Liste mit den URLs zum Download einer Filmliste
    public boolean addWithCheck(de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl filmliste) {
        for (de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl datenUrlFilmliste : this) {
            if (datenUrlFilmliste.arr[de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR].equals(filmliste.arr[de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
                return false;
            }
        }
        return add(filmliste);
    }

    public void sort() {
        int nr = 0;
        Collections.sort(this);
        for (de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl datenUrlFilmliste : this) {
            String str = String.valueOf(nr++);
            while (str.length() < 3) {
                str = "0" + str;
            }
            datenUrlFilmliste.arr[de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_NR_NR] = str;
        }
    }

    public String[][] getTableObjectData() {
        de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl filmUpdate;
        String[][] object;
        Iterator<de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl> iterator = this.iterator();
        object = new String[this.size()][de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_MAX_ELEM];
        int i = 0;
        while (iterator.hasNext()) {
            filmUpdate = iterator.next();
            System.arraycopy(filmUpdate.arr, 0, object[i], 0, de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_MAX_ELEM);
            object[i][de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_DATUM_NR] = filmUpdate.getDateStr(); // lokale Zeit anzeigen
            object[i][de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_ZEIT_NR] = filmUpdate.getTimeStr(); // lokale Zeit anzeigen
            ++i;
        }
        return object;
    }

    public de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl getDatenUrlFilmliste(String url) {
        de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl update;
        for (de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl datenUrlFilmliste : this) {
            update = datenUrlFilmliste;
            if (update.arr[de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR].equals(url)) {
                return update;
            }
        }
        return null;
    }

    public String getRand(ArrayList<String> bereitsGebraucht) {
        // gibt nur noch akt.xml und diff.xml und da sind alle Listen
        // aktuell, Prio: momentan sind alle Listen gleich gewichtet
        if (this.isEmpty()) {
            return "";
        }

        LinkedList<de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl> listePrio = new LinkedList<>();
        //nach prio gewichten
        for (de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl datenFilmlisteUrl : this) {
            if (bereitsGebraucht != null) {
                if (bereitsGebraucht.contains(datenFilmlisteUrl.arr[de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
                    // wurde schon versucht
                    continue;
                }
                if (datenFilmlisteUrl.arr[de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_NR].equals(de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1)) {
                    listePrio.add(datenFilmlisteUrl);
                    listePrio.add(datenFilmlisteUrl);
                } else {
                    listePrio.add(datenFilmlisteUrl);
                    listePrio.add(datenFilmlisteUrl);
                    listePrio.add(datenFilmlisteUrl);
                }
            }
        }

        de.mediathekview.mlib.filmlisten.DatenFilmlisteUrl datenFilmlisteUrl;
        if (!listePrio.isEmpty()) {
            int nr = new Random().nextInt(listePrio.size());
            datenFilmlisteUrl = listePrio.get(nr);
        } else {
            // dann wird irgendeine Versucht
            int nr = new Random().nextInt(this.size());
            datenFilmlisteUrl = this.get(nr);
        }
        return datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR];
    }

//    public String getRand(ArrayList<String> bereitsGebraucht, int errcount) {
//        final int MAXMINUTEN = 50;
//        int minCount = 3;
//        if (errcount > 0) {
//            minCount = 3 + 2 * errcount;
//        }
//        String ret = "";
//        if (!this.isEmpty()) {
//            DatenFilmlisteUrl datenUrlFilmliste;
//            LinkedList<DatenFilmlisteUrl> listeZeit = new LinkedList<>();
//            LinkedList<DatenFilmlisteUrl> listePrio = new LinkedList<>();
//            //aktuellsten auswählen
//            Iterator<DatenFilmlisteUrl> it = this.iterator();
//            Date today = new Date(System.currentTimeMillis());
//            Date d;
//            int minuten = 200;
//            int count = 0;
//            while (it.hasNext()) {
//                datenUrlFilmliste = it.next();
//                if (bereitsGebraucht != null) {
//                    if (bereitsGebraucht.contains(datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
//                        // wurde schon versucht
//                        continue;
//                    }
//                }
//                try {
//                    d = datenUrlFilmliste.getDate();
//                    // debug
//                    //SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
//                    //String s = sdf_datum_zeit.format(d);
//                    long m = today.getTime() - d.getTime();
//                    if (m < 0) {
//                        m = 0;
//                    }
//                    minuten = Math.round(m / (1000 * 60));
//                } catch (Exception ignored) {
//                }
//                if (minuten < MAXMINUTEN) {
//                    listeZeit.add(datenUrlFilmliste);
//                    ++count;
//                } else if (count < minCount) {
//                    listeZeit.add(datenUrlFilmliste);
//                    ++count;
//                }
//            }
//            //nach prio gewichten
//            it = listeZeit.iterator();
//            while (it.hasNext()) {
//                datenUrlFilmliste = it.next();
//                if (datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_NR].equals(DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1)) {
//                    listePrio.add(datenUrlFilmliste);
//                } else {
//                    listePrio.add(datenUrlFilmliste);
//                    listePrio.add(datenUrlFilmliste);
//                }
//            }
//            if (listePrio.size() > 0) {
//                int nr = new Random().nextInt(listePrio.size());
//                datenUrlFilmliste = listePrio.get(nr);
//            } else {
//                // dann wird irgendeine Versucht
//                int nr = new Random().nextInt(this.size());
//                datenUrlFilmliste = this.get(nr);
//            }
//            ret = datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR];
//        }
//        return ret;
//    }
}
