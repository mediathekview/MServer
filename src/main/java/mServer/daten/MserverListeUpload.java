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

public class MserverListeUpload extends LinkedList<MserverDatenUpload> {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean add(MserverDatenUpload mvsDatenUpload) {
        String[] was = mvsDatenUpload.arr[MserverDatenUpload.UPLOAD_LISTE_NR].split(",");
        for (String s : was) {
            MserverDatenUpload u = mvsDatenUpload.getCopy();
            u.arr[MserverDatenUpload.UPLOAD_LISTE_NR] = s;
            super.add(u);
        }
        return true;
    }

}
