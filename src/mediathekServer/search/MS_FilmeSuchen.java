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
package mediathekServer.search;

public class MS_FilmeSuchen {

    private static boolean allesLaden = false;
    private static String output = "";
    private static String imprtUrl = "";
    private static String userAgent = "";

    public static boolean filmeSuchen() {
        boolean ret = false;
//////////        new MediathekNoGui(MS_Daten.getBasisVerzeichnis(), allesLaden, output, imprtUrl, userAgent).starten();
        ret = true;
        return ret;
    }
}
