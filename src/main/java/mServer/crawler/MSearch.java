/*
 *  MediathekView
 *  Copyright (C) 2013 W. Xaver
 *  W.Xaver[at]googlemail.com
 *  http://zdfmediathk.sourceforge.net/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler;

import mSearch.Config;
import mSearch.daten.ListeFilme;
import mSearch.filmeSuchen.ListenerFilmeLaden;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import mSearch.filmlisten.FilmlisteLesen;
import mSearch.filmlisten.WriteFilmlistJson;
import mSearch.tool.Log;

public class MSearch implements Runnable {

    private ListeFilme listeFilme = new ListeFilme();
    private final FilmeSuchen msFilmeSuchen;
    private boolean serverLaufen = false;

    public MSearch() {
        msFilmeSuchen = new FilmeSuchen();
    }

    @Override
    public synchronized void run() {
        // für den MServer
        serverLaufen = true;
        Config.setStop(false);//damits vom letzten mal stoppen nicht mehr gesetzt ist, falls es einen harten Abbruch gab
        if (crawlerConfig.dirFilme.isEmpty()) {
            Log.sysLog("Kein Pfad der Filmlisten angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        crawlerTool.startMsg();
        Log.sysLog("");
        Log.sysLog("");
        msFilmeSuchen.addAdListener(new ListenerFilmeLaden() {
            @Override
            public void fertig(ListenerFilmeLadenEvent event) {
                serverLaufen = false;
            }
        });
        // alte Filmliste laden
        new FilmlisteLesen().readFilmListe(crawlerTool.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme, 0 /*all days*/);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (crawlerConfig.nurSenderLaden == null) {
            // alle Sender laden
            msFilmeSuchen.filmeBeimSenderLaden(listeFilme);
        } else {
            // dann soll nur ein Sender geladen werden
            msFilmeSuchen.updateSender(crawlerConfig.nurSenderLaden, listeFilme);
        }
        try {
            while (serverLaufen) {
                this.wait(5000);
            }
        } catch (Exception ex) {
            Log.errorLog(496378742, "run()");
        }
        undTschuess();
    }

    public void stop() {
        if (serverLaufen) {
            // nur dann wird noch gesucht
            Config.setStop();
        }
    }

    public ListeFilme getListeFilme() {
        return listeFilme;
    }

    private void importLive(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        Log.sysLog("Live-Streams importieren von: " + importUrl);
        tmpListe.clear();
        new FilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        Log.sysLog("--> von  Anz. Filme: " + listeFilme.size());
        listeFilme.addLive(tmpListe);
        Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void importUrl(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        Log.sysLog("Filmliste importieren von: " + importUrl);
        tmpListe.clear();
        new FilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        Log.sysLog("--> von  Anz. Filme: " + listeFilme.size());
        listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */, false /*ersetzen*/);
        Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void importOld(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        Log.sysLog("Alte Filmliste importieren von: " + importUrl);
        tmpListe.clear();
        new FilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        Log.sysLog("--> von  Anz. Filme: " + listeFilme.size());
        int anz = listeFilme.updateListeOld(tmpListe);
        Log.sysLog("    gefunden: " + anz);
        Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void undTschuess() {
        Config.setStop(false); // zurücksetzen!! sonst klappt das Lesen der Importlisten nicht!!!!!
        listeFilme = msFilmeSuchen.listeFilmeNeu;
        ListeFilme tmpListe = new ListeFilme();

        //================================================
        // noch anere Listen importieren
        Log.sysLog("");
        if (!crawlerConfig.importLive.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Live-Streams importieren");
            importLive(tmpListe, crawlerConfig.importLive);
            Log.sysLog("");
        }
        if (!crawlerConfig.importUrl_1__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste Import 1");
            importUrl(tmpListe, crawlerConfig.importUrl_1__anhaengen);
            Log.sysLog("");
        }
        if (!crawlerConfig.importUrl_2__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste Import 2");
            importUrl(tmpListe, crawlerConfig.importUrl_2__anhaengen);
            Log.sysLog("");
        }
        if (!crawlerConfig.importAkt.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            // noch prüfen ob Filmliste von heute!
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste Import akt");
            importUrl(tmpListe, crawlerConfig.importAkt);
            Log.sysLog("");
        }
        if (!crawlerConfig.importOld.isEmpty() && crawlerTool.loadLongMax()) {
            // wenn angeben, dann Filme die noch "gehen" aus einer alten Liste anhängen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste OLD importieren");
            importOld(tmpListe, crawlerConfig.importOld);
            Log.sysLog("");
        }

        //================================================
        // Filmliste schreiben, normal, xz komprimiert
        Log.sysLog("");
        Log.sysLog("");
        Log.sysLog("============================================================================");
        Log.sysLog("============================================================================");
        Log.sysLog("Filmeliste fertig: " + listeFilme.size() + " Filme");
        Log.sysLog("============================================================================");
        Log.sysLog("");
        Log.sysLog("   --> und schreiben:");
        new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_akt(true /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_akt_xz(), listeFilme);

        //================================================
        // Org
        Log.sysLog("");
        if (crawlerConfig.orgFilmlisteErstellen) {
            // org-Liste anlegen, typ. erste Liste am Tag
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Org-Lilste schreiben: " + crawlerTool.getPathFilmlist_json_org());
            new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_org(), listeFilme);
            new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_org_xz(), listeFilme);
        }

        //====================================================
        // noch das diff erzeugen
        String org = crawlerConfig.orgFilmliste.isEmpty() ? crawlerTool.getPathFilmlist_json_org() : crawlerConfig.orgFilmliste;
        Log.sysLog("");
        Log.sysLog("============================================================================");
        Log.sysLog("Diff erzeugen, von: " + org + " nach: " + crawlerTool.getPathFilmlist_json_diff());
        tmpListe.clear();
        ListeFilme diff;
        new FilmlisteLesen().readFilmListe(org, tmpListe, 0 /*all days*/);
        if (tmpListe.isEmpty()) {
            // dann ist die komplette Liste das diff
            Log.sysLog("   --> Lesefehler der Orgliste: Diff bleibt leer!");
            diff = new ListeFilme();
        } else if (tmpListe.isOlderThan(24 * 60 * 60)) {
            // älter als ein Tag, dann stimmt was nicht!
            Log.sysLog("   --> Orgliste zu alt: Diff bleibt leer!");
            diff = new ListeFilme();
        } else {
            // nur dann macht die Arbeit sinn
            diff = listeFilme.neueFilme(tmpListe);
        }
        Log.sysLog("   --> und schreiben:");
        new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_diff(), diff);
        new WriteFilmlistJson().filmlisteSchreibenJson(crawlerTool.getPathFilmlist_json_diff_xz(), diff);
        Log.sysLog("   --> Anz. Filme Diff: " + diff.size());

        //================================================
        // fertig
        Log.endMsg();
    }

}
