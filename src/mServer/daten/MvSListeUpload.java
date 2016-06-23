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
package mServer.daten;

import java.util.LinkedList;

public class MvSListeUpload extends LinkedList<MvSDatenUpload> {

    @Override
    public boolean add(MvSDatenUpload mvsDatenUpload) {
        String[] was = mvsDatenUpload.arr[MvSDatenUpload.UPLOAD_LISTE_NR].split(",");
        for (String s : was) {
            MvSDatenUpload u = mvsDatenUpload.getCopy();
            u.arr[MvSDatenUpload.UPLOAD_LISTE_NR] = s;
            super.add(u);
        }
        return true;
    }

}
