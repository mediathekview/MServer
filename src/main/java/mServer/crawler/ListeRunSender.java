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
package mServer.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("serial")
public class ListeRunSender extends LinkedList<RunSender> {

    private final static String TRENNER = " | ";
    private static final String SENDER = " Sender ";

    public boolean listeFertig() {
        // liefert true wenn alle Sender fertig sind
        for (RunSender run : this) {
            if (!run.fertig) {
                return false;
            }
        }
        return true;
    }

    public RunSender getSender(String sender) {
        for (RunSender run : this) {
            if (run.sender.equals(sender)) {
                return run;
            }
        }
        return null;
    }

    public RunSender senderFertig(String sender) {
        for (RunSender run : this) {
            if (run.sender.equals(sender)) {
                run.fertig = true;
                run.endZeit = new Date();
                return run;
            }
        }
        return null;
    }

    public String getSenderRun() {
        final StringBuilder builder = new StringBuilder();
        for (RunSender run : this) {
            if (!run.fertig) {
                builder.append(run.sender);
                builder.append(' ');
            }
        }

        return builder.toString();
    }

    public int getAnzSenderRun() {
        int ret = 0;
        for (RunSender run : this) {
            if (!run.fertig) {
                ++ret;
            }
        }
        return ret;
    }

    public int getMax() {
        int ret = 0;
        for (RunSender run : this) {
            ret += run.max;
        }
        return ret;
    }

    public int getProgress() {
        int prog = 0;
        int max = 0;
        for (RunSender run : this) {
            prog += run.progress;
            max += run.max;
        }
        if (prog >= max && max >= 1) {
            prog = max - 1;
        }
        return prog;
    }

    public void inc(String sender, RunSender.Count what) {
        inc(sender, what, 1);
    }

    public void inc(String sender, RunSender.Count what, long i) {
        final AtomicLong counter = getCounter(sender).counter.get(what);
        counter.addAndGet(i);
    }

    public long get(String sender, RunSender.Count what) {
        return getCounter(sender).counter.get(what).get();
    }

    public String getRate(String sender) {
        String rate = "";
        int dauerSender = getSender(sender).getLaufzeitSekunden();
        long groesseByte = get(sender, RunSender.Count.SUM_TRAFFIC_BYTE);
        if (groesseByte > 0 && dauerSender > 0) {
            double doub = (1.0 * groesseByte / dauerSender / 1000); // kB/s
            rate = doub < 1 ? "<1" : String.format("%.1f", (doub));
        }
        return rate;
    }

    public long get(RunSender.Count what) {
        long ret = 0;
        for (RunSender run : this) {
            ret += run.counter.get(what).get();
        }
        return ret;
    }

    public ArrayList<String> getTextCount(ArrayList<String> ret) {
        getTextCount_(ret, new RunSender.Count[]{RunSender.Count.ANZAHL, RunSender.Count.FILME, RunSender.Count.FEHLER,
            RunSender.Count.FEHLVERSUCHE, RunSender.Count.WARTEZEIT_FEHLVERSUCHE, RunSender.Count.PROXY});
        ret.add("");
        ret.add("");

        getTextCount_(ret, new RunSender.Count[]{RunSender.Count.SUM_DATA_BYTE, RunSender.Count.SUM_TRAFFIC_BYTE,
            RunSender.Count.SUM_TRAFFIC_LOADART_NIX, RunSender.Count.GET_SIZE_SUM, RunSender.Count.GET_SIZE_PROXY});

        ret.add("");
        ret.add("");
        return ret;
    }

