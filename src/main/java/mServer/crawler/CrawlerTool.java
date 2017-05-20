/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.*;
import de.mediathekview.mlib.tool.Functions;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.tool.MserverDatumZeit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author emil
 */
public class CrawlerTool
{
    private static final Logger LOG = LogManager.getLogger(CrawlerTool.class);
    public static final String nameOrgFilmlist_xz = "filme-org.xz"; // ist die "ORG" Filmliste, typ. die erste am Tag, xz komprimiert
    public static final String nameDiffFilmlist = "filme-diff.json"; // ist ein diff der aktuellen zur ORG Filmliste
    // Namen der Filmlisten im: Konfig-Ordner/filmlisten/
    public static final String nameAktFilmlist = "filme.json"; // ist die aktuelle Filmliste
    public static final String nameDiffFilmlist_xz = "filme-diff.xz"; // ist ein diff der aktuellen zur ORG Filmliste, xz komprimiert
    public static final String nameOrgFilmlist = "filme-org.json"; // ist die "ORG" Filmliste, typ. die erste am Tag
    public static final String nameAktFilmlist_xz = "filme.xz"; // ist die aktuelle Filmliste, xz komprimiert

    public static synchronized void startMsg()
    {
        Log.startZeit.setTime(System.currentTimeMillis());
        Log.versionMsg(Const.PROGRAMMNAME);
        Log.sysLog(Log.LILNE);
        Log.sysLog("");
        Log.sysLog("Programmpfad: " + Functions.getPathJar());
        Log.sysLog("Filmliste: " + getPathFilmlist_json_akt(true /*aktDate*/));
        Log.sysLog("Useragent: " + Config.getUserAgent());
        Log.sysLog("");
        Log.sysLog(Log.LILNE);
        Log.sysLog("");
        if (loadLongMax())
        {
            Log.sysLog("Laden:  alles");
        } else
        {
            Log.sysLog("Laden:  nur update");
        }
        if (CrawlerConfig.updateFilmliste)
        {
            Log.sysLog("Filmliste:  nur updaten");
        } else
        {
            Log.sysLog("Filmliste:  neu erstellen");
        }
        Log.sysLog("ImportURL 1:  " + CrawlerConfig.importUrl_1__anhaengen);
        Log.sysLog("ImportURL 2:  " + CrawlerConfig.importUrl_2__anhaengen);
        Log.sysLog("ImportOLD:  " + CrawlerConfig.importOld);
        Log.sysLog("ImportAkt:  " + CrawlerConfig.importAkt);
        if (CrawlerConfig.nurSenderLaden != null)
        {
            Log.sysLog("Nur Sender laden:  " + StringUtils.join(CrawlerConfig.nurSenderLaden, ','));
        }
        Log.sysLog("");
        Log.sysLog(Log.LILNE);
    }

    public static boolean loadShort()
    {
        return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_SHORT;
    }

    public static boolean loadLong()
    {
        return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_LONG;
    }

    public static boolean loadMax()
    {
        return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_MAX;
    }

    public static boolean loadLongMax()
    {
        return CrawlerConfig.senderLoadHow >= CrawlerConfig.LOAD_LONG;
    }

    public static String getPathFilmlist_json_org_xz()
    {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameOrgFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff_xz()
    {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameDiffFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff()
    {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameDiffFilmlist);
    }

    public static String getPathFilmlist_json_akt_xz()
    {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameAktFilmlist_xz);
    }

