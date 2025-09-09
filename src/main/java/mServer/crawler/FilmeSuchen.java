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
package mServer.crawler;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLaden;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLadenEvent;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.*;
import mServer.crawler.sender.ard.ArdCrawler;
import mServer.crawler.sender.arte.MediathekArte;
import mServer.crawler.sender.dreisat.DreiSatCrawler;
import mServer.crawler.sender.dw.DwCrawler;
import mServer.crawler.sender.kika.KikaApiCrawler;
import mServer.crawler.sender.orfon.OrfOnCrawler;
import mServer.crawler.sender.phoenix.PhoenixCrawler;
import mServer.crawler.sender.sr.SrCrawler;
import mServer.crawler.sender.srf.SrfCrawler;
import mServer.crawler.sender.zdf.ZdfCrawler;
import mServer.tool.MserverDaten;
import mServer.tool.MserverKonstanten;
import mServer.tool.StatsUpload;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.swing.event.EventListenerList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * ###########################################################################################################
 * Ablauf: die gefundenen Filme kommen in die "listeFilme" -> bei einem vollen
 * Suchlauf: passiert nichts weiter -> bei einem Update: "listeFilme" mit alter
 * Filmliste auffüllen, URLs die es schon gibt werden verworfen "listeFilme" ist
 * dann die neue komplette Liste mit Filmen
 * ##########################################################################################################
 */
public class FilmeSuchen {

  public ListeFilme listeFilmeNeu; // neu angelegte Liste und da kommen die neu gesuchten Filme rein
  public ListeFilme listeFilmeAlt; // ist die "alte" Liste, wird beim Aufruf übergeben und enthält am Ende das Ergebnis
  // private
  private final LinkedList<MediathekReader> mediathekListe = new LinkedList<>(); // ist die Liste mit allen MediathekReadern (also allen Sender)
  private final EventListenerList listeners = new EventListenerList();
  public static final ListeRunSender listeSenderLaufen = new ListeRunSender(); // Liste mit Infos über jeden laufeneden MedathekReader
  private Date startZeit = new Date();
  private Date stopZeit = new Date();
  private boolean allStarted = false;
  private final FastDateFormat sdf = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss");

  public FilmeSuchen() {
    // für jeden Sender einen MediathekReader anlegen, mit der Prio ob
    // sofort gestartet oder erst später
    //Reader laden Spaltenweises Laden
    List<String> crawlerList = Arrays.asList(MserverDaten.system[MserverKonstanten.SYSTEM_CRAWLER_LIST_NR].split(","));  
    if (MserverDaten.system[MserverKonstanten.SYSTEM_CRAWLER_LIST_NR].isEmpty()) {
      crawlerList = new ArrayList<>(Arrays.asList("ARD","ZDF","ARTE","DW","KIKA","3SAT","SR","SRF","SRFPOD","ORF","PHONIX"));
    }
    
    if (crawlerList.contains("ARD")) {
      mediathekListe.add(new ArdCrawler(this, 0));
    }
    if (crawlerList.contains("ZDF")) {
      mediathekListe.add(new ZdfCrawler(this, 0));
    }
    if (crawlerList.contains("ARTE")) {
      mediathekListe.add(new MediathekArte(this, 0));
    }
    if (crawlerList.contains("DW")) {
      mediathekListe.add(new DwCrawler(this, 0));
    }
    if (crawlerList.contains("KIKA")) {
      mediathekListe.add(new KikaApiCrawler(this, 0));
    }
    if (crawlerList.contains("3SAT")) {
      mediathekListe.add(new DreiSatCrawler(this, 1));
    }
    if (crawlerList.contains("SR")) {
      mediathekListe.add(new SrCrawler(this, 1));
    }
    if (crawlerList.contains("SRF")) {
      mediathekListe.add(new SrfCrawler(this, 1));
    }
    if (crawlerList.contains("SRFPOD")) {
      mediathekListe.add(new MediathekSrfPod(this, 1));
    }
    if (crawlerList.contains("ORF")) {
      mediathekListe.add(new OrfOnCrawler(this, 1));
    }
    if (crawlerList.contains("PHONIX")) {
      mediathekListe.add(new PhoenixCrawler(this, 1));
    }
  
  }

