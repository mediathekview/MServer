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
package mediathekServer.tool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import mediathek.tool.Funktionen;

public class MS_Log {

    private static LinkedList<Integer[]> fehlerListe = new LinkedList<Integer[]>(); // [Fehlernummer, Anzahl]
    private static Date startZeit = new Date(System.currentTimeMillis());
    private static Date stopZeit = null;

    public void resetFehlerListe() {
        fehlerListe.clear();
    }

    public static synchronized void versionsMeldungen(String classname) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        MS_Log.systemMeldung("");
        MS_Log.systemMeldung("");
        MS_Log.systemMeldung("###########################################################");
        MS_Log.systemMeldung("###########################################################");
        MS_Log.systemMeldung("Programmstart: " + sdf.format(startZeit));
        MS_Log.systemMeldung("###########################################################");
        MS_Log.systemMeldung("###########################################################");
        long totalMem = Runtime.getRuntime().totalMemory();
        MS_Log.systemMeldung("totalMemory: " + totalMem / (1024L * 1024L) + " MB");
        long maxMem = Runtime.getRuntime().maxMemory();
        MS_Log.systemMeldung("maxMemory: " + maxMem / (1024L * 1024L) + " MB");
        long freeMem = Runtime.getRuntime().freeMemory();
        MS_Log.systemMeldung("freeMemory: " + freeMem / (1024L * 1024L) + " MB");
        MS_Log.systemMeldung("###########################################################");
        //Version
        MS_Log.systemMeldung(Funktionen.getProgVersionString());
        MS_Log.systemMeldung("Klassenname: " + classname);
        MS_Log.systemMeldung("###########################################################");
    }

    public static synchronized void startMeldungen(String classname) {
        versionsMeldungen(classname);
        MS_Log.systemMeldung("Programmpfad: " + MS_Funktionen.getPathJar());
        MS_Log.systemMeldung("Verzeichnis Einstellungen: " + MS_Daten.getBasisVerzeichnis());
        MS_Log.systemMeldung("Useragent: " + MS_Daten.getUserAgent());
        MS_Log.systemMeldung("###########################################################");
        MS_Log.systemMeldung("");
        MS_Log.systemMeldung("");
    }

    public static synchronized void debugMeldung(String text) {
        if (MS_Daten.debug) {
            debugmeldung(text);
        }
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, String klasse, String text) {
        fehlermeldung_(fehlerNummer, klasse, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, String klasse, String text, Exception ex) {
        String[] f = new String[2];
        f[0] = text;
        f[1] = ex.getMessage();
        fehlermeldung_(fehlerNummer, klasse, f);
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, String klasse, String[] text) {
        fehlermeldung_(fehlerNummer, klasse, text);
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, String klasse, String[] text, Exception ex) {
        String[] f = new String[text.length + 1];
        for (int i = 0; i < text.length; ++i) {
            f[i] = text[i];
        }
        f[text.length] = ex.getMessage();
        fehlermeldung_(fehlerNummer, klasse, f);
    }

    public static synchronized void systemMeldung(String text) {
        systemmeldung(new String[]{text});
    }

    public static synchronized void systemMeldung(String[] text) {
        systemmeldung(text);
    }

    public static void printEndeMeldung() {
        if (fehlerListe.size() == 0) {
            systemMeldung("###########################################################");
            systemMeldung(" Keine Fehler :)");
            systemMeldung("###########################################################");
        } else {
            // Fehler ausgeben
            int i_1;
            int i_2;
            for (int i = 1; i < fehlerListe.size(); ++i) {
                for (int k = i; k > 0; --k) {
                    i_1 = fehlerListe.get(k - 1)[1];
                    i_2 = fehlerListe.get(k)[1];
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (i_1 < i_2) {
                        fehlerListe.add(k - 1, fehlerListe.remove(k));
                    } else {
                        break;
                    }
                }
            }
            systemMeldung("###########################################################");
            Iterator<Integer[]> it = fehlerListe.iterator();
            while (it.hasNext()) {
                Integer[] integers = it.next();
                if (integers[0] < 0) {
                    systemMeldung(" Fehlernummer: " + integers[0] + " Anzahl: " + integers[1]);
                } else {
                    systemMeldung(" Fehlernummer:  " + integers[0] + " Anzahl: " + integers[1]);
                }
            }
            systemMeldung("###########################################################");
        }
        // Laufzeit ausgeben
        stopZeit = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        int minuten;
        try {
            minuten = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000 * 60));
        } catch (Exception ex) {
            minuten = -1;
        }
        systemMeldung("");
        systemMeldung("");
        systemMeldung("###########################################################");
        systemMeldung("   --> Beginn: " + sdf.format(startZeit));
        systemMeldung("   --> Fertig: " + sdf.format(stopZeit));
        systemMeldung("   --> Dauer[Min]: " + (minuten == 0 ? "<1" : minuten));
        systemMeldung("###########################################################");
        systemMeldung("");
        systemMeldung("   und Tschuess");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("###########################################################");
    }

    private static void addFehlerNummer(int nr) {
        Iterator<Integer[]> it = fehlerListe.iterator();
        while (it.hasNext()) {
            Integer[] i = it.next();
            if (i[0].intValue() == nr) {
                i[1]++;
                return;
            }
        }
        // dann gibts die Nummer noch nicht
        fehlerListe.add(new Integer[]{new Integer(nr), new Integer(1)});
    }

    private static void fehlermeldung_(int fehlerNummer, String klasse, String[] texte) {
        addFehlerNummer(fehlerNummer);
        final String FEHLER = "Fehler: ";
        final String z = "*";
        System.out.println(z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z);
        System.out.println(z + " Fehlernr: " + fehlerNummer);
        System.out.println(z + " " + FEHLER + klasse);
        for (int i = 0; i < texte.length; ++i) {
            System.out.println(z + "           " + texte[i]);
        }
        System.out.println(z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z);
    }

    private static void debugmeldung(String texte) {
        System.out.println("|||| " + texte);
    }

    private static void systemmeldung(String[] texte) {
        final String z = ". ";
        if (texte.length <= 1) {
            System.out.println(z + " " + texte[0]);
        } else {
            String zeile = "---------------------------------------";
            String txt;
            System.out.println(z + zeile);
            for (int i = 0; i < texte.length; ++i) {
                txt = "| " + texte[i];
                System.out.println(z + txt);
            }
            System.out.println(z + zeile);
        }
    }
}
