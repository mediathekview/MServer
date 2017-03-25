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

public class MserverDatumZeit {

    private static final SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
    private static final SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat sdf_datum_yyyy_MM_dd = new SimpleDateFormat("yyyy.MM.dd");

    public static String getJetzt() {
        return sdf_datum_zeit.format(new Date());
    }

    public static String getHeute() {
        return sdf_datum.format(new Date());
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
        return sdf_datum_yyyy_MM_dd.format(new Date());
    }

    public static String getNameAkt(String path) {
        // liefert den Namen der Filmliste "akt" von heute
        if (path.isEmpty()) {
            return "";
        }
        return Functions.addsPfad(path, "Filmliste-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xz");
    }

}
