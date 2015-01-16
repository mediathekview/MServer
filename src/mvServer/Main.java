package mvServer;

import mvServer.tool.MvSDaten;
import mvServer.tool.MvSLog;

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

    public Main() {
    }

    private enum StartupMode {

        SERVER, VERSION
    }

    public static void main(String[] args) {
        final String ar[] = args;

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                StartupMode state = StartupMode.SERVER;

                if (ar != null) {
                    for (String s : ar) {
                        s = s.toLowerCase();
                        switch (s) {
                            case "-d":
                                MvSDaten.debug = true;
                                break;
                            case "-v":
                                state = StartupMode.VERSION;
                                break;

                        }
                    }
                }

                switch (state) {
                    case SERVER:
                        new MvServer(ar).starten();
                        break;
                    case VERSION:
                        MvSLog.versionsMeldungen(this.getClass().getName());
                        System.exit(0);
                        break;
                }
            }
        });

    }
}
