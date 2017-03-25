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
package mServer.tool;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.lang3.time.FastDateFormat;

import de.mediathekview.mlib.tool.Functions;

public class MserverLog {

    private static final LinkedList<Integer[]> fehlerListe = new LinkedList<>(); // [Fehlernummer, Anzahl]
    private static final Date startZeit = new Date(System.currentTimeMillis());
    private static final String logfile = MserverDaten.getLogDatei(MserverKonstanten.LOG_FILE_NAME);

    public static synchronized void versionsMeldungen(String classname) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemmeldung(new String[]{".___  ___.  _______  _______   __       ___   .___________. __    __   _______  __  ___"}, false);
        systemmeldung(new String[]{"|   \\/   | |   ____||       \\ |  |     /   \\  |           ||  |  |  | |   ____||  |/  /"}, false);
        systemmeldung(new String[]{"|  \\  /  | |  |__   |  .--.  ||  |    /  ^  \\ `---|  |----`|  |__|  | |  |__   |  '  /"}, false);
        systemmeldung(new String[]{"|  |\\/|  | |   __|  |  |  |  ||  |   /  /_\\  \\    |  |     |   __   | |   __|  |    <"}, false);
        systemmeldung(new String[]{"|  |  |  | |  |____ |  '--'  ||  |  /  _____  \\   |  |     |  |  |  | |  |____ |  .  \\"}, false);
        systemmeldung(new String[]{"|__|  |__| |_______||_______/ |__| /__/     \\__\\  |__|     |__|  |__| |_______||__|\\__\\"}, false);
        systemMeldung("");
        systemMeldung("###########################################################");
        systemMeldung("###########################################################");
        systemMeldung("Programmstart: " + sdf.format(startZeit));
        systemMeldung("###########################################################");
        long totalMem = Runtime.getRuntime().totalMemory();
        systemMeldung("totalMemory: " + totalMem / (1024L * 1024L) + " MB");
        long maxMem = Runtime.getRuntime().maxMemory();
        systemMeldung("maxMemory: " + maxMem / (1024L * 1024L) + " MB");
        long freeMem = Runtime.getRuntime().freeMemory();
        systemMeldung("freeMemory: " + freeMem / (1024L * 1024L) + " MB");
        systemMeldung("###########################################################");
        //Version
        systemMeldung(MserverKonstanten.PROGRAMMNAME + Functions.getProgVersionString());
        systemMeldung("Compiled: " + Functions.getCompileDate());
        systemMeldung("Klassenname: " + classname);
        systemMeldung("###########################################################");
    }

    public static synchronized void startMeldungen(String classname) {
        versionsMeldungen(classname);
        systemMeldung("Programmpfad: " + Functions.getPathJar());
        systemMeldung("Verzeichnis Einstellungen: " + MserverDaten.getBasisVerzeichnis());
        systemMeldung("Useragent: " + MserverDaten.getUserAgent());
        systemMeldung("###########################################################");
        if (MserverDaten.debug) {
            MserverLog.systemMeldung("");
            MserverLog.systemMeldung("== Debug on ======");
            MserverLog.systemMeldung("");
        }
        systemMeldung("");
        systemMeldung("");
    }

    public static synchronized void debugMeldung(String text) {
        if (MserverDaten.debug) {
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
        System.arraycopy(text, 0, f, 0, text.length);
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
        if (fehlerListe.isEmpty()) {
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
            for (Integer[] integers : fehlerListe) {
                if (integers[0] < 0) {
                    systemMeldung(" Fehlernummer: " + integers[0] + " Anzahl: " + integers[1]);
                } else {
                    systemMeldung(" Fehlernummer:  " + integers[0] + " Anzahl: " + integers[1]);
                }
            }
            systemMeldung("###########################################################");
        }
        // Laufzeit ausgeben
        final Date stopZeit = new Date(System.currentTimeMillis());
        final FastDateFormat sdf = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss");
        final int minuten = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000 * 60));

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
        for (Integer[] i : fehlerListe) {
            if (i[0] == nr) {
                i[1]++;
                return;
            }
        }
        // dann gibts die Nummer noch nicht
        fehlerListe.add(new Integer[]{nr, 1});
    }

    private static void fehlermeldung_(int fehlerNummer, String klasse, String[] texte) {
        addFehlerNummer(fehlerNummer);
        final String FEHLER = "Fehler(" + MserverKonstanten.PROGRAMMNAME + "): ";
        final String z = "*";
        System.out.println(z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z);
        System.out.println(z + " Fehlernr: " + fehlerNummer);
        System.out.println(z + ' ' + FEHLER + klasse);
        for (String aTexte : texte) {
            System.out.println(z + "           " + aTexte);
        }
        System.out.println(z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z + z);
    }

    private static void debugmeldung(String texte) {
        System.out.println("|||| " + texte);
    }

    private static void systemmeldung(String[] texte) {
        systemmeldung(texte, true);
    }

    private static void systemmeldung(String[] texte, boolean datum) {
        final String z = "o ";
        if (texte.length <= 1) {
            System.out.println(z + ' ' + texte[0]);
        } else {
            String zeile = "---------------------------------------";
            String txt;
            System.out.println(z + zeile);
            for (String aTexte : texte) {
                txt = "| " + aTexte;
                System.out.println(z + txt);
            }
            System.out.println(z + zeile);
        }
        // ins Logfile eintragen
        if (!logfile.isEmpty()) {
            try (FileOutputStream fos = new FileOutputStream(logfile, true);
                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {
                for (String s : texte) {
                    if (s.isEmpty()) {
                        writer.write("\n"); // nur leere Zeile schrieben
                    } else {
                        if (datum) {
                            writer.write(MserverDatumZeit.getJetzt() + "     " + s);
                        } else {
                            writer.write(s);
                        }
                        writer.write("\n");
                    }
                }
                writer.close();
            } catch (Exception ex) {
                System.out.println("Fehler beim Logfile schreiben: " + ex.getMessage());
            }
        }
    }

    public void resetFehlerListe() {
        fehlerListe.clear();
    }
}
