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
package de.mediathekview.mlib.tool;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.EventListener;

public class Listener implements EventListener {

    public static final int EREIGNIS_LISTE_HISTORY_GEAENDERT = 1;
    public static final int EREIGNIS_LISTE_PSET = 2;

    public static final int EREIGNIS_ANZAHL_DOWNLOADS = 6;
    public static final int EREIGNIS_LISTE_URL_FILMLISTEN = 7;
    public static final int EREIGNIS_LISTE_FILMLISTEN_SERVER = 8;
    public static final int EREIGNIS_LISTE_DOWNLOADS = 9;
    public static final int EREIGNIS_LISTE_ABOS = 10;
    public static final int EREIGNIS_LISTE_ERLEDIGTE_ABOS = 11;
    public static final int EREIGNIS_ART_IMPORT_FILMLISTE = 12;
    public static final int EREIGNIS_ART_DOWNLOAD_PROZENT = 13;
    public static final int EREIGNIS_START_EVENT = 14;
    public static final int EREIGNIS_START_EVENT_BUTTON = 15;

    public static final int EREIGNIS_PROGRAMM_OEFFNEN = 16;
    public static final int EREIGNIS_MEDIATHEKGUI_ORG_TITEL = 17;
    public static final int EREIGNIS_MEDIATHEKGUI_PROGRAMM_AKTUELL = 18;
    public static final int EREIGNIS_MEDIATHEKGUI_UPDATE_VERFUEGBAR = 19;
    public static final int EREIGNIS_PANEL_FILTER_ANZEIGEN = 20;
    public static final int EREIGNIS_PANEL_DOWNLOAD_FILTER_ANZEIGEN = 21;
    public static final int EREIGNIS_PANEL_ABO_FILTER_ANZEIGEN = 22;
    public static final int EREIGNIS_FILM_BESCHREIBUNG_ANZEIGEN = 23;
    public static final int EREIGNIS_DOWNLOAD_BESCHREIBUNG_ANZEIGEN = 24;

    public static final int EREIGNIS_SUCHFELD_FOCUS_SETZEN = 26;
    public static final int EREIGNIS_BLACKLIST_AUCH_FUER_ABOS = 27;
    public static final int EREIGNIS_BANDBREITE = 28;
    public static final int EREIGNIS_REIHENFOLGE_DOWNLOAD = 29;
    public static final int EREIGNIS_TIMER = 30;
    public static final int EREIGNIS_GEO = 31;
    public static final int EREIGNIS_BESCHREIBUNG = 32;
    public static final int EREIGNIS_RESET_INTERRUPT = 33;
    public static final int EREIGNIS_FILTER_ANZAHL = 34;
    public static final int EREIGNIS_TRAYICON = 35;
    public static final int EREIGNIS_FONT = 36;
    public static final int EREIGNIS_DIALOG_MEDIA_DB = 37;
    public static final int EREIGNIS_REPLACELIST_CHANGED = 38;
    public static final int EREIGNIS_BLACKLIST_GEAENDERT = 39;
    public static final int EREIGNIS_BLACKLIST_START_GEAENDERT = 40;
    public static final int EREIGNIS_MEDIA_DB_START = 41;
    public static final int EREIGNIS_MEDIA_DB_STOP = 42;
    public static final int EREIGNIS_TABS_TOP = 43;
    public static final int EREIGNIS_TOOLBAR_VIS = 44;
    public static final int EREIGNIS_TOOLBAR_BUTTON_KLEIN = 45;
    public static final int EREIGNIS_BANDWIDTH_MONITOR = 46;

    public int[] mvEreignis = {-1};
    public String klasse = "";
    private static EventListenerList listeners = new EventListenerList();

    public Listener(int eereignis, String kklasse) {
        mvEreignis = new int[]{eereignis};
        klasse = kklasse;
    }

    public Listener(int[] eereignis, String kklasse) {
        mvEreignis = eereignis;
        klasse = kklasse;
    }

    public void ping() {
    }

    public static synchronized void addListener(Listener listener) {
        listeners.add(Listener.class, listener);
    }

    public static synchronized void notify(int ereignis, String klasse) {
        for (Listener l : listeners.getListeners(Listener.class)) {
            for (int er : l.mvEreignis) {
                if (er == ereignis) {
                    if (!l.klasse.equals(klasse)) {
                        // um einen Kreislauf zu verhindern
                        try {
                            l.pingen();
                        } catch (Exception ex) {
                            DbgMsg.print("ListenerMediathekView.notifyMediathekListener: " + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void pingen() {
        try {
//            if (SwingUtilities.isEventDispatchThread()) {
//                ping();
//            } else {
            SwingUtilities.invokeLater(this::ping);
//            }
        } catch (Exception ex) {
            Log.errorLog(698989743, ex);
        }
    }
}
