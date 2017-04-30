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
package mServer.tool;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import de.mediathekview.mlib.tool.Functions;
import java.text.ParseException;
import org.apache.commons.lang3.time.FastDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MserverDatumZeit {

    private static final Logger LOG = LogManager.getLogger(MserverDatumZeit.class);
    
    private static final SimpleDateFormat SDF_DATUM_ZEIT = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
    private static final SimpleDateFormat SDF_DATUM = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat SDF_DATUM_YYYY_MM_DD = new SimpleDateFormat("yyyy.MM.dd");

    private static final FastDateFormat FDF_OUT_TIME = FastDateFormat.getInstance("HH:mm:ss");
    private static final FastDateFormat FDF_OUT_DAY = FastDateFormat.getInstance("dd.MM.yyyy");
    
    public static String getJetzt() {
        return SDF_DATUM_ZEIT.format(new Date());
    }

    public static String getHeute() {
        return SDF_DATUM.format(new Date());
    }

    public static long getSecondsUntilNextDay() {
        // now
        LocalDateTime now = LocalDateTime.now();
        // tomorrow 0:00
        LocalDateTime future = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1L);
        Duration duration = Duration.between(now, future);
        return duration.getSeconds();
    }

    public static String getHeute_yyyy_MM_dd() {
        return SDF_DATUM_YYYY_MM_DD.format(new Date());
    }

    public static String getNameAkt(String path) {
        // liefert den Namen der Filmliste "akt" von heute
        if (path.isEmpty()) {
            return "";
        }
        return Functions.addsPfad(path, "Filmliste-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xz");
    }

    /**
     * formats a date/datetime string to the date format used in DatenFilm
     * @param dateValue the date/datetime value
     * @param sdf the format of dateValue
     * @return the formatted date string
     */
    public static String formatDate(String dateValue, FastDateFormat sdf) {
        try {
            return FDF_OUT_DAY.format(sdf.parse(dateValue));
        } catch (ParseException ex) {
            LOG.debug(String.format("Fehler beim Parsen des Datums %s: %s", dateValue, ex.getMessage()));
        }
        
        return "";
    }

    /**
     * formats a datetime string to the time format used in DatenFilm
     * @param dateValue the datetime value
     * @param sdf the format of dateValue
     * @return the formatted time string
     */
    public static  String formatTime(String dateValue, FastDateFormat sdf) {
        try {
            return FDF_OUT_TIME.format(sdf.parse(dateValue));
        } catch (ParseException ex) {
            LOG.debug(String.format("Fehler beim Parsen des Datums %s: %s", dateValue, ex.getMessage()));
        }
        
        return "";
    }  
         
}
