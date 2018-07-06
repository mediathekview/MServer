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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;

public class MediathekMdr extends MediathekReader {

  public final static String SENDERNAME = Const.MDR;
  private final LinkedList<String> listeTage = new LinkedList<>();
  private final LinkedList<String[]> listeGesucht = new LinkedList<>(); //thema,titel,datum,zeit

  /**
   *
   * @param ssearch
   * @param startPrio
   */
  public MediathekMdr(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, /* threads */ 3, /* urlWarten */ 200, startPrio);
  }

  /**
   *
   */
  @Override
  public void addToList() {
    // <a href="/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-A_numberofelements-1_zc-ef89b6fa.html#letternavi

    final String URL_SENDUNGEN = "http://www.mdr.de/mediathek/fernsehen/a-z/index.html";
    final String MUSTER = "<a href=\"/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter";
    final String MUSTER_ADD = "http://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter";

    final String URL_TAGE = "http://www.mdr.de/mediathek/fernsehen/index.html";
    final String MUSTER_TAGE = "<a href=\"/mediathek/fernsehen/sendung-verpasst--100_date-";
    final String MUSTER_ADD_TAGE = "http://www.mdr.de/mediathek/fernsehen/sendung-verpasst--100_date-";

    MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    listeThemen.clear();
    listeTage.clear();
    listeGesucht.clear();
    meldungStart();
    GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
    seite = getUrlIo.getUri_Utf(SENDERNAME, URL_SENDUNGEN, seite, "");
    int pos = 0;
    int pos1;
    int pos2;
    String url = "";
    while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
      pos += MUSTER.length();
      pos1 = pos;
      pos2 = seite.indexOf("\"", pos);
      if (pos1 != -1 && pos2 != -1) {
        url = seite.substring(pos1, pos2);
      }
      if (url.isEmpty()) {
        Log.errorLog(889216307, "keine URL");
      } else {
        url = MUSTER_ADD + url;
        if (url.contains("#")) {
          url = url.substring(0, url.indexOf('#'));
        }
        listeThemen.addUrl(new String[]{url});
      }
    }

    seite = getUrlIo.getUri_Utf(SENDERNAME, URL_TAGE, seite, "");
    pos = 0;
    url = "";
    while ((pos = seite.indexOf(MUSTER_TAGE, pos)) != -1) {
      pos += MUSTER_TAGE.length();
      pos1 = pos;
      pos2 = seite.indexOf("\"", pos);
      if (pos1 != -1 && pos2 != -1) {
        url = seite.substring(pos1, pos2);
      }
      if (url.isEmpty()) {
        Log.errorLog(461225808, "keine URL");
      } else {
        url = MUSTER_ADD_TAGE + url;
        //FIXME
        assert (istInListe(listeTage, url) == (listeTage.contains(url)));
        if (!istInListe(listeTage, url)) {
          listeTage.add(url);
        }
      }
    }
    if (Config.getStop()) {
      meldungThreadUndFertig();
    } else if (listeThemen.isEmpty() && listeTage.isEmpty()) {
      meldungThreadUndFertig();
    } else {
      meldungAddMax(listeThemen.size() + listeTage.size());
      listeSort(listeThemen, 0);
      for (int t = 0; t < getMaxThreadLaufen(); ++t) {
        //new Thread(new ThemaLaden()).start();
        Thread th = new ThemaLaden();
        th.setName(SENDERNAME + t);
        th.start();
      }
    }
  }

  @Override
  public void clear() {
    listeThemen.clear();
    listeTage.clear();
    listeGesucht.clear();
  }

  private class ThemaLaden extends Thread {

    private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
    private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seiteTage = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite4 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

    @Override
    public void run() {
      try {
        meldungAddThread();
        String[] link;
        while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
          meldungProgress(link[0]);
          addThema(link[0]);
        }
        String url;
        while (!Config.getStop() && (url = getListeTage()) != null) {
          meldungProgress(url);
          addTage(url);
        }
      } catch (Exception ex) {
        Log.errorLog(115896304, ex);
      }
      meldungThreadUndFertig();
    }

    private void addThema(String strUrlFeed) {
      final String MUSTER = "<div class=\"media mediaA \">";

      int pos = 0;
      String thema, url = "";
      try {
        seite1 = getUrl.getUri(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 2 /* versuche */, seite1, "");
        while (!Config.getStop() && (pos = seite1.indexOf(MUSTER, pos)) != -1) {
          pos += MUSTER.length();
          url = seite1.extract("<a href=\"", "\"", pos);
          thema = seite1.extract(" class=\"headline\" title=\"\">", "<", pos);
          if (url.isEmpty()) {
            Log.errorLog(952136547, "keine URL: " + strUrlFeed);
          } else if (url.startsWith("http")) {
            // URLs, die mit http-Protokoll beginnen, ignorieren
            // das sind Themen, die bereits unter anderem Buchstaben vorhanden sind
            // Beispiel: Buchstabe S -> Sachsenspiegel ist unter Buchstabe M -> MDR Sachsenspiegel vorhanden
          } else {
            meldung(url);
            url = "http://www.mdr.de" + url;
            addSendugen(strUrlFeed, thema, url);
          }
        }
        if (url.isEmpty()) {
          Log.errorLog(766250249, "keine URL: " + strUrlFeed);
        }
      } catch (Exception ex) {
        Log.errorLog(316874602, ex);
      }
    }

    private void addTage(String urlSeite) {
      final String MUSTER = "<div class=\"media mediaA \">";

      int pos = 0;
      String thema, url = "";
      try {
        seiteTage = getUrl.getUri(SENDERNAME, urlSeite, StandardCharsets.UTF_8, 2 /* versuche */, seiteTage, "");
        while (!Config.getStop() && (pos = seiteTage.indexOf(MUSTER, pos)) != -1) {
          pos += MUSTER.length();
          url = seiteTage.extract("<a href=\"/mediathek/", "\"", pos);
          thema = seiteTage.extract(" class=\"headline\" title=\"\">", "<", pos);
          if (url.isEmpty()) {
            Log.errorLog(975401478, "keine URL: " + urlSeite);
          } else {
            meldung(url);
            url = "http://www.mdr.de/mediathek/" + url;
            addSendug(urlSeite, thema, url);
          }
        }
        if (url.isEmpty()) {
          Log.errorLog(930215470, "keine URL: " + urlSeite);
        }
      } catch (Exception ex) {
        Log.errorLog(102540897, ex);
      }
    }

    private void addSendugen(String strUrlFeed, String thema, String urlThema) {
      seite2 = getUrl.getUri(SENDERNAME, urlThema, StandardCharsets.UTF_8, 2 /* versuche */, seite2, "Thema: " + thema);
      final String muster;
      if (seite2.indexOf("div class=\"media mediaA \">") != -1) {
        muster = "div class=\"media mediaA \">";
      } else {
        muster = "<span class=\"broadcastSeriesTitle\">";
      }
      int pos = 0, count = 0;
      String url = "";
      while ((pos = seite2.indexOf(muster, pos)) != -1) {
        ++count;
        if (!CrawlerTool.loadLongMax()) {
          if (count > 5) {
            return;
          }
        }
        pos += muster.length();
        url = seite2.extract("<a href=\"/mediathek/fernsehen/a-z", "\"", pos);
        if (url.isEmpty()) {
          Log.errorLog(915263421, new String[]{"keine URL: " + urlThema, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
        } else {
          url = "http://www.mdr.de/mediathek/fernsehen/a-z" + url;
          addSendug(strUrlFeed, thema, url);
        }
      }

      if (url.isEmpty()) {
        Log.errorLog(765213014, new String[]{"keine URL: " + urlThema, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
      }
    }

    private void addSendug(String strUrlFeed, String thema, String urlSendung) {
      final String MUSTER_XML = "'playerXml':'";
      final String MUSTER_ADD = "http://www.mdr.de";
      seite3 = getUrl.getUri_Utf(SENDERNAME, urlSendung, seite3, "Thema: " + thema);
      int pos = 0;
      int pos1;
      int pos2;
      String url = "";
      int stop = seite3.indexOf("Meistgeklickt");
      while ((pos = seite3.indexOf(MUSTER_XML, pos)) != -1) {
        if (stop > 0 && pos > stop) {
          break;
        }
        pos += MUSTER_XML.length();
        pos1 = pos;
        if ((pos2 = seite3.indexOf("'", pos)) != -1) {
          url = seite3.substring(pos1, pos2);
        }
        if (url.isEmpty()) {
          Log.errorLog(256987304, new String[]{"keine URL: " + urlSendung, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
        } else {
          url = url.replace("\\", "");
          url = MUSTER_ADD + url;
          addXml(strUrlFeed, thema, url, urlSendung);
        }
      }
      if (url.isEmpty()) {
        Log.errorLog(256987304, new String[]{"keine URL: " + urlSendung, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
      }
    }

    private void addXml(String strUrlFeed, String thema, String xmlSite, String filmSite) {
      final String MUSTER_URL_MP4 = "<progressiveDownloadUrl>";
      String titel, datum, zeit, urlMp4, urlMp4_klein, urlHD, urlSendung, description;
      long duration;

      try {
        seite4 = getUrl.getUri_Utf(SENDERNAME, xmlSite, seite4, "Thema: " + thema);
        if (seite4.length() == 0) {
          Log.errorLog(903656532, xmlSite);
          return;
        }

        duration = 0;
        try {
          String d = seite4.extract("<duration>", "<");
          if (!d.isEmpty()) {
            String[] parts = d.split(":");
            duration = 0;
            long power = 1;
            for (int i = parts.length - 1; i >= 0; i--) {
              duration += Long.parseLong(parts[i]) * power;
              power *= 60;
            }
          }
        } catch (Exception ex) {
          Log.errorLog(313698749, ex, xmlSite);
        }

        titel = seite4.extract("<title>", "<");
        description = seite4.extract("<teaserText>", "<");
        String subtitle = seite4.extract("<videoSubtitleUrl>", "<");
        datum = seite4.extract("<broadcastStartDate>", "<");
        if (datum.isEmpty()) {
          datum = seite4.extract("<datetimeOfBroadcasting>", "<");
        }
        if (datum.isEmpty()) {
          datum = seite4.extract("<webTime>", "<");
        }
        zeit = convertZeitXml(datum);
        datum = convertDatumXml(datum);
        urlSendung = seite4.extract("<htmlUrl>", "<");
        if (urlSendung.isEmpty()) {
          urlSendung = filmSite;
        }

        // Film-URLs suchen
        urlHD = seite4.extract("| 1280x720", MUSTER_URL_MP4, "<");
        urlMp4 = seite4.extract("| 960x540", MUSTER_URL_MP4, "<");
        if (urlMp4.isEmpty()) {
          urlMp4 = seite4.extract("| MP4 Web L+ |", MUSTER_URL_MP4, "<");
        }
        urlMp4_klein = seite4.extract("| 512x288", MUSTER_URL_MP4, "<");

        if (urlMp4.isEmpty()) {
          urlMp4 = urlMp4_klein;
          urlMp4_klein = "";
        }

        if (urlMp4.isEmpty()) {
          Log.errorLog(326541230, new String[]{"keine URL: " + xmlSite, "Thema: " + thema, " UrlFeed: " + strUrlFeed});
        } else if (!existiertSchon(thema, titel, datum, zeit)) {
          meldung(urlMp4);

          DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, urlMp4, ""/*rtmpUrl*/, datum, zeit, duration, description);
          CrawlerTool.addUrlKlein(film, urlMp4_klein, "");
          CrawlerTool.addUrlHd(film, urlHD, "");
          CrawlerTool.addUrlSubtitle(film, subtitle);
          addFilm(film);
        }

      } catch (Exception ex) {
        Log.errorLog(446286970, ex);
      }
    }
  }

  private String convertDatumXml(String datum) {
    //<broadcastStartDate>23.08.2012 22:05</broadcastStartDate>
    try {
      SimpleDateFormat sdfIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      Date filmDate = sdfIn.parse(datum);
      SimpleDateFormat sdfOut;
      sdfOut = new SimpleDateFormat("dd.MM.yyyy");
      datum = sdfOut.format(filmDate);
    } catch (Exception ex) {
      Log.errorLog(435209987, ex);
    }
    return datum;
  }

  private String convertZeitXml(String datum) {
    //<broadcastStartDate>23.08.2012 22:05</broadcastStartDate>
    try {
      SimpleDateFormat sdfIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      Date filmDate = sdfIn.parse(datum);
      SimpleDateFormat sdfOut;
      sdfOut = new SimpleDateFormat("HH:mm:ss");
      datum = sdfOut.format(filmDate);
    } catch (Exception ex) {
      Log.errorLog(102658736, ex);
    }
    return datum;
  }

  private synchronized String getListeTage() {
    return listeTage.pollFirst();
  }

  private synchronized boolean existiertSchon(String thema, String titel, String datum, String zeit) {
    // liefert true wenn schon in der Liste, ansonsten fügt es ein
    boolean gefunden = false;
    for (String[] k : listeGesucht) {
      if (k[0].equalsIgnoreCase(thema) && k[1].equalsIgnoreCase(titel) && k[2].equalsIgnoreCase(datum) && k[3].equalsIgnoreCase(zeit)) {
        gefunden = true;
      }
    }
    if (!gefunden) {
      listeGesucht.add(new String[]{thema, titel, datum, zeit});
    }
    return gefunden;
  }
}
