/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package de.mediathekview.mserver.base.utils;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;

import java.util.*;

public class GeoLocationGuesser {
  private GeoLocationGuesser() {
    super();
  }

  public static Collection<GeoLocations> getGeoLocations(final Sender aSender, final String aUrl) {
    switch (aSender) {
      case ARD:
      case WDR:
      case NDR:
      case SWR:
      case MDR:
      case BR:
      case RBB:
        return getGeoLocationsArd(aUrl);

      case ZDF:
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
    final Map<GeoLocations, List<String>> geoUrls = new EnumMap<>(GeoLocations.class);
    geoUrls.put(
        GeoLocations.GEO_DE,
        Arrays.asList(
            "mvideos-geo.daserste.de",
            "media.ndr.de/progressive_geo",
            "mediandr-a.akamaihd.net//progressive_geo",
            "/de/",
            "mediandr-a.akamaihd.net/progressive_geo",
            "cdn-storage.br.de/geo",
            "cdn-sotschi.br.de/geo/b7",
            "ondemandgeo.mdr.de",
            "ondemand-de.wdr.de",
            "wdr_fs_geo-lh.akamaihd.net",
            "odgeomdr-a.akamaihd.net",
            "hrardmediathek-a.akamaihd.net/video/as/geoblocking/",
            "rbbmediapmdp-a.akamaihd.net/content-de/"));
    geoUrls.put(
        GeoLocations.GEO_DE_AT_CH, Arrays.asList("ondemand-dach.wdr.de", "/dach/", "/deChAt/"));
    geoUrls.put(GeoLocations.GEO_DE_FR, List.of("arte-ard-mediathek"));

    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeolocationsForGeoUrls(
      final Map<GeoLocations, List<String>> geolocationUrls, final String url) {
    final Collection<GeoLocations> geoLocations = new HashSet<>();

    for (final var geoUrlEntry : geolocationUrls.entrySet()) {
      for (final String geoUrl : geoUrlEntry.getValue()) {
        if (url.contains(geoUrl)) {
          geoLocations.add(geoUrlEntry.getKey());
        }
      }
    }

    if (geoLocations.isEmpty()) {
      geoLocations.add(GeoLocations.GEO_NONE);
    }
    return geoLocations;
  }

  private static Collection<GeoLocations> getGeoLocationsKiKa(final String url) {
    final Map<GeoLocations, List<String>> geoUrls = new EnumMap<>(GeoLocations.class);
    geoUrls.put(GeoLocations.GEO_DE, Arrays.asList("pmdgeo.kika.de", "kika_geo-lh.akamaihd.net", "pmdgeokika"));

    final Collection<GeoLocations> geo = getGeolocationsForGeoUrls(geoUrls, url);
    if (geo.contains(GeoLocations.GEO_NONE)) {
      geo.clear();
      geo.addAll(getGeoLocationsArd(url));
    }
    if (geo.contains(GeoLocations.GEO_NONE)) {
      geo.clear();
      geo.addAll(getGeoLocationsZdfPart(url));
    }
    return geo;
  }

  private static Collection<GeoLocations> getGeoLocationsOrf(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new EnumMap<>(GeoLocations.class);
    geoUrls.put(
        GeoLocations.GEO_AT,
        Arrays.asList(
            "apasfpd.apa.at/cms-austria",
            "apasfpd.sf.apa.at/cms-austria",
            "apasfw.apa.at/cms-austria"));
    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeoLocationsSrfPodcast(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new EnumMap<>(GeoLocations.class);
    geoUrls.put(GeoLocations.GEO_CH, List.of("podcasts.srf.ch/ch/audio"));

    return getGeolocationsForGeoUrls(geoUrls, aUrl);
  }

  private static Collection<GeoLocations> getGeoLocationsZdfPart(final String aUrl) {
    final Map<GeoLocations, List<String>> geoUrls = new EnumMap<>(GeoLocations.class);
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
