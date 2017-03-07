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
package mServer;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.renderer.SimpleTextRenderer;
import javafx.application.Application;
import mServer.crawler.CrawlerTool;
import mServer.crawler.gui.MSG;
import mServer.tool.MserverDaten;
import mServer.tool.MserverDatumZeit;
import mServer.tool.MserverLog;

public class Main {

    public Main() {
    }

    private enum StartupMode {

        SERVER, VERSION, GUI
    }

    public static void main(String[] args) {

        StartupMode state = StartupMode.SERVER;

        BasicEtmConfigurator.configure();
        EtmManager.getEtmMonitor().start();

        if (args != null) {
            for (String s : args) {
                s = s.toLowerCase();
                switch (s) {
                    case "-d":
                        MserverDaten.debug = true;
                        break;
                    case "-v":
                        state = StartupMode.VERSION;
                        break;
                    case "-gui":
                        state = StartupMode.GUI;
                        break;

                }
            }
        }

        switch (state) {
            case SERVER:
                try {
                    runServer(args);
                } catch (InterruptedException e) {
                    MserverLog.fehlerMeldung(34975920, Main.class.getName(), "startServer", e);
                }
                break;
            case VERSION:
                MserverLog.versionsMeldungen(Main.class.toString());
                System.exit(0);
                break;
            case GUI:
                java.awt.EventQueue.invokeLater(() -> {
                    CrawlerTool.startMsg();
                    Application.launch(MSG.class, args);
                });
        }

        EtmManager.getEtmMonitor().render(new SimpleTextRenderer());
        EtmManager.getEtmMonitor().stop();
    }

    private static void runServer(String[] ar) throws InterruptedException {
        while (new MServer(ar).starten()) {
            long timeToSleep = (MserverDatumZeit.getSecondsUntilNextDay() + 120) * 1000; // 0:02
            MserverLog.systemMeldung("Schlafenlegen bis zum nächsten Tag (" + timeToSleep + "ms)");
            Thread.sleep(timeToSleep);
            MserverLog.systemMeldung("Neustart der Suche");
        }
    }

}