  public static String[] getNamenSender() {
    // liefert eine Array mit allen Sendernamen
    return Const.SENDER;
  }

  public void addAdListener(ListenerFilmeLaden listener) {
    listeners.add(ListenerFilmeLaden.class, listener);
  }

  /**
   * es werden alle Filme gesucht
   *
   * @param listeFilme
   */
  public synchronized void filmeBeimSenderLaden(ListeFilme listeFilme) {
    initStart(listeFilme);
    // die mReader nach Prio starten
    mrStarten(0);
    if (mediathekListe.stream().filter(mr -> mr.getStartPrio() == 1).count() == 0) {
      allStarted = true;
    } else {
      if (!Config.getStop()) {
        // waren und wenn Suchlauf noch nicht abgebrochen weiter mit dem Rest
        mrWarten(4*60);//4*60);
        mrStarten(1);
        allStarted = true;
      }
    }
  }

  /**
   * es werden nur einige Sender aktualisiert
   *
   * @param nameSender
   * @param listeFilme
   */
  public void updateSender(String[] nameSender, ListeFilme listeFilme) {
    // nur für den Mauskontext "Sender aktualisieren"
    boolean starten = false;
    initStart(listeFilme);
    for (MediathekReader reader : mediathekListe) {
      for (String s : nameSender) {
        if (reader.checkNameSenderFilmliste(s)) {
          starten = true;
          new Thread(reader).start();
          //reader.start();
        }
      }
    }
    allStarted = true;
    if (!starten) {
      // dann fertig
      meldenFertig("");
    }
  }

