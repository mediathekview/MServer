package mediathekServer;

import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Log;

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
public class Main {

    public static void main(String[] args) {
        final String ar[] = args;
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                if (ar != null) {
                    for (String s : ar) {
                        if (s.equalsIgnoreCase("-d")) {
                            MS_Daten.debug = true;
                        }
                        if (s.equalsIgnoreCase("-v")) {
                            MS_Log.versionsMeldungen(this.getClass().getName());
                            System.exit(0);
                        }
                        if (s.equalsIgnoreCase("-muster")) {
                            new MediathekServer().musterSchreiben(ar);
                        }
                    }
                }
                new MediathekServer(ar).starten();
            }
        });

    }
}
