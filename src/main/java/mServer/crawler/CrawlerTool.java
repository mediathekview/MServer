/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package mServer.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Functions;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.sender.MediathekReader;
import mServer.tool.MserverDatumZeit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author emil
 */
public class CrawlerTool {
  private static final String URL_START_WITHOUT_PROTOCOL_PATTERN = "//";
  private static final String HTTPS = "https:";
  private static final Logger LOG = LogManager.getLogger(CrawlerTool.class);
  public static final String nameOrgFilmlist_xz = "filme-org.xz"; // ist die
                                                                  // "ORG"
                                                                  // Filmliste,
                                                                  // typ. die
                                                                  // erste am
                                                                  // Tag, xz
                                                                  // komprimiert
  public static final String nameDiffFilmlist = "filme-diff.json"; // ist ein
                                                                   // diff der
                                                                   // aktuellen
                                                                   // zur ORG
                                                                   // Filmliste
  // Namen der Filmlisten im: Konfig-Ordner/filmlisten/
  public static final String nameAktFilmlist = "filme.json"; // ist die
                                                             // aktuelle
                                                             // Filmliste
  public static final String nameDiffFilmlist_xz = "filme-diff.xz"; // ist ein
                                                                    // diff
                                                                    // der
                                                                    // aktuellen
                                                                    // zur ORG
                                                                    // Filmliste,
                                                                    // xz
                                                                    // komprimiert
  public static final String nameOrgFilmlist = "filme-org.json"; // ist die
                                                                 // "ORG"
                                                                 // Filmliste,
                                                                 // typ. die
                                                                 // erste am
                                                                 // Tag
  public static final String nameAktFilmlist_xz = "filme.xz"; // ist die
                                                              // aktuelle
                                                              // Filmliste, xz
                                                              // komprimiert
  private static final String RTMP = "rtmp";

  public static Film createFilm(final Sender aSender, final String aUrlNormal, final String aTitel,
      final String aThema, final String aDatum, final String aZeit, final long aDurationInSecunds,
      final String aUrlWebseite, final String aBeschreibung, final String aUrlHd,
      final String aUrlSmall) throws MalformedURLException {
    final Film film = new Film(UUID.randomUUID(), aSender, aTitel, aThema,
        MserverDatumZeit.parseDateTime(aDatum, aZeit),
        Duration.of(aDurationInSecunds, ChronoUnit.SECONDS));
    film.setGeoLocations(CrawlerTool.getGeoLocations(aSender, aUrlNormal));
    film.setWebsite(new URL(aUrlWebseite));

    film.addUrl(Resolution.NORMAL, stringToFilmUrl(aUrlNormal));
    if (StringUtils.isNotBlank(aBeschreibung)) {
      film.setBeschreibung(aBeschreibung);
    }
    if (StringUtils.isNotBlank(aUrlHd)) {
      film.addUrl(Resolution.HD, CrawlerTool.stringToFilmUrl(aUrlHd));
    }
    if (StringUtils.isNotBlank(aUrlSmall)) {
      film.addUrl(Resolution.SMALL, CrawlerTool.stringToFilmUrl(aUrlSmall));
    }
    return film;
  }

  public static long getFileSize(final URL aURL) {
    long fileSize = 0;

    if (aURL.toString().contains(RTMP)) {
      // Cant get the size of rtmp.
      return -1;
    }

    final OkHttpClient client = MVHttpClient.getInstance().getSSLUnsafeClient();
    final Request request = new Request.Builder().url(aURL).head().build();
    try (Response response = client.newCall(request).execute();
        ResponseBody body = response.body()) {
      if (response.isSuccessful()) {
        long respLength = body.contentLength();
        if (respLength < 1_000_000) {
          respLength = -1;
        } else if (respLength > 1_000_000) {
          respLength /= 1_000_000;
        }
        fileSize = respLength;
      }
    } catch (final IOException ioException) {
      LOG.error(String.format("Die größe der Url \"%s\" konnte nicht ermittelt werden.", aURL),
          ioException);
    }

    return fileSize;

  }

