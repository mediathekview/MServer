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
package mServer.check;

public class Check {

    private enum CheckMode {

        NOTHING, CLEAN_SENDER_URL, CLEAN_URL
    }
    CheckMode state = CheckMode.NOTHING;

    final String SIMULATE = "-s";
    final String CHECK_SENDER_URL = "-csu";
    final String CHECK_URL = "-cu";

    final String HELP = ""
            + "\n"
            + "\n"
            + "erster Parameter: Filmliste\n"
            + "-s     Vorgang nur Simulieren\n"
            + "-cu    Check: Löschen von doppelten Einträgen: Url\n"
            + "-csu   Check: Löschen von doppelten Einträgen: Sender-Url\n"
            + "\n";

    String path = "";
    boolean simulate = false;

    public Check(String[] ar) {
        if (ar != null) {
            if (!getArgs(ar)) {
                System.out.println(HELP);
            }
        }
    }

    public void start() {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        switch (state) {
            case CLEAN_SENDER_URL:
                System.out.println("===================================================");
                System.out.println("Check: Löschen von doppelten Einträgen: Sender-Url");
                if (simulate) {
                    System.out.println("   nur simulieren");
                }
                System.out.println("===================================================");
                System.out.println("");
                new DelDuplicate(path, simulate).delSenderUrl();
                break;
            case CLEAN_URL:
                System.out.println("===========================================");
                System.out.println("Check: Löschen von doppelten Einträgen: Url");
                if (simulate) {
                    System.out.println("   nur simulieren");
                }
                System.out.println("===========================================");
                System.out.println("");
                new DelDuplicate(path, simulate).delUrl();
                break;
            case NOTHING:

        }
        System.out.println("");
        System.out.println("");
        System.out.println("");

        Thread.currentThread().interrupt();
    }

    private boolean getArgs(String[] ar) {
        boolean getPath = false;
        boolean getWhat = false;

        for (String s : ar) {
            if (!s.startsWith("-")) {
                path = s;
                getPath = true;
            }
            switch (s.toLowerCase()) {
                case SIMULATE:
                    simulate = true;
                    break;
                case CHECK_SENDER_URL:
                    state = CheckMode.CLEAN_SENDER_URL;
                    getWhat = true;
                    break;
                case CHECK_URL:
                    state = CheckMode.CLEAN_URL;
                    getWhat = true;
                    break;
            }
        }
        if (!getPath || !getWhat) {
            state = CheckMode.NOTHING;
        }
        return getPath && getWhat;
    }
}
