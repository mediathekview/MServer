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
import mServer.tool.UrlService;

public class Crawler implements Runnable {

    private ListeFilme listeFilme = new ListeFilme();
    private final FilmeSuchen filmeSuchen;
    private boolean serverLaufen = false;

    public Crawler() {
        filmeSuchen = new FilmeSuchen();
    }

    @Override
    public synchronized void run() {
        // für den MServer
        serverLaufen = true;
        Config.setStop(false);//damits vom letzten mal stoppen nicht mehr gesetzt ist, falls es einen harten Abbruch gab
        if (CrawlerConfig.dirFilme.isEmpty()) {
            Log.sysLog("Kein Pfad der Filmlisten angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        CrawlerTool.startMsg();
        Log.sysLog("");
        Log.sysLog("");
        filmeSuchen.addAdListener(new ListenerFilmeLaden() {
            @Override
            public void fertig(ListenerFilmeLadenEvent event) {
                serverLaufen = false;
            }
        });
        // alte Filmliste laden
        new FilmlisteLesen().readFilmListe(CrawlerTool.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme, 0 /*all days*/);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (CrawlerConfig.nurSenderLaden == null) {
            // alle Sender laden
            filmeSuchen.filmeBeimSenderLaden(listeFilme);
        } else {
            // dann soll nur ein Sender geladen werden
            filmeSuchen.updateSender(CrawlerConfig.nurSenderLaden, listeFilme);
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
        //listeFilme.addLive(tmpListe);
        new AddToFilmlist(listeFilme, tmpListe, new UrlService()).addLiveStream();
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
        //int anz = listeFilme.updateListeOld(tmpListe);
        int anz = new AddToFilmlist(listeFilme, tmpListe, new UrlService()).addOldList();
        Log.sysLog("    gefunden: " + anz);
        Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void undTschuess() {
        Config.setStop(false); // zurücksetzen!! sonst klappt das Lesen der Importlisten nicht!!!!!
        listeFilme = filmeSuchen.listeFilmeNeu;
        ListeFilme tmpListe = new ListeFilme();

        //================================================
        // noch anere Listen importieren
        Log.sysLog("");
        if (!CrawlerConfig.importLive.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Live-Streams importieren");
            importLive(tmpListe, CrawlerConfig.importLive);
            Log.sysLog("");
        }
        if (!CrawlerConfig.importUrl_1__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste Import 1");
            importUrl(tmpListe, CrawlerConfig.importUrl_1__anhaengen);
            Log.sysLog("");
        }
        if (!CrawlerConfig.importUrl_2__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste Import 2");
            importUrl(tmpListe, CrawlerConfig.importUrl_2__anhaengen);
            Log.sysLog("");
        }
        if (!CrawlerConfig.importAkt.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            // noch prüfen ob Filmliste von heute!
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste Import akt");
            importUrl(tmpListe, CrawlerConfig.importAkt);
            Log.sysLog("");
        }
        if (!CrawlerConfig.importOld.isEmpty() && CrawlerTool.loadLongMax()) {
            // wenn angeben, dann Filme die noch "gehen" aus einer alten Liste anhängen
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Filmliste OLD importieren");
            importOld(tmpListe, CrawlerConfig.importOld);
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
        new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_akt(true /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_akt_xz(), listeFilme);

        //================================================
        // Org
        Log.sysLog("");
        if (CrawlerConfig.orgFilmlisteErstellen) {
            // org-Liste anlegen, typ. erste Liste am Tag
            Log.sysLog("");
            Log.sysLog("============================================================================");
            Log.sysLog("Org-Lilste schreiben: " + CrawlerTool.getPathFilmlist_json_org());
            new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_org(), listeFilme);
            new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_org_xz(), listeFilme);
        }

        //====================================================
        // noch das diff erzeugen
        String org = CrawlerConfig.orgFilmliste.isEmpty() ? CrawlerTool.getPathFilmlist_json_org() : CrawlerConfig.orgFilmliste;
        Log.sysLog("");
        Log.sysLog("============================================================================");
        Log.sysLog("Diff erzeugen, von: " + org + " nach: " + CrawlerTool.getPathFilmlist_json_diff());
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
        new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_diff(), diff);
        new WriteFilmlistJson().filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_diff_xz(), diff);
        Log.sysLog("   --> Anz. Filme Diff: " + diff.size());

        //================================================
        // fertig
        Log.endMsg();
    }

}