  public static Collection<GeoLocations> getGeoLocations(final Sender aSender, final String aUrl) {
    switch (aSender) {
      case ARD:
      case WDR:
      case NDR:
      case SWR:
      case MDR:
      case BR:
        return getGeoLocationsArd(aUrl);

      case ZDF_TIVI:
      case DREISAT:
        return getGeoLocationsZdfPart(aUrl);

      case ORF:
        return getGeoLocationsOrf(aUrl);

      case SRF_PODCAST:
        return getGeoLocationsSrfPodcast(aUrl);

      case KIKA:
        return getGeoLocationsKiKa(aUrl);
      default:
        final Collection<GeoLocations> geoLocations = new ArrayList<>();
        geoLocations.add(GeoLocations.GEO_NONE);
        return geoLocations;
    }

  }

  public static String getPathFilmlist_json_akt(final boolean aktDate) {
    if (aktDate) {
      return Functions.addsPfad(CrawlerConfig.dirFilme,
          new SimpleDateFormat("yyyy.MM.dd__HH.mm.ss").format(new Date()) + "__" + nameAktFilmlist);
    } else {
      return Functions.addsPfad(CrawlerConfig.dirFilme, nameAktFilmlist);
    }
  }

  public static String getPathFilmlist_json_akt_xz() {
    return Functions.addsPfad(CrawlerConfig.dirFilme, nameAktFilmlist_xz);
  }

  public static String getPathFilmlist_json_diff() {
    return Functions.addsPfad(CrawlerConfig.dirFilme, nameDiffFilmlist);
  }

  public static String getPathFilmlist_json_diff_xz() {
    return Functions.addsPfad(CrawlerConfig.dirFilme, nameDiffFilmlist_xz);
  }

  public static String getPathFilmlist_json_org() {
    return Functions.addsPfad(CrawlerConfig.dirFilme, nameOrgFilmlist);
  }

  public static String getPathFilmlist_json_org_xz() {
    return Functions.addsPfad(CrawlerConfig.dirFilme, nameOrgFilmlist_xz);
  }

  public static void improveAufloesung(final Film aFilm) throws MalformedURLException {
    updateNormal(aFilm);
    updateHD(aFilm);
  }

  public static boolean loadLong() {
    return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_LONG;
  }

  public static boolean loadLongMax() {
    return CrawlerConfig.senderLoadHow >= CrawlerConfig.LOAD_LONG;
  }

  public static boolean loadMax() {
    return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_MAX;
  }

  public static boolean loadShort() {
    return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_SHORT;
  }

  public static synchronized void startMsg() {
    Log.startZeit.setTime(System.currentTimeMillis());
    Log.versionMsg(Const.PROGRAMMNAME);
    Log.sysLog(Log.LILNE);
    Log.sysLog("");
    Log.sysLog("Programmpfad: " + Functions.getPathJar());
    Log.sysLog("Filmliste: " + getPathFilmlist_json_akt(true /* aktDate */));
    Log.sysLog("Useragent: " + Config.getUserAgent());
    Log.sysLog("");
    Log.sysLog(Log.LILNE);
    Log.sysLog("");
    if (loadLongMax()) {
      Log.sysLog("Laden:  alles");
    } else {
      Log.sysLog("Laden:  nur update");
    }
    if (CrawlerConfig.updateFilmliste) {
      Log.sysLog("Filmliste:  nur updaten");
    } else {
      Log.sysLog("Filmliste:  neu erstellen");
    }
    Log.sysLog("ImportURL 1:  " + CrawlerConfig.importUrl_1__anhaengen);
    Log.sysLog("ImportURL 2:  " + CrawlerConfig.importUrl_2__anhaengen);
    Log.sysLog("ImportOLD:  " + CrawlerConfig.importOld);
    Log.sysLog("ImportAkt:  " + CrawlerConfig.importAkt);
    if (CrawlerConfig.nurSenderLaden != null) {
      Log.sysLog("Nur Sender laden:  " + StringUtils.join(CrawlerConfig.nurSenderLaden, ','));
    }
    Log.sysLog("");
    Log.sysLog(Log.LILNE);
  }

