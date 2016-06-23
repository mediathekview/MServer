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
import java.util.Date;

public class MvSDatumZeit {

    private static final SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
    private static final SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat sdf_datum_yyyy_MM_dd = new SimpleDateFormat("yyyy.MM.dd");

    public static String getJetzt() {
        return sdf_datum_zeit.format(new Date());
    }

    public static String getHeute() {
        return sdf_datum.format(new Date());
    }

    public static String getHeute_yyyy_MM_dd() {
        return sdf_datum_yyyy_MM_dd.format(new Date());
    }


}
