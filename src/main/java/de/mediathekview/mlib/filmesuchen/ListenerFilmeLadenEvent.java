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
package de.mediathekview.mlib.filmesuchen;

public class ListenerFilmeLadenEvent {

    public String senderUrl = "";
    public String text = "";
    public int max = 0;
    public int progress = 0;
    public boolean fehler = false;
    public int count = 0;

    public ListenerFilmeLadenEvent(String ssender, String ttext, int mmax, int pprogress, int ccount, boolean ffehler) {
        senderUrl = ssender;
        text = ttext;
        max = mmax;
        progress = pprogress;
        count = ccount;
        fehler = ffehler;
    }
}
