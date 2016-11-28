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
import mSearch.tool.Functions;
import mSearch.tool.Log;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author emil
 */
public class crawlerTool {

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
        Log.sysLog("Filmliste: " + getPathFilmlist_json_akt(true /*aktDate*/ ));
        Log.sysLog("Useragent: " + Config.getUserAgent());
        Log.sysLog("");
        Log.sysLog(Log.LILNE);
        Log.sysLog("");
        if (loadLongMax()) {
            Log.sysLog("Laden:  alles");
        } else {
            Log.sysLog("Laden:  nur update");
        }
        if (crawlerConfig.updateFilmliste) {
            Log.sysLog("Filmliste:  nur updaten");
        } else {
            Log.sysLog("Filmliste:  neu erstellen");
        }
        Log.sysLog("ImportURL 1:  " + crawlerConfig.importUrl_1__anhaengen);
        Log.sysLog("ImportURL 2:  " + crawlerConfig.importUrl_2__anhaengen);
        Log.sysLog("ImportOLD:  " + crawlerConfig.importOld);
        Log.sysLog("ImportAkt:  " + crawlerConfig.importAkt);
        if (crawlerConfig.nurSenderLaden != null) {
            Log.sysLog("Nur Sender laden:  " + StringUtils.join(crawlerConfig.nurSenderLaden, ','));
        }
        Log.sysLog("");
        Log.sysLog(Log.LILNE);
    }

    public static boolean loadShort() {
        return crawlerConfig.senderLoadHow == crawlerConfig.LOAD_SHORT;
    }

    public static boolean loadLong() {
        return crawlerConfig.senderLoadHow == crawlerConfig.LOAD_LONG;
    }

    public static boolean loadMax() {
        return crawlerConfig.senderLoadHow == crawlerConfig.LOAD_MAX;
    }

    public static boolean loadLongMax() {
        return crawlerConfig.senderLoadHow >= crawlerConfig.LOAD_LONG;
    }

    public static String getPathFilmlist_json_org_xz() {
        return Functions.addsPfad(crawlerConfig.dirFilme, nameOrgFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff_xz() {
        return Functions.addsPfad(crawlerConfig.dirFilme, nameDiffFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff() {
        return Functions.addsPfad(crawlerConfig.dirFilme, nameDiffFilmlist);
    }

    public static String getPathFilmlist_json_akt_xz() {
        return Functions.addsPfad(crawlerConfig.dirFilme, nameAktFilmlist_xz);
    }

    public static String getPathFilmlist_json_org() {
        return Functions.addsPfad(crawlerConfig.dirFilme, nameOrgFilmlist);
    }

    /*public static String getUserAgent_dynamic() {
    int zufall = 1 + (int) (Math.random() * 10000); // 1 - 10000
    //String user = " user-" + zufall;
    if (userAgent == null) {
    return MSConst.USER_AGENT_DEFAULT + " user-" + zufall;
    } else {
    return userAgent + " user-" + zufall;
    }
    }*/
    public static String getPathFilmlist_json_akt(boolean aktDate) {
        if (aktDate) {
            return Functions.addsPfad(crawlerConfig.dirFilme, new SimpleDateFormat("yyyy.MM.dd__HH.mm.ss").format(new Date()) + "__" + nameAktFilmlist);
        } else {
            return Functions.addsPfad(crawlerConfig.dirFilme, nameAktFilmlist);
        }
    }
    
}
