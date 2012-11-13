/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekServer.tool;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MS_DatumZeit {

    private static SimpleDateFormat sdf_stunde_minute = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
    private static SimpleDateFormat sdf_zeit = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");

    public static String getJetzt() {
        Date today;
        String output;
        today = new Date();
        output = sdf_datum_zeit.format(today);
        return output;
    }

    public static String getJetzt_hh_mm() {
        Date today;
        String output;
        today = new Date();
        output = sdf_stunde_minute.format(today);
        return output;
    }

    public static String getHeute() {
        Date today;
        String output;
        today = new Date();
        output = sdf_datum.format(today);
        return output;
    }

    public static Date convertDatum(String datum) {
        try {
            return sdf_datum.parse(datum);
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(852414170, MS_DatumZeit.class.getName(), datum, ex);
            return new Date();
        }
    }

    public static Date convertDateTime(String datumZeit) {
        try {
            return sdf_datum_zeit.parse(datumZeit);
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(919596320, MS_DatumZeit.class.getName(), datumZeit, ex);
            return new Date();
        }
    }
}