  public static FilmUrl stringToFilmUrl(final String aUrl) throws MalformedURLException {

    try {
      if (aUrl.startsWith(URL_START_WITHOUT_PROTOCOL_PATTERN)) {
        return uriToFilmUrl(new URL(HTTPS + aUrl));
      }
      return uriToFilmUrl(new URL(aUrl));

    } catch (final MalformedURLException aMalformedURLException) {
      LOG.error(String.format("Die URL \"%s\" ist kaputt.", aUrl));
      throw aMalformedURLException;
    }
  }

  public static void updateHD(final Film aFilm) throws MalformedURLException {
    final Map<String, List<String>> urls = new HashMap<>();

    urls.put("3328k_p36v12.mp4",
        Arrays.asList("1456k_p13v12.mp4", "2256k_p14v12.mp4", "2328k_p35v12.mp4"));
    urls.put("3256k_p15v12.mp4",
        Arrays.asList("1456k_p13v12.mp4", "2256k_p14v12.mp4", "2328k_p35v12.mp4"));

    urls.put("3296k_p15v13.mp4",
        Arrays.asList("1496k_p13v13.mp4", "2296k_p14v13.mp4", "2328k_p35v13.mp4"));
    urls.put("3328k_p36v13.mp4",
        Arrays.asList("1496k_p13v13.mp4", "2296k_p14v13.mp4", "2328k_p35v13.mp4"));

    if (aFilm.getUrl(Resolution.NORMAL).toString().contains("media.ndr.de")) {
      urls.put(".hd.mp4", Arrays.asList(".hq.mp4"));
    }

    if (aFilm.getUrl(Resolution.NORMAL).toString().contains("cdn-storage.br.de")) {
      urls.put("_X.mp4", Arrays.asList("_C.mp4"));
    }

    if (aFilm.getUrl(Resolution.NORMAL).toString().contains("pd-ondemand.swr.de")) {
      urls.put(".xl.mp4", Arrays.asList(".l.mp4"));
    }

    updateUrl(urls, aFilm, Resolution.HD);
  }

  public static void updateNormal(final Film aFilm) throws MalformedURLException {
    final Map<String, List<String>> urls = new HashMap<>();

    urls.put("2328k_p35v11.mp4", Arrays.asList("2256k_p14v11.mp4"));
    urls.put("2328k_p35v12.mp4", Arrays.asList("2256k_p14v12.mp4"));
    urls.put("2328k_p35v13.mp4", Arrays.asList("2296k_p14v13.mp4"));
    urls.put("2328k_p35v11.mp4", Arrays.asList("1456k_p13v11.mp4"));
    urls.put("2256k_p14v11.mp4", Arrays.asList("1456k_p13v11.mp4"));
    urls.put("2328k_p35v12.mp4", Arrays.asList("1456k_p13v12.mp4"));
    urls.put("2256k_p14v12.mp4", Arrays.asList("1456k_p13v12.mp4"));
    urls.put("2328k_p35v13.mp4", Arrays.asList("1496k_p13v13.mp4"));
    urls.put("2296k_p14v13.mp4", Arrays.asList("1496k_p13v13.mp4"));

    updateUrl(urls, aFilm, Resolution.NORMAL);
  }

  public static FilmUrl uriToFilmUrl(final URL aURL) {
    return new FilmUrl(aURL, getFileSize(aURL));
  }

