package mServer.crawler.sender;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.tool.MserverDaten;

public class MediathekNdr extends MediathekReader implements Runnable {
  private class ThemaLaden extends Thread {

    private final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
    private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    private final ArrayList<String> liste = new ArrayList<>();

    @Override
    public void run() {
      try {
        meldungAddThread();
        final Iterator<String[]> themaIterator = listeThemen.iterator();
        while (!Config.getStop() && themaIterator.hasNext()) {
          final String[] thema = themaIterator.next();
          try {
            meldungProgress(thema[1]);
            feedEinerSeiteSuchen(thema[0], thema[1] /* thema */);
          } catch (final Exception ex) {
            Log.errorLog(336901211, ex);
          }
        }
      } catch (final Exception ex) {
        Log.errorLog(554632590, ex);
      }
      meldungThreadUndFertig();
    }

    private long convertDuration(final String duration, final String strUrlFeed) {
      long durationInSeconds = 0;
      try {
        if (!duration.isEmpty()) {
          final String[] parts = duration.split(":");
          long power = 1;
          durationInSeconds = 0;
          for (int i = parts.length - 1; i >= 0; i--) {
            durationInSeconds += Long.parseLong(parts[i]) * power;
            power *= 60;
          }
        }
      } catch (final NumberFormatException ex) {
        if (MserverDaten.debug) {
          Log.errorLog(369015497, ex, strUrlFeed);
        }
      } catch (final Exception ex) {
        Log.errorLog(369015497, ex, strUrlFeed);
      }

      return durationInSeconds;
    }

    private String extractDescription(final MSStringBuilder page) {
      final String desc = extractString(page, "<meta property=\"og:description\" content=\"", "\"");
      if (desc == null) {
        return "";
      }
      return desc;
    }

    private String extractString(final MSStringBuilder source, final String startMarker,
        final String endMarker) {
      int start = source.indexOf(startMarker);
      if (start == -1) {
        return null;
      }
      start = start + startMarker.length();
      final int end = source.indexOf(endMarker, start);
      if (end == -1) {
        return null;
      }
      return source.substring(start, end);
    }

    private void feedEinerSeiteSuchen(final String strUrlFeed, final String tthema) {
      final String MUSTER_URL = "<a href=\"";
      final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
      seite1 = getUrlIo.getUri(SENDER.getName(), strUrlFeed, StandardCharsets.UTF_8,
          3 /* versuche */, seite1, "Thema: " + tthema/* meldung */);
      int pos = 0;
      String url;
      String titel;
      String thema = tthema;
      String datum = "";
      String zeit = "";
      long durationInSeconds;
      String tmp;
      boolean tage = false;
      try {
        meldung(strUrlFeed);
        String muster;
        if (seite1.indexOf("<strong class=\"time\">") != -1) {
          muster = "<strong class=\"time\">";
          tage = true;
        } else {
          muster = "<span class=\"icon icon_video\"></span>";
        }
        while (!Config.getStop() && (pos = seite1.indexOf(muster, pos)) != -1) {
          pos += muster.length();
          url = seite1.extract(MUSTER_URL, "\"", pos);
          if (url.isEmpty()) {
            Log.errorLog(659210274, "keine Url feedEinerSeiteSuchen" + strUrlFeed);
            continue;
          }
          if (!url.startsWith("http")) {
            url = "http://www.ndr.de" + url;
          }
          if (tage) {
            // <h3><a href="/fernsehen/epg/import/Rote-Rosen,sendung64120.html" title="Rote Rosen"
            // >Rote Rosen (1725)</a></h3>
            thema = seite1.extract(MUSTER_URL, " title=\"", "\"", pos, 0, "");
            titel = seite1.extract(MUSTER_URL, ">", "<", pos, 0, "");
            if (titel.contains("(Wdh.)")) {
              // dann sollte der Beitrag schon in der Liste sein
              continue;
            }
            if (thema.equals(titel) && thema.contains(" - ")) {
              thema = thema.substring(0, thema.indexOf(" - ")).trim();
              titel = titel.substring(titel.indexOf(" - "));
              titel = titel.replace(" - ", "").trim();
            }
          } else {
            titel = seite1.extract(" title=\"", "\"", pos);
            titel = titel.replace("Zum Video:", "").trim();
          }
          if (tage) {
            tmp = seite1.substring(pos, seite1.indexOf("<", pos));
            datum = tthema;
            try {
              final Date filmDate = FastDateFormat.getInstance("HH:mm").parse(tmp);
              zeit = FastDateFormat.getInstance("HH:mm:ss").format(filmDate);
            } catch (final Exception ex) {
              Log.errorLog(795623017, "convertDatum: " + strUrlFeed);
            }
          } else {
            tmp = seite1.extract("<div class=\"subline date\">", "<", pos);
            final String[] dateValues = parseDateTime(tmp, strUrlFeed);
            datum = dateValues[0];
            zeit = dateValues[1];
          }
          if (tage) {
            // <span class="icon icon_video" aria-label="L&auml;nge"></span>29:59</div>
            final String duration = seite1.extract("\"L&auml;nge\"></span>", "<", pos).trim();
            durationInSeconds = convertDuration(duration, strUrlFeed);
          } else {
            String duration = seite1.extract("Video (", ")", pos);
            duration = duration.replace("min", "").trim();
            durationInSeconds = convertDuration(duration, strUrlFeed);
          }
          filmSuchen_1(strUrlFeed, thema, titel, url, datum, zeit, durationInSeconds);
        }
      } catch (final Exception ex) {
        Log.errorLog(693219870, strUrlFeed);
      }
    }

