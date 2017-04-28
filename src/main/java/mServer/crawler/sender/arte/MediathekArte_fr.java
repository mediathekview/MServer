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
package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;

public class MediathekArte_fr extends MediathekArte_de {

    private final static String SENDERNAME = Const.ARTE_FR;

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekArte_fr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, startPrio, SENDERNAME);
        URL_CONCERT = "http://concert.arte.tv/fr/videos/all";
        URL_CONCERT_NOT_CONTAIN = "-STA";
        TIME_1 = "<li>Diffusion :</li>";
        TIME_2 = "à";
        LANG_CODE = "fr";
    }
}
