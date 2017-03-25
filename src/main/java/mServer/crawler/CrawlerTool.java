/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Functions;
import de.mediathekview.mlib.tool.Log;

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