    private void filmSuchen_1(final String strUrlThema, final String thema, final String titel,
        final String filmWebsite, String datum, String zeit, final long durationInSeconds) {
      // playlist: [
      // {
      // 1:
      // {src:'http://hds.ndr.de/z/2013/0419/TV-20130419-1010-0801.,hi,hq,.mp4.csmil/manifest.f4m',
      // type:"application/f4m+xml"},
      // 2:
      // {src:'http://hls.ndr.de/i/2013/0419/TV-20130419-1010-0801.,lo,hi,hq,.mp4.csmil/master.m3u8',
      // type:"application/x-mpegURL"},
      // 3: {src:'http://media.ndr.de/progressive/2013/0419/TV-20130419-1010-0801.hi.mp4',
      // type:"video/mp4"},

      // http://media.ndr.de/progressive/2012/0820/TV-20120820-2300-0701.hi.mp4
      // rtmpt://cp160844.edgefcs.net/ondemand/mp4:flashmedia/streams/ndr/2012/0820/TV-20120820-2300-0701.hq.mp4
      seite2 =
          getUrl.getUri_Utf(SENDER.getName(), filmWebsite, seite2, "strUrlThema: " + strUrlThema);
      final String description = extractDescription(seite2);
      // String[] keywords = extractKeywords(seite2);
      String subtitle = seite2.extract(",tracks: [{ src: \"", "\""); // ,tracks: [{ src:
                                                                     // "/fernsehen/sendungen/45_min/video-podcast/ut20448.xml",
                                                                     // srclang:"de"}]
      if (!subtitle.isEmpty()) {
        subtitle = "http://www.ndr.de" + subtitle;
        // } else {
        // System.out.println("Test");
      }
      meldung(filmWebsite);
      int pos1;
      try {
        // src="/fernsehen/hallondsopplatt162-player_image-2c09ece0-0508-49bf-b4d6-afff2be2115c_theme-ndrde.html"
        // http://www.ndr.de/fernsehen/hallondsopplatt162-ppjson_image-2c09ece0-0508-49bf-b4d6-afff2be2115c.json
        // id="pp_hallondsopplatt162"
        if (datum.isEmpty()) {
          String tmp = seite2.extract("<span itemprop=\"datePublished\"", "</");
          if ((pos1 = tmp.indexOf(">")) != -1) {
            tmp = tmp.substring(pos1 + 1, tmp.length());
            final String[] dateValues = parseDateTime(tmp, strUrlThema);
            datum = dateValues[0];
            zeit = dateValues[1];
          }
        }

        String json = seite2.extract("<meta itemprop=\"embedURL\" content=\"", "\"");
        if (!json.isEmpty()) {
          json = json.replace("-player.html", "-ardjson.json");
          filmSuchen_2(strUrlThema, thema, titel, filmWebsite, json, datum, zeit, durationInSeconds,
              description, subtitle);

        } else {
          Log.errorLog(915230214, "auch keine Url: " + filmWebsite);
        }
      } catch (final Exception ex) {
        Log.errorLog(699830157, ex);
      }
    }