    public static String getPathFilmlist_json_org()
    {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameOrgFilmlist);
    }

    public static String getPathFilmlist_json_akt(boolean aktDate)
    {
        if (aktDate)
        {
            return Functions.addsPfad(CrawlerConfig.dirFilme, new SimpleDateFormat("yyyy.MM.dd__HH.mm.ss").format(new Date()) + "__" + nameAktFilmlist);
        } else
        {
            return Functions.addsPfad(CrawlerConfig.dirFilme, nameAktFilmlist);
        }
    }

    public static long getFileSize(URI aURI)
    {
        long fileSize = 0;

        OkHttpClient client = MVHttpClient.getInstance().getReducedTimeOutClient();
        Request request = new Request.Builder().url(aURI.toString()).head().build();
        try (Response response = client.newCall(request).execute();
             ResponseBody body = response.body())
        {
            if (response.isSuccessful())
            {
                long respLength = body.contentLength();
                if (respLength < 1_000_000)
                {
                    respLength = -1;
                } else if (respLength > 1_000_000)
                {
                    respLength /= 1_000_000;
                }
                fileSize = respLength;
            }
        } catch (IOException ioException)
        {
            LOG.error(String.format("Die größe der Url \"%s\" konnte nicht ermittelt werden.", aURI), ioException);
        }
        return fileSize;
    }

    public static FilmUrl uriToFilmUrl(URI aURI)
    {
        return new FilmUrl(aURI, getFileSize(aURI));
    }

    public static FilmUrl stringToFilmUrl(final String aUrl) throws URISyntaxException
    {
        try
        {
            return uriToFilmUrl(new URI(aUrl));
        } catch (URISyntaxException uriSyntaxException)
        {
            LOG.error(String.format("Die URL \"%s\" ist kaputt.", aUrl));
            throw uriSyntaxException;
        }
    }

    private static Collection<GeoLocations> getGeoLocationsArd(String aUrl)
    {
        Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
        geoUrls.put(GeoLocations.GEO_DE, Arrays.asList(
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
                "pd-videos.daserste.de/de"
        ));

        return getGeolocationsForGeoUrls(geoUrls, aUrl);
    }

    private static Collection<GeoLocations> getGeolocationsForGeoUrls(Map<GeoLocations, List<String>> aGeoUrls, String aUrl)
    {
        Collection<GeoLocations> geoLocations = new HashSet<>();

        for (GeoLocations geoLocation : aGeoUrls.keySet())
        {
            for (String geoUrl : aGeoUrls.get(geoLocation))
            {
                if (aUrl.contains(geoUrl))
                {
                    geoLocations.add(geoLocation);
                }
            }
        }

        if (geoLocations.isEmpty())
        {
            geoLocations.add(GeoLocations.GEO_NONE);
        }
        return geoLocations;
    }

    private static Collection<GeoLocations> getGeoLocationsZdfPart(String aUrl)
    {
        Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
        geoUrls.put(GeoLocations.GEO_DE, Arrays.asList(
                "rodl.zdf.de/de",
                "rodlzdf-a.akamaihd.net/de"
        ));

        geoUrls.put(GeoLocations.GEO_DE_AT_CH, Arrays.asList(
                "rodl.zdf.de/dach",
                "rodlzdf-a.akamaihd.net/dach"
        ));

        geoUrls.put(GeoLocations.GEO_DE_AT_CH_EU, Arrays.asList(
                "rodl.zdf.de/ebu",
                "rodlzdf-a.akamaihd.net/ebu"
        ));
        return getGeolocationsForGeoUrls(geoUrls, aUrl);
    }

    private static Collection<GeoLocations> getGeoLocationsSrfPodcast(String aUrl)
    {
        Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
        geoUrls.put(GeoLocations.GEO_CH, Arrays.asList(
                "podcasts.srf.ch/ch/audio"
        ));

        return getGeolocationsForGeoUrls(geoUrls, aUrl);
    }

    private static Collection<GeoLocations> getGeoLocationsOrf(String aUrl)
    {
        Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
        geoUrls.put(GeoLocations.GEO_AT, Arrays.asList(
                "apasfpd.apa.at/cms-austria",
                "apasfw.apa.at/cms-austria"
        ));

        return getGeolocationsForGeoUrls(geoUrls, aUrl);
    }

    private static Collection<GeoLocations> getGeoLocationsKiKa(String aUrl)
    {
        Map<GeoLocations, List<String>> geoUrls = new HashMap<>();
        geoUrls.put(GeoLocations.GEO_AT, Arrays.asList(
                "pmdgeo.kika.de",
                "kika_geo-lh.akamaihd.net"
        ));

        return getGeolocationsForGeoUrls(geoUrls, aUrl);
    }

    public static Collection<GeoLocations> getGeoLocations(final Sender aSender, final String aUrl)
    {
        switch (aSender)
        {
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
                Collection<GeoLocations> geoLocations = new ArrayList<>();
                geoLocations.add(GeoLocations.GEO_NONE);
                return geoLocations;
        }

    }

    public static Film createFilm(Sender aSender,
                                  String aUrlNormal,
                                  String aTitel,
                                  String aThema,
                                  String aDatum,
                                  String aZeit,
                                  long aDurationInSecunds,
                                  String aUrlWebseite,
                                  String aBeschreibung,
                                  String aUrlHd,
                                  String aUrlSmall) throws URISyntaxException
    {
        Film film = new Film(UUID.randomUUID(),
                CrawlerTool.getGeoLocations(aSender, aUrlNormal),
                aSender,
                aTitel,
                aThema,
                MserverDatumZeit.parseDateTime(aDatum, aZeit),
                Duration.of(aDurationInSecunds, ChronoUnit.SECONDS),
                new URI(aUrlWebseite));

        if (StringUtils.isNotBlank(aBeschreibung))
        {
            film.setBeschreibung(aBeschreibung);
        }
        if (StringUtils.isNotBlank(aUrlHd))
        {
            film.addUrl(Qualities.HD, CrawlerTool.stringToFilmUrl(aUrlHd));
        }
        if (StringUtils.isNotBlank(aUrlSmall))
        {
            film.addUrl(Qualities.SMALL, CrawlerTool.stringToFilmUrl(aUrlSmall));
        }
        return film;
    }
}