  private static Collection<GeoLocations> getGeoLocationsArd(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
    geoUrls.put(GeoLocations.GEO_DE,
        Arrays.asList("mvideos-geo.daserste.de", "media.ndr.de/progressive_geo",
            "mediandr-a.akamaihd.net//progressive_geo", "pdodswr-a.akamaihd.net/swr/geo/de",
            "mediandr-a.akamaihd.net/progressive_geo", "cdn-storage.br.de/geo",
            "cdn-sotschi.br.de/geo/b7", "pd-ondemand.swr.de/geo/de", "ondemandgeo.mdr.de",
            "ondemand-de.wdr.de", "wdr_fs_geo-lh.akamaihd.net", "adaptiv.wdr.de/i/medp/de",
            "pd-videos.daserste.de/de", "wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de",
            "wdrmedien-a.akamaihd.net/medp/ondemand/de",
            "odgeomdr-a.akamaihd.net"));
    geoUrls.put(GeoLocations.GEO_DE_AT_CH,
        Arrays.asList("ondemand-dach.wdr.de", "wdradaptiv-vh.akamaihd.net/i/medp/ondemand/dach",
            "wdrmedien-a.akamaihd.net/medp/ondemand/dach", "adaptiv.wdr.de/i/medp/dach"));

    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeolocationsForGeoUrls(
      final Map<GeoLocations, List<String>> aGeoUrls, final String aUrl) {
    final Collection<GeoLocations> geoLocations = new HashSet<>();

    for (final GeoLocations geoLocation : aGeoUrls.keySet()) {
      for (final String geoUrl : aGeoUrls.get(geoLocation)) {
        if (aUrl.contains(geoUrl)) {
          geoLocations.add(geoLocation);
        }
      }
    }

    if (geoLocations.isEmpty()) {
      geoLocations.add(GeoLocations.GEO_NONE);
    }
    return geoLocations;
  }

  private static Collection<GeoLocations> getGeoLocationsKiKa(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
    geoUrls.put(GeoLocations.GEO_AT, Arrays.asList("pmdgeo.kika.de", "kika_geo-lh.akamaihd.net"));

    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeoLocationsOrf(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
    geoUrls.put(GeoLocations.GEO_AT,
        Arrays.asList("apasfpd.apa.at/cms-austria", "apasfpd.sf.apa.at/cms-austria", "apasfw.apa.at/cms-austria"));

    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeoLocationsSrfPodcast(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
    geoUrls.put(GeoLocations.GEO_CH, Arrays.asList("podcasts.srf.ch/ch/audio"));

    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeoLocationsZdfPart(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
    geoUrls.put(GeoLocations.GEO_DE, Arrays.asList("rodl.zdf.de/de", "rodlzdf-a.akamaihd.net/de"));

    geoUrls.put(GeoLocations.GEO_DE_AT_CH,
        Arrays.asList("rodl.zdf.de/dach", "rodlzdf-a.akamaihd.net/dach"));

    geoUrls.put(GeoLocations.GEO_DE_AT_CH_EU,
        Arrays.asList("rodl.zdf.de/ebu", "rodlzdf-a.akamaihd.net/ebu"));
    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static void updateUrl(final Map<String, List<String>> aUrls, final Film aFilm,
      final Resolution aTargetQuality) throws MalformedURLException {
    String url;
    if (aFilm.getUrls().containsKey(aTargetQuality)) {
      url = aFilm.getUrl(aTargetQuality).toString();
    } else {
      url = aFilm.getUrl(Resolution.NORMAL).toString();
    }

    for (final String betterUrl : aUrls.keySet()) {
      for (final String baderUrl : aUrls.get(betterUrl)) {
        if (url.contains(baderUrl)) {
          ;
        }
        {
          url = url.replace(baderUrl, betterUrl);
        }
      }
    }
    if (MediathekReader.urlExists(url)) {
      aFilm.addUrl(aTargetQuality, stringToFilmUrl(url));
    }
  }
}
