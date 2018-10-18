/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package mServer.crawler;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author emil
 */
public class CrawlerTool {

  private static final String URL_START_WITHOUT_PROTOCOL_PATTERN = "//";
  private static final String HTTPS = "https:";
  private static final Logger LOG = LogManager.getLogger(CrawlerTool.class);
  // aktuelle
  // Filmliste, xz
  // komprimiert
  private static final String RTMP = "rtmp";

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

}