    private void filmSuchen_2(final String strUrlThema, final String thema, final String titel,
        final String filmWebsite, final String json, final String datum, final String zeit,
        final long durationInSeconds, final String description, String subtitle) {

      seite3 = getUrl.getUri_Utf(SENDER.getName(), json, seite3, "strUrlThema: " + strUrlThema);
      String url_hd = "", url_xl = "", url_m = "";
      seite3.extractList("", "", "\"_stream\": \"https://mediandr", "\"", "https://mediandr",
          liste);

      for (final String s : liste) {
        if (s.endsWith(".hd.mp4")) {
          url_hd = s;
        } else if (s.endsWith(".hq.mp4")) {
          url_xl = s;
        } else if (s.endsWith(".hi.mp4")) {
          url_m = s;
        }
      }
      liste.clear();
      if (url_xl.isEmpty()) {
        url_xl = url_m;
        url_m = "";
      }

      final String http = "http:";
      final String https = "https:";
      url_hd = url_hd.replaceFirst(https, http);
      url_xl = url_xl.replaceFirst(https, http);
      url_m = url_m.replaceFirst(https, http);

      if (subtitle.isEmpty()) {
        subtitle = seite3.extract("\"_subtitleUrl\":", "\"/", "\"", "http://www.ndr.de/");
      }

      if (!url_xl.isEmpty()) {
        try {
          final Film film = CrawlerTool.createFilm(SENDER, url_xl, titel, thema, datum, zeit,
              durationInSeconds, filmWebsite, description, url_hd, url_m);
          if (!subtitle.isEmpty()) {
            film.addSubtitle(new URL(subtitle));
          }
          addFilm(film);
        } catch (final MalformedURLException uriSyntaxEception) {
          LOG.error(
              String.format("Der Film \"%s - %s\" konnte nicht umgewandelt werden.", thema, titel),
              uriSyntaxEception);
        }
      } else {
        Log.errorLog(915234210, "keine URL im json: " + filmWebsite);
      }
    }

    /*
     * private String[] extractKeywords(MSStringBuilder page) { String keywords =
     * extractString(page, "<meta name=\"keywords\"  lang=\"de\" content=\"", "\""); if (keywords ==
     * null) { return new String[]{""}; } String[] k = keywords.split(","); for (int i = 0; i <
     * k.length; i++) { k[i] = k[i].trim(); } return k; }
     */

    private String[] parseDateTime(final String dateTimeValue, final String strUrlFeed) {
      final String[] dateValues = new String[2];
      dateValues[0] = "";
      dateValues[1] = "";

      final String dateTime = dateTimeValue.replace("Uhr", "").trim();

      if (!dateTime.isEmpty()) {
        try {
          final Date filmDate = FastDateFormat.getInstance("dd.MM.yyyy HH:mm").parse(dateTime);
          dateValues[0] = FastDateFormat.getInstance("dd.MM.yyyy").format(filmDate);
          dateValues[1] = FastDateFormat.getInstance("HH:mm:ss").format(filmDate);
        } catch (final Exception ex) {
          Log.errorLog(623657941, "convertDatum: " + strUrlFeed);
        }
      }

      return dateValues;
    }
  }

  private static final Logger LOG = LogManager.getLogger(MediathekNdr.class);
  public final static Sender SENDER = Sender.NDR;

  private MSStringBuilder seiteAlle = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

  public MediathekNdr(final FilmeSuchen ssearch, final int startPrio) {
    super(ssearch, SENDER.getName(), /* threads */ 2, /* urlWarten */ 50, startPrio);
  }

