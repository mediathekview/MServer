/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import java.text.SimpleDateFormat;
import java.util.Date;
import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.tool.Functions;
import mSearch.tool.Log;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author emil
 */
public class CrawlerTool {

    public static final String nameOrgFilmlist_xz = "filme-org.xz"; // ist die "ORG" Filmliste, typ. die erste am Tag, xz komprimiert
    public static final String nameDiffFilmlist = "filme-diff.json"; // ist ein diff der aktuellen zur ORG Filmliste
    // Namen der Filmlisten im: Konfig-Ordner/filmlisten/
    public static final String nameAktFilmlist = "filme.json"; // ist die aktuelle Filmliste
    public static final String nameDiffFilmlist_xz = "filme-diff.xz"; // ist ein diff der aktuellen zur ORG Filmliste, xz komprimiert
    public static final String nameOrgFilmlist = "filme-org.json"; // ist die "ORG" Filmliste, typ. die erste am Tag
    public static final String nameAktFilmlist_xz = "filme.xz"; // ist die aktuelle Filmliste, xz komprimiert

    public static synchronized void startMsg() {
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

    public static boolean loadShort() {
        return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_SHORT;
    }

    public static boolean loadLong() {
        return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_LONG;
    }

    public static boolean loadMax() {
        return CrawlerConfig.senderLoadHow == CrawlerConfig.LOAD_MAX;
    }

    public static boolean loadLongMax() {
        return CrawlerConfig.senderLoadHow >= CrawlerConfig.LOAD_LONG;
    }

    public static String getPathFilmlist_json_org_xz() {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameOrgFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff_xz() {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameDiffFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff() {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameDiffFilmlist);
    }

    public static String getPathFilmlist_json_akt_xz() {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameAktFilmlist_xz);
    }

    public static String getPathFilmlist_json_org() {
        return Functions.addsPfad(CrawlerConfig.dirFilme, nameOrgFilmlist);
    }

    public static String getPathFilmlist_json_akt(boolean aktDate) {
        if (aktDate) {
            return Functions.addsPfad(CrawlerConfig.dirFilme, new SimpleDateFormat("yyyy.MM.dd__HH.mm.ss").format(new Date()) + "__" + nameAktFilmlist);
        } else {
            return Functions.addsPfad(CrawlerConfig.dirFilme, nameAktFilmlist);
        }
    }

    public static void setGeo(DatenFilm film) {
        switch (film.arr[DatenFilm.FILM_SENDER]) {
            case Const.ARD:
                if (film.arr[DatenFilm.FILM_URL].startsWith("http://pd-videos.daserste.de/de/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_DE;
                }
            case Const.WDR:
            case Const.NDR:
            case Const.SWR:
            case Const.MDR:
            case Const.BR:
                if (film.arr[DatenFilm.FILM_URL].startsWith("http://mvideos-geo.daserste.de/") || 
                        film.arr[DatenFilm.FILM_URL].startsWith("http://media.ndr.de/progressive_geo/") || 
                        film.arr[DatenFilm.FILM_URL].startsWith("http://cdn-storage.br.de/geo/") || 
                        film.arr[DatenFilm.FILM_URL].startsWith("http://cdn-sotschi.br.de/geo/b7/") || 
                        film.arr[DatenFilm.FILM_URL].startsWith("http://pd-ondemand.swr.de/geo/de/") || 
                        film.arr[DatenFilm.FILM_URL].startsWith("http://ondemandgeo.mdr.de/") || 
                        film.arr[DatenFilm.FILM_URL].startsWith("http://ondemand-de.wdr.de/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_DE;
                }
                break;
            case Const.ZDF:
            case Const.ZDF_TIVI:
            case Const.DREISAT:
                if (film.arr[DatenFilm.FILM_URL].startsWith("http://nrodl.zdf.de/de/") || film.arr[DatenFilm.FILM_URL].startsWith("http://rodl.zdf.de/de/") || film.arr[DatenFilm.FILM_URL].startsWith("https://nrodlzdf-a.akamaihd.net/de/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_DE;
                } else if (film.arr[DatenFilm.FILM_URL].startsWith("http://nrodl.zdf.de/dach/") || film.arr[DatenFilm.FILM_URL].startsWith("http://rodl.zdf.de/dach/") || film.arr[DatenFilm.FILM_URL].startsWith("https://nrodlzdf-a.akamaihd.net/dach")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_DE + "-" + DatenFilm.GEO_AT + "-" + DatenFilm.GEO_CH;
                } else if (film.arr[DatenFilm.FILM_URL].startsWith("http://nrodl.zdf.de/ebu/") || film.arr[DatenFilm.FILM_URL].startsWith("http://rodl.zdf.de/ebu/") || film.arr[DatenFilm.FILM_URL].startsWith("https://nrodlzdf-a.akamaihd.net/ebu/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_DE + "-" + DatenFilm.GEO_AT + "-" + DatenFilm.GEO_CH + "-" + DatenFilm.GEO_EU;
                }
                break;
            case Const.ORF:
                if (film.arr[DatenFilm.FILM_URL].startsWith("http://apasfpd.apa.at/cms-austria/") || film.arr[DatenFilm.FILM_URL].startsWith("rtmp://apasfw.apa.at/cms-austria/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_AT;
                }
                break;
            case Const.SRF_PODCAST:
                if (film.arr[DatenFilm.FILM_URL].startsWith("http://podcasts.srf.ch/ch/audio/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_CH;
                }
                break;
            case Const.KIKA:
                if (film.arr[DatenFilm.FILM_URL].startsWith("http://pmdgeo.kika.de/")) {
                    film.arr[DatenFilm.FILM_GEO] = DatenFilm.GEO_DE;
                }
                break;
        }
    }

    public static void addUrlHd(DatenFilm film, String url, String urlRtmp) {
        film.arr[DatenFilm.FILM_URL_HD] = url.isEmpty() ? "" : getKlein(film.arr[DatenFilm.FILM_URL], url);
        film.arr[DatenFilm.FILM_URL_RTMP_HD] = urlRtmp.isEmpty() ? "" : getKlein(film.arr[DatenFilm.FILM_URL_RTMP], urlRtmp);
    }

    public static void addUrlSubtitle(DatenFilm film, String url) {
        film.arr[DatenFilm.FILM_URL_SUBTITLE] = url;
    }

    public static void addUrlKlein(DatenFilm film, String url, String urlRtmp) {
        film.arr[DatenFilm.FILM_URL_KLEIN] = url.isEmpty() ? "" : getKlein(film.arr[DatenFilm.FILM_URL], url);
        film.arr[DatenFilm.FILM_URL_RTMP_KLEIN] = urlRtmp.isEmpty() ? "" : getKlein(film.arr[DatenFilm.FILM_URL_RTMP], urlRtmp);
    }

    private static String getKlein(String url1, String url2) {
        String ret = "";
        boolean diff = false;
        for (int i = 0; i < url2.length(); ++i) {
            if (url1.length() > i) {
                if (url1.charAt(i) != url2.charAt(i)) {
                    if (!diff) {
                        ret = i + "|";
                    }
                    diff = true;
                }
            } else {
                diff = true;
            }
            if (diff) {
                ret += url2.charAt(i);
            }
        }
        return ret;
    }

}
