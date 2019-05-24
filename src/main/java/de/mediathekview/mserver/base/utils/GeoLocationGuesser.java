/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package de.mediathekview.mserver.base.utils;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;

import java.util.*;

public class GeoLocationGuesser {

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

  private static Collection<GeoLocations> getGeoLocationsArd(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
    geoUrls.put(
        GeoLocations.GEO_DE,
        Arrays.asList(
            "mvideos-geo.daserste.de",
            "media.ndr.de/progressive_geo",
            "mediandr-a.akamaihd.net//progressive_geo",
            "pdodswr-a.akamaihd.net/swr/geo/de",
            "mediandr-a.akamaihd.net/progressive_geo",
            "cdn-storage.br.de/geo",
            "cdn-sotschi.br.de/geo/b7",
            "pd-ondemand.swr.de/geo/de",
            "ondemandgeo.mdr.de",
            "ondemand-de.wdr.de",
            "wdr_fs_geo-lh.akamaihd.net",
            "adaptiv.wdr.de/i/medp/de",
            "pd-videos.daserste.de/de",
            "wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de",
            "wdrmedien-a.akamaihd.net/medp/ondemand/de",
            "odgeomdr-a.akamaihd.net"));
    geoUrls.put(
        GeoLocations.GEO_DE_AT_CH,
        Arrays.asList(
            "ondemand-dach.wdr.de",
            "wdradaptiv-vh.akamaihd.net/i/medp/ondemand/dach",
            "wdrmedien-a.akamaihd.net/medp/ondemand/dach",
            "adaptiv.wdr.de/i/medp/dach"));

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
    geoUrls.put(
        GeoLocations.GEO_AT,
        Arrays.asList(
            "apasfpd.apa.at/cms-austria",
            "apasfpd.sf.apa.at/cms-austria",
            "apasfw.apa.at/cms-austria"));

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

    geoUrls.put(
        GeoLocations.GEO_DE_AT_CH,
        Arrays.asList("rodl.zdf.de/dach", "rodlzdf-a.akamaihd.net/dach"));

    geoUrls.put(
        GeoLocations.GEO_DE_AT_CH_EU,
        Arrays.asList("rodl.zdf.de/ebu", "rodlzdf-a.akamaihd.net/ebu"));
    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }
}