    public void getTextSum(ArrayList<String> retArray) {
        //wird ausgeführt wenn Sender beendet ist
        final String[] titel1 = {" Sender ", " [min] ", " [kB/s] ", "s/Seite", "Threads", "Wait"};
        String zeile = "";
        String[] names = new String[titel1.length];
        for (int i = 0; i < titel1.length; ++i) {
            names[i] = titel1[i];
            zeile += textLaenge(names[i].length(), names[i]) + TRENNER;
        }
        retArray.add(zeile);
        retArray.add("-------------------------------------------------------");

        for (RunSender run : this) {
            int dauerSender = run.getLaufzeitSekunden();
            long groesseByte = this.get(run.sender, RunSender.Count.SUM_TRAFFIC_BYTE);
            long anzahlSeiten = this.get(run.sender, RunSender.Count.ANZAHL);

            String rate = "";
            if (groesseByte > 0 && dauerSender > 0) {
                double doub = (1.0 * groesseByte / dauerSender / 1000); // kB/s
                rate = doub < 1 ? "<1" : String.format("%.1f", (doub));
            }

            String dauerProSeite = "";
            if (anzahlSeiten > 0) {
                dauerProSeite = String.format("%.2f", (1.0 * dauerSender / anzahlSeiten));
            }

            // =================================
            // Zeile1
            zeile = textLaenge(titel1[0].length(), run.sender) + TRENNER;
            zeile += textLaenge(titel1[1].length(), run.getLaufzeitMinuten()) + TRENNER;
            zeile += textLaenge(titel1[2].length(), rate) + TRENNER;
            zeile += textLaenge(titel1[3].length(), dauerProSeite) + TRENNER;
            zeile += textLaenge(titel1[4].length(), run.maxThreads + "") + TRENNER;
            zeile += textLaenge(titel1[5].length(), run.waitOnLoad + "") + TRENNER;
            retArray.add(zeile);
        }
        retArray.add("");
        retArray.add("");
    }

    private RunSender getCounter(String sender) {
        for (RunSender run : this) {
            if (run.sender.equals(sender)) {
                return run;
            }
        }
        RunSender ret = new RunSender(sender, 0, 0);
        add(ret);
        return ret;
    }

    private ArrayList<String> getTextCount_(ArrayList<String> ret, RunSender.Count[] spalten) {
        String zeile;
        String[] names = RunSender.Count.getNames();

        // Titelzeile
        zeile = SENDER + TRENNER;
        for (int i = 0; i < names.length; ++i) {
            // alle Spalten checken, ob gebraucht
            for (RunSender.Count sp : spalten) {
                if (i == sp.ordinal()) {
                    zeile += textLaenge(names[i].length(), names[i]) + TRENNER;
                }
            }
        }

        ret.add(zeile);
        ret.add("-------------------------------------------------------");

        for (RunSender run : this) {
            zeile = textLaenge(SENDER.length(), run.sender) + TRENNER;
            for (int i = 0; i < names.length; ++i) {
                // alle Spalten chekcken, ob gebraucht
                for (RunSender.Count sp : spalten) {
                    if (i == sp.ordinal()) {
                        if (i == RunSender.Count.SUM_DATA_BYTE.ordinal() || i == RunSender.Count.SUM_TRAFFIC_BYTE.ordinal()
                                || i == RunSender.Count.SUM_TRAFFIC_LOADART_NIX.ordinal()) {
                            zeile += textLaenge(names[i].length(), String.valueOf(RunSender.getStringZaehler(get(run.sender, RunSender.Count.values()[i])))) + TRENNER;
                        } else if (i == RunSender.Count.WARTEZEIT_FEHLVERSUCHE.ordinal()) {
                            long l = get(run.sender, RunSender.Count.values()[i]); // dann sinds ms
                            zeile += textLaenge(names[i].length(), String.valueOf(l == 0 ? "0" : (l < 1000 ? "<1" : l / 1000))) + TRENNER;
                        } else {
                            zeile += textLaenge(names[i].length(), String.valueOf(get(run.sender, RunSender.Count.values()[i]))) + TRENNER;
                        }
                    }
                }
            }
            ret.add(zeile);
        }
        ret.add("");
        return ret;
    }

    private String textLaenge(int max, String text) {
        if (text.length() > max) {
            text = text.substring(0, max - 1);
        }
        while (text.length() < max) {
            text = text + " ";
        }
        return text;
    }

}
