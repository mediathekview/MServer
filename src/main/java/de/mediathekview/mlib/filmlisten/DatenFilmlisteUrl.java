/*    
 *    MediathekView
 *    Copyright (C) 2008   W. Xaver
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.tool.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

public class DatenFilmlisteUrl implements Comparable<DatenFilmlisteUrl> {

    public static final String SERVER_ART_AKT = "akt";
    public static final String SERVER_ART_OLD = "old";
    public static final String SERVER_ART_DIFF = "diff";

    public static final String FILM_UPDATE_SERVER_PRIO_1 = "1";
    public static final String FILM_UPDATE_SERVER = "film-update-server";
    public static final String FILM_UPDATE_SERVER_NR = "film-update-server-nr";
    public static final int FILM_UPDATE_SERVER_NR_NR = 0;
    public static final String FILM_UPDATE_SERVER_URL = "film-update-server-url";
    public static final int FILM_UPDATE_SERVER_URL_NR = 1;
    public static final String FILM_UPDATE_SERVER_DATUM = "film-update-server-datum"; // Datum in UTC
    public static final int FILM_UPDATE_SERVER_DATUM_NR = 2;
    public static final String FILM_UPDATE_SERVER_ZEIT = "film-update-server-zeit"; // Zeit in UTC
    public static final int FILM_UPDATE_SERVER_ZEIT_NR = 3;
    public static final String FILM_UPDATE_SERVER_PRIO = "film-update-server-prio";
    public static final int FILM_UPDATE_SERVER_PRIO_NR = 4;
    public static final String FILM_UPDATE_SERVER_ART = "film-update-server-art";
    public static final int FILM_UPDATE_SERVER_ART_NR = 5;
    public static final int FILM_UPDATE_SERVER_MAX_ELEM = 6;
    public static final String[] FILM_UPDATE_SERVER_COLUMN_NAMES = {FILM_UPDATE_SERVER_NR, FILM_UPDATE_SERVER_URL,
        FILM_UPDATE_SERVER_DATUM, FILM_UPDATE_SERVER_ZEIT, FILM_UPDATE_SERVER_PRIO, FILM_UPDATE_SERVER_ART};

    public static final String[] FILM_UPDATE_SERVER_COLUMN_NAMES_ANZEIGE = {"Nr", "Update-Url", "Datum", "Zeit", "Prio", "Art"};

    public String[] arr;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public DatenFilmlisteUrl() {
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        makeArr();
    }

    public DatenFilmlisteUrl(String url, String prio, String art) {
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        makeArr();
        arr[FILM_UPDATE_SERVER_URL_NR] = url;
        arr[FILM_UPDATE_SERVER_PRIO_NR] = prio;
        arr[FILM_UPDATE_SERVER_DATUM_NR] = "";
        arr[FILM_UPDATE_SERVER_ZEIT_NR] = "";
        arr[FILM_UPDATE_SERVER_ART_NR] = art;
    }

    public DatenFilmlisteUrl(String url, String art) {
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        makeArr();
        arr[FILM_UPDATE_SERVER_URL_NR] = url;
        arr[FILM_UPDATE_SERVER_PRIO_NR] = DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1;
        arr[FILM_UPDATE_SERVER_DATUM_NR] = "";
        arr[FILM_UPDATE_SERVER_ZEIT_NR] = "";
        arr[FILM_UPDATE_SERVER_ART_NR] = art;
    }

    public Date getDate() {
        String date = arr[FILM_UPDATE_SERVER_DATUM_NR] + " " + arr[FILM_UPDATE_SERVER_ZEIT_NR];
        Date d;
        try {
            d = sdf.parse(date);
        } catch (Exception ex) {
            d = new Date();
        }
        return d;
    }

    public String getDateStr() {
        SimpleDateFormat sdf_ = new SimpleDateFormat("dd.MM.yyyy");
        sdf_.setTimeZone(SimpleTimeZone.getDefault());
        String d;
        try {
            d = sdf_.format(getDate());
        } catch (Exception ex) {
            d = sdf_.format(new Date());
        }
        return d;
    }

    public String getTimeStr() {
        SimpleDateFormat sdf_ = new SimpleDateFormat("HH:mm:ss");
        sdf_.setTimeZone(SimpleTimeZone.getDefault());
        String d;
        try {
            d = sdf_.format(getDate());
        } catch (Exception ex) {
            d = sdf_.format(new Date());
        }
        return d;
    }

    @Override
    public int compareTo(DatenFilmlisteUrl arg0) {
        int ret = 0;
        try {
            //31.10.2010	16:54:17
            String ich = arr[FILM_UPDATE_SERVER_DATUM_NR] + " " + arr[FILM_UPDATE_SERVER_ZEIT_NR];
            String du = arg0.arr[FILM_UPDATE_SERVER_DATUM_NR] + " " + arg0.arr[FILM_UPDATE_SERVER_ZEIT_NR];
            if (ich.equals(du)) {
                return 0;
            }
            Date d_ich = sdf.parse(ich);
            Date d_du = sdf.parse(du);
            ret = d_du.compareTo(d_ich);
        } catch (ParseException ex) {
            Log.errorLog(936542876,   ex);
        }
        return ret;
    }

    public boolean aelterAls(int tage) {
        boolean ret = false;
        try {
            //31.10.2010	16:54:17
            String ich = arr[FILM_UPDATE_SERVER_DATUM_NR] + " " + arr[FILM_UPDATE_SERVER_ZEIT_NR];
            Date d_ich = sdf.parse(ich);
            Calendar cal = Calendar.getInstance();
            // tage vom calendar abziehen
            cal.add(Calendar.DATE, -tage);
            ret = d_ich.before(cal.getTime());
        } catch (ParseException ex) {
            Log.errorLog(915468973,   ex);
        }
        return ret;
    }

    private void makeArr() {
        arr = new String[FILM_UPDATE_SERVER_MAX_ELEM];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = "";
        }
    }
}