  private boolean alleSeiteSuchen(final String strUrlFeed, final String tthema) {
    boolean ret = false;
    final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
    seiteAlle = getUrlIo.getUri(SENDER.getName(), strUrlFeed, StandardCharsets.UTF_8,
        3 /* versuche */, seiteAlle, "Thema: " + tthema/* meldung */);
    int pos1 = 0, pos2, anz1, anz2 = 0;
    try {
      // <a class="square button" href="/mediathek/mediatheksuche105_broadcast-1391_page-5.html"
      // title="Zeige Seite 5">
      // http://www.ndr.de/mediathek/mediatheksuche105_broadcast-30_page-1.html
      final String WEITER = " title=\"Zeige Seite ";
      while ((pos1 = seiteAlle.indexOf(WEITER, pos1)) != -1) {
        pos1 += WEITER.length();
        if ((pos2 = seiteAlle.indexOf("\"", pos1)) != -1) {
          final String anz = seiteAlle.substring(pos1, pos2);
          try {
            anz1 = Integer.parseInt(anz);
            if (anz2 < anz1) {
              anz2 = anz1;
            }
          } catch (final Exception ex) {
            Log.errorLog(643208979, strUrlFeed);
          }
        }
      }
      for (int i = 2; i <= anz2 && i <= 10; ++i) {
        // geht bei 2 los da das ja schon die erste Seite ist!
        // das: http://www.ndr.de/mediathek/mediatheksuche105_broadcast-30.html
        // wird: http://www.ndr.de/mediathek/mediatheksuche105_broadcast-30_page-3.html
        final String url_ = strUrlFeed.replace(".html", "_page-" + i + ".html");
        listeThemen.add(new String[] {url_, tthema});
        ret = true;
      }
    } catch (final Exception ex) {
      Log.errorLog(913047821, strUrlFeed);
    }
    return ret;
  }

  // -> erste Seite:
  // <h5><a href="/mediathek/mediatheksuche103_broadcast-30.html">Nordmagazin</a></h5>
  @Override
  protected void addToList() {
    // <broadcast id="1391" site="ndrfernsehen">45 Min</broadcast>
    final String ADRESSE = "http://www.ndr.de/mediathek/sendungen_a-z/index.html";
    final String MUSTER_URL1 = "<li><a href=\"/mediathek/mediatheksuche105_broadcast-";
    listeThemen.clear();

    meldungStart();

    MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    final GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
    seite = getUrlIo.getUri(SENDER.getName(), ADRESSE, StandardCharsets.UTF_8, 5 /* versuche */,
        seite, ""/* meldung */);
    int pos = 0;
    int pos1;
    int pos2;
    String url = "";
    String thema = "";
    while ((pos = seite.indexOf(MUSTER_URL1, pos)) != -1) {
      try {
        pos += MUSTER_URL1.length();
        pos1 = pos;
        if ((pos2 = seite.indexOf("\"", pos)) != -1) {
          url = seite.substring(pos1, pos2);
        }
        pos1 = seite.indexOf(">", pos);
        pos2 = seite.indexOf("<", pos);
        if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
          thema = seite.substring(pos1 + 1, pos2);
        }
        if (url.isEmpty()) {
          Log.errorLog(210367600, "keine Url");
          continue;
        }
        final String url_ = "http://www.ndr.de/mediathek/mediatheksuche105_broadcast-" + url;
        final String[] add = new String[] {url_, thema};
        if (CrawlerTool.loadLongMax()) {
          if (!alleSeiteSuchen(url_, thema)) {
            // dann halt so versuchen
            listeThemen.add(add);
          }
        } else {
          listeThemen.add(add);
        }
      } catch (final Exception ex) {
        Log.errorLog(332945670, ex);
      }
    }
    // noch "Verpasst" für die letzten Tage einfügen
    // http://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2014-05-17.html
    // http://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2014-05-17_display-onlyvideo.html
    final FastDateFormat formatter1 = FastDateFormat.getInstance("yyyy-MM-dd");
    final FastDateFormat formatter2 = FastDateFormat.getInstance("dd.MM.yyyy");
    final int maxTage = CrawlerTool.loadLongMax() ? 30 : 20;
    for (int i = 0; i < maxTage; ++i) {
      // https://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2015-09-05_display-all.html
      final String URL = "http://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-";
      final String tag = formatter1.format(new Date().getTime() - 1000 * 60 * 60 * 24 * i);
      final String date = formatter2.format(new Date().getTime() - 1000 * 60 * 60 * 24 * i);
      // String urlString = URL + tag + "_display-onlyvideo.html"; --> stimmt leider nicht immer
      final String urlString = URL + tag + "_display-all.html";
      listeThemen.add(new String[] {urlString, date});
    }

    if (Config.getStop()) {
      meldungThreadUndFertig();
    } else if (listeThemen.isEmpty()) {
      meldungThreadUndFertig();
    } else {
      meldungAddMax(listeThemen.size());
      for (int t = 0; t < getMaxThreadLaufen(); ++t) {
        final Thread th = new ThemaLaden();
        th.setName(SENDER.getName() + t);
        th.start();
      }
    }
  }
}
