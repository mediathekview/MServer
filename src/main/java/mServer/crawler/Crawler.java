/*
 * MediathekView Copyright (C) 2013 W. Xaver W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler;

import java.util.concurrent.TimeUnit;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLaden;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLadenEvent;
import de.mediathekview.mlib.filmlisten.FilmlisteLesen;
import de.mediathekview.mlib.tool.Log;

public class Crawler implements Runnable {

  private ListeFilme listeFilme = new ListeFilme();
  private final FilmeSuchen filmeSuchen;
  private boolean serverLaufen = false;

  public Crawler() {
    filmeSuchen = new FilmeSuchen();
  }

  public ListeFilme getListeFilme() {
    return listeFilme;
  }

  @Override
  public synchronized void run() {
    // für den MServer
    serverLaufen = true;
    Config.setStop(false);// damits vom letzten mal stoppen nicht mehr gesetzt ist, falls es einen
                          // harten Abbruch gab
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
      public void fertig(final ListenerFilmeLadenEvent event) {
        serverLaufen = false;
      }
    });
    // alte Filmliste laden
    listeFilme = new FilmlisteLesen()
        .readFilmListe(CrawlerTool.getPathFilmlist_json_akt(false /* aktDate */), 0 /* all days */);
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
        TimeUnit.SECONDS.timedWait(this, 5);
      }
    } catch (final Exception ex) {
      Log.errorLog(496378742, "run()");
    }
    undTschuess();
  }

  public void stop() {
    if (serverLaufen) {
      // nur dann wird noch gesucht
      Config.setStop(true);
    }
  }

  private void importLive(final String importUrl) {
    // ================================================
    // noch anere Listen importieren
    Log.sysLog("Live-Streams importieren von: " + importUrl);
    final ListeFilme tmpListe = new FilmlisteLesen().readFilmListe(importUrl, 0 /* all days */);
    Log.sysLog("--> von  Anz. Filme: " + listeFilme.size());
    // listeFilme.addLive(tmpListe);
    new AddToFilmlist(listeFilme, tmpListe).addLiveStream();
    Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
    System.gc();
    listeFilme.sort();
  }

  private void importOld(final String importUrl) {
    // ================================================
    // noch anere Listen importieren
    Log.sysLog("Alte Filmliste importieren von: " + importUrl);
    final ListeFilme tmpListe = new FilmlisteLesen().readFilmListe(importUrl, 0 /* all days */);
    Log.sysLog("--> von  Anz. Filme: " + listeFilme.size());
    // int anz = listeFilme.updateListeOld(tmpListe);
    final int anz = new AddToFilmlist(listeFilme, tmpListe).addOldList();
    Log.sysLog("    gefunden: " + anz);
    Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
    System.gc();
    listeFilme.sort();
  }

  private void importUrl(final String importUrl) {
    // ================================================
    // noch anere Listen importieren
    Log.sysLog("Filmliste importieren von: " + importUrl);
    final ListeFilme tmpListe = new FilmlisteLesen().readFilmListe(importUrl, 0 /* all days */);
    Log.sysLog("--> von  Anz. Filme: " + listeFilme.size());
    listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */, false /* ersetzen */);
    Log.sysLog("--> nach Anz. Filme: " + listeFilme.size());
    System.gc();
    listeFilme.sort();
  }

  private void undTschuess() {
    Config.setStop(false); // zurücksetzen!! sonst klappt das Lesen der Importlisten nicht!!!!!
    listeFilme = filmeSuchen.listeFilmeNeu;

    // ================================================
    // noch anere Listen importieren
    Log.sysLog("");
    if (!CrawlerConfig.importLive.isEmpty()) {
      // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
      Log.sysLog("");
      Log.sysLog("============================================================================");
      Log.sysLog("Live-Streams importieren");
      importLive(CrawlerConfig.importLive);
      Log.sysLog("");
    }
    if (!CrawlerConfig.importUrl_1__anhaengen.isEmpty()) {
      // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
      Log.sysLog("");
      Log.sysLog("============================================================================");
      Log.sysLog("Filmliste Import 1");
      importUrl(CrawlerConfig.importUrl_1__anhaengen);
      Log.sysLog("");
    }
    if (!CrawlerConfig.importUrl_2__anhaengen.isEmpty()) {
      // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
      Log.sysLog("");
      Log.sysLog("============================================================================");
      Log.sysLog("Filmliste Import 2");
      importUrl(CrawlerConfig.importUrl_2__anhaengen);
      Log.sysLog("");
    }
    if (!CrawlerConfig.importAkt.isEmpty()) {
      // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
      // noch prüfen ob Filmliste von heute!
      Log.sysLog("");
      Log.sysLog("============================================================================");
      Log.sysLog("Filmliste Import akt");
      importUrl(CrawlerConfig.importAkt);
      Log.sysLog("");
    }
    if (!CrawlerConfig.importOld.isEmpty() && CrawlerTool.loadLongMax()) {
      // wenn angeben, dann Filme die noch "gehen" aus einer alten Liste anhängen
      Log.sysLog("");
      Log.sysLog("============================================================================");
      Log.sysLog("Filmliste OLD importieren");
      importOld(CrawlerConfig.importOld);
      Log.sysLog("");
    }

    // ================================================
    // Filmliste schreiben, normal, xz komprimiert
    Log.sysLog("");
    Log.sysLog("");
    Log.sysLog("============================================================================");
    Log.sysLog("============================================================================");
    Log.sysLog("Filmeliste fertig: " + listeFilme.size() + " Filme");
    Log.sysLog("============================================================================");
    Log.sysLog("");
    Log.sysLog("   --> und schreiben:");

    // final WriteFilmlistJson writer = null;
    // writer.filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_akt(false /* aktDate */),
    // listeFilme);
    // writer.filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_akt(true /* aktDate */),
    // listeFilme);
    // writer.filmlisteSchreibenJsonCompressed(CrawlerTool.getPathFilmlist_json_akt_xz(),
    // listeFilme);
    // ================================================
    // Org
    Log.sysLog("");
    if (CrawlerConfig.orgFilmlisteErstellen) {
      // org-Liste anlegen, typ. erste Liste am Tag
      Log.sysLog("");
      Log.sysLog("============================================================================");
      Log.sysLog("Org-Lilste schreiben: " + CrawlerTool.getPathFilmlist_json_org());
      // writer.filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_org(), listeFilme);
      // writer.filmlisteSchreibenJsonCompressed(CrawlerTool.getPathFilmlist_json_org_xz(),
      // listeFilme);
    }

    // ====================================================
    // noch das diff erzeugen
    final String org = CrawlerConfig.orgFilmliste.isEmpty() ? CrawlerTool.getPathFilmlist_json_org()
        : CrawlerConfig.orgFilmliste;
    Log.sysLog("");
    Log.sysLog("============================================================================");
    Log.sysLog("Diff erzeugen, von: " + org + " nach: " + CrawlerTool.getPathFilmlist_json_diff());
    ListeFilme diff;
    final ListeFilme tmpListe = new FilmlisteLesen().readFilmListe(org, 0 /* all days */);
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
    // writer.filmlisteSchreibenJson(CrawlerTool.getPathFilmlist_json_diff(), diff);
    // writer.filmlisteSchreibenJsonCompressed(CrawlerTool.getPathFilmlist_json_diff_xz(), diff);
    Log.sysLog("   --> Anz. Filme Diff: " + diff.size());

    // ================================================
    // fertig
    Log.endMsg();
  }


}
