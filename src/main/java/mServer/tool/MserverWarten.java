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

import java.util.concurrent.TimeUnit;

/**
 *
 * @author emil
 */
public class MserverWarten {

    public synchronized void sekundenWarten(int sekunden) {
        MserverLog.systemMeldung("Warten: " + String.valueOf(sekunden) + " Sekunden");
        try {
            while (sekunden > 0) {
                TimeUnit.SECONDS.sleep(1);
                sekunden--;
                System.out.print("\r");
                System.out.print(String.valueOf(sekunden));
            }
        } catch (Exception ex) {
            MserverLog.fehlerMeldung(347895642, MserverWarten.class.getName(), "Warten nach dem Suchen", ex);
        }
        System.out.println("");
    }
}