  public synchronized RunSender melden(String sender, int max, int progress, String text) {
    RunSender runSender = listeSenderLaufen.getSender(sender);
    if (runSender != null) {
      runSender.max = max;
      runSender.progress = progress;
    } else {
      // Sender startet
      runSender = new RunSender(sender, max, progress);
      listeSenderLaufen.add(runSender);
      //wird beim Start des Senders aufgerufen, 1x
      if (listeSenderLaufen.size() <= 1 /* erster Aufruf */) {
        notifyStart(new ListenerFilmeLadenEvent(sender, text, listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
      }
    }
    notifyProgress(new ListenerFilmeLadenEvent(sender, text, listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
    progressBar();
    return runSender;
  }

  public synchronized void meldenFertig(String sender) {
    //wird ausgeführt wenn Sender beendet ist
    String zeile;
    RunSender run = listeSenderLaufen.senderFertig(sender);
    if (run != null) {

      int sekunden = run.getLaufzeitSekunden();
      long anzahlFilme = listeSenderLaufen.get(sender, RunSender.Count.FILME);

      // Statistikexport pro Sender
      StatsUpload.getInstance().catchSenderStat(sender, sekunden, anzahlFilme);

      long anzahlSeiten = listeSenderLaufen.get(run.sender, RunSender.Count.ANZAHL);
      final String rate = listeSenderLaufen.getRate(sender);

      zeile = "" + '\n';
      zeile += "-------------------------------------------------------------------------------------" + '\n';
      zeile += "Fertig " + sender + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " Uhr, Filme: " + anzahlFilme + '\n';
      zeile += "     -> Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60) + '\n';
      zeile += "     ->     [kB/s]: " + rate + '\n';
      zeile += "     ->     Seiten: " + anzahlSeiten + '\n';
      zeile += "     ->       Rest: " + listeSenderLaufen.getSenderRun() + '\n';
      zeile += "-------------------------------------------------------------------------------------" + '\n';
      Log.sysLog(zeile);
    }
    if (!allStarted || !listeSenderLaufen.listeFertig()) {
      //nur ein Sender fertig oder noch nicht alle gestartet
      notifyProgress(new ListenerFilmeLadenEvent(sender, "", listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
    } else {
      // alles fertig
      // wird einmal aufgerufen, wenn alle Sender fertig sind
      Log.progress(""); // zum löschen der Progressbar
      if (Config.getStop()) {
        // Abbruch melden
        Log.sysLog("                                                                                     ");
        Log.sysLog("                                                                                     ");
        Log.sysLog("*************************************************************************************");
        Log.sysLog("*************************************************************************************");
        Log.sysLog("*************************************************************************************");
        Log.sysLog("     ----- Abbruch -----                                                             ");
        Log.sysLog("*************************************************************************************");
        Log.sysLog("*************************************************************************************");
        Log.sysLog("*************************************************************************************");
        Log.sysLog("                                                                                     ");
        Log.sysLog("                                                                                     ");
      }
      mrClear();
      if (CrawlerConfig.updateFilmliste) {
        // alte Filme eintragen wenn angefordert oder nur ein update gesucht wurde
        zeile = "" + '\n';
        zeile += "-------------------------------------------------------------------------------------" + '\n';
        zeile += "Update Filmliste:" + '\n';
        zeile += "     -> Einträge bisher: " + listeFilmeNeu.size() + '\n';
        zeile += "     -> Einträge alte: " + listeFilmeAlt.size() + '\n';

        listeFilmeNeu.updateListe(listeFilmeAlt, true /* über den Index vergleichen */, false /*ersetzen*/);

        zeile += "     -> Einträge danach: " + listeFilmeNeu.size() + '\n';
        zeile += "-------------------------------------------------------------------------------------" + '\n';
        Log.sysLog(zeile);
      }
      listeFilmeNeu.sort();
      // FilmlisteMetaDaten
      stopZeit = new Date(System.currentTimeMillis());
      listeFilmeNeu.writeMetaData();

      endeMeldung().forEach(Log::sysLog);

      notifyFertig(new ListenerFilmeLadenEvent(sender, "", listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), (int) listeSenderLaufen.get(RunSender.Count.FILME), false));
    }
  }

  public ArrayList<String> endeMeldung() {
    // wird einmal aufgerufen, wenn alle Sender fertig sind
    ArrayList<String> retArray = new ArrayList<>();
    // Sender ===============================================
    // ======================================================
    retArray.add("");
    retArray.add("");
    retArray.add("=================================================================================");
    retArray.add("==  Sender  =====================================================================");
    retArray.add("");
    listeSenderLaufen.getTextSum(retArray);
    listeSenderLaufen.getTextCount(retArray);

    // Gesamt ===============================================
    // ======================================================
    int sekunden = getDauerSekunden();

    // Statistikexport Suchlauf Laufzeit
    StatsUpload.getInstance().setData(StatsUpload.Data.CRAWLSTAT_LAUFZEIT, sekunden);
    // Statistikexport Suchlauf neue Filme
    StatsUpload.getInstance().setData(StatsUpload.Data.CRAWLSTAT_FILMENEU, listeSenderLaufen.get(RunSender.Count.FILME));

    retArray.add("");
    retArray.add("=================================================================================");
    retArray.add("=================================================================================");
    retArray.add("");
    retArray.add("       Filme geladen: " + listeSenderLaufen.get(RunSender.Count.FILME));
    retArray.add("      Seiten geladen: " + listeSenderLaufen.get(RunSender.Count.ANZAHL));

    retArray.add("   Summe geladen[MB]: " + RunSender.getStringZaehler(listeSenderLaufen.get(RunSender.Count.SUM_DATA_BYTE)));
    retArray.add("        Traffic [MB]: " + RunSender.getStringZaehler(listeSenderLaufen.get(RunSender.Count.SUM_TRAFFIC_BYTE)));

    // Durchschnittswerte ausgeben
    double doub = (1.0 * listeSenderLaufen.get(RunSender.Count.SUM_TRAFFIC_BYTE)) / (sekunden == 0 ? 1 : sekunden) / 1000;
    String rate = doub < 1 ? "<1" : String.format("%.1f", (doub));
    retArray.add("    ->    Rate[kB/s]: " + rate);
    retArray.add("    ->    Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60));
    retArray.add("           ->  Start: " + sdf.format(startZeit));
    retArray.add("           ->   Ende: " + sdf.format(stopZeit));
    retArray.add("");
    retArray.add("=================================================================================");
    retArray.add("=================================================================================");
    return retArray;
  }

  private synchronized void mrStarten(int prio) {
    // die MediathekReader mit "prio" starten
    mediathekListe.stream().filter(mr -> mr.getStartPrio() == prio).forEach(mr -> new Thread(mr).start());
  }

  private synchronized void mrClear() {
    //die MediathekReader aufräumen
    mediathekListe.forEach(MediathekReader::clear);
  }

  private synchronized void mrWarten(int seconds) {
    // 4 Minuten warten, alle 10 Sekunden auf STOP prüfen
    try {
      for (int i = 0; i < seconds; ++i) {
        if (Config.getStop()) {
          break;
        }
        this.wait(1000); // warten, Sender nach der Gesamtlaufzeit starten
      }
    } catch (Exception ex) {
      Log.errorLog(978754213, ex);
    }
  }

  private int getDauerSekunden() {
    int sekunden;
    try {
      sekunden = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000));
    } catch (Exception ex) {
      sekunden = 1;
    }
    if (sekunden <= 0) {
      sekunden = 1;
    }
    return sekunden;
  }

  private void initStart(ListeFilme listeFilme) {
    // das Absuchen der Sender vorbereiten
    listeSenderLaufen.clear();
    allStarted = false;
    listeFilmeAlt = listeFilme;
    Config.setStop(false);
    startZeit = new Date(System.currentTimeMillis());
    listeFilmeNeu = new ListeFilme();
//        listeFilmeNeu.liveStreamEintragen();
    Log.sysLog("");
    Log.sysLog("=======================================");
    Log.sysLog("Start Filme laden:");
    if (CrawlerTool.loadMax()) {
      Log.sysLog("Filme laden: max");
    } else if (CrawlerTool.loadLongMax()) {
      Log.sysLog("Filme laden: long");
    } else {
      Log.sysLog("Filme laden: short");
    }
    if (CrawlerConfig.updateFilmliste) {
      Log.sysLog("Filmliste: aktualisieren");
    } else {
      Log.sysLog("Filmliste: neue erstellen");
    }
    Log.sysLog("=======================================");
    Log.sysLog("");
  }

  private void progressBar() {
    int max = listeSenderLaufen.getMax();
    int progress = listeSenderLaufen.getProgress();
    int proz = 0;
    String text;
    int sekunden = 0;
    try {
      sekunden = Math.round((new Date(System.currentTimeMillis()).getTime() - startZeit.getTime()) / (1000));
    } catch (Exception ignored) {
    }

    if (max != 0) {
      if (progress != 0) {
        proz = progress * 100 / max;
      }
      if (max > 0 && proz == 100) {
        proz = 99;
      }
      text = "  [ ";

      final int a = proz / 10;
      for (int i = 0; i < a; ++i) {
        text += "#";
      }
      for (int i = 0; i < (10 - a); ++i) {
        text += "-";
      }
      text += " ]  " + listeSenderLaufen.get(RunSender.Count.ANZAHL) + " Seiten / "
              + proz + "% von " + max + " Themen / Filme: " + listeSenderLaufen.get(RunSender.Count.FILME)
              + " / Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60)
              + " / R-Sender: " + listeSenderLaufen.getAnzSenderRun();
      Log.progress(text);
    }
  }

  private void notifyStart(ListenerFilmeLadenEvent event) {
    for (Object l : listeners.getListenerList()) {
      if (l instanceof ListenerFilmeLaden) {
        ((ListenerFilmeLaden) l).start(event);
      }
    }
  }

  private void notifyProgress(ListenerFilmeLadenEvent event) {
    for (Object l : listeners.getListenerList()) {
      if (l instanceof ListenerFilmeLaden) {
        ((ListenerFilmeLaden) l).progress(event);
      }
    }
  }

  private void notifyFertig(ListenerFilmeLadenEvent event) {
    for (Object l : listeners.getListenerList()) {
      if (l instanceof ListenerFilmeLaden) {
        ((ListenerFilmeLaden) l).fertig(event);
      }
    }
  }
}
