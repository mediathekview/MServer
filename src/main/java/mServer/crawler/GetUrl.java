/*
 *  MediathekView
 *  Copyright (C) 2008 W. Xaver
 *  W.Xaver[at]googlemail.com
 *  http://zdfmediathk.sourceforge.net/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import mSearch.Config;
import mSearch.Const;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;
import mSearch.tool.MVHttpClient;
import mServer.tool.MserverDaten;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * ist die Klasse die die HTML-Seite tatsächlich lädt
 */
public class GetUrl {

    private final static int WARTEN_NO_BUFFER = 2_000;
    private final static int PAUSE = 1_000;
    public static boolean showLoadTime = false; //DEBUG
    private long wartenBasis = 500;

    public GetUrl(long wwartenBasis) {
        wartenBasis = wwartenBasis;
    }

    public MSStringBuilder getUri_Utf(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, Const.KODIERUNG_UTF, 1 /* versuche */, seite, meldung);
    }

    public MSStringBuilder getUri_Iso(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, Const.KODIERUNG_ISO15, 1 /* versuche */, seite, meldung);
    }

    public MSStringBuilder getUri(String sender, String addr, String kodierung, int maxVersuche, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, kodierung, maxVersuche, seite, meldung, "");
    }

    public MSStringBuilder getUri(String sender, String addr, String kodierung, int maxVersuche, MSStringBuilder seite, String meldung, String token) {
        int aktVer = 0;
        boolean letzterVersuch;

        do {
            ++aktVer;
            try {
                if (aktVer > 1) {
                    // und noch eine Pause vor dem nächsten Versuch
                    TimeUnit.MILLISECONDS.sleep(PAUSE);
                }
                letzterVersuch = (aktVer >= maxVersuche);
                seite = getUriNew(sender, addr, seite, kodierung, meldung, maxVersuche, letzterVersuch, token);
                if (seite.length() > 0) {
                    // und nix wie weiter
                    if (Config.debug && aktVer > 1) {
                        String text = sender + " [" + aktVer + '/' + maxVersuche + "] ~~~> " + addr;
                        Log.sysLog(text);
                    }
                    // nur dann zählen
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.ANZAHL);
                    return seite;
                } else {
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.FEHLVERSUCHE);
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.WARTEZEIT_FEHLVERSUCHE.ordinal(), wartenBasis);
                    if (letzterVersuch) {
                        // dann wars leider nichts
                        FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.FEHLER);
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(698963200, ex, sender);
            }
        } while (!Config.getStop() && aktVer < maxVersuche);
        return seite;
    }

    private void updateStatistics(String sender, HttpCompressionType ladeArt, long bytesWritten) {
        FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_DATA_BYTE.ordinal(), bytesWritten);
        FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_BYTE.ordinal(), bytesWritten);
        switch (ladeArt) {
            case NONE:
                FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_NIX.ordinal(), bytesWritten);
                break;
            case DEFLATE:
                FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_DEFLATE.ordinal(), bytesWritten);
                break;
            case GZIP:
                FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_GZIP.ordinal(), bytesWritten);
                break;
        }
    }

    private MSStringBuilder getUriNew(String sender, String addr, MSStringBuilder seite,
                                      String kodierung, String meldung, int versuch, boolean lVersuch,
                                      String token) {
        EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("GetUrl.getUriNew");

        seite.setLength(0);

        long start = 0, stop;

        if (showLoadTime)
            start = System.currentTimeMillis();

        long load = 0;

        try {
            TimeUnit.MILLISECONDS.sleep(100);//wartenBasis
            if (MserverDaten.debug)
                Log.sysLog("Durchsuche: " + addr);
            Request.Builder builder = new Request.Builder().url(addr);
            if (!token.isEmpty()) {
                builder.addHeader("Api-Auth", "Bearer " + token);
            }

            Request request = builder.build();

            try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
                 ResponseBody body = response.body()) {
                if (response.isSuccessful()) {
                    if (body.contentType() != null) {
                        //valid response
                        try (InputStreamReader inReader = new InputStreamReader(body.byteStream(), kodierung)) {
                            final char[] buffer = new char[16 * 1024];
                            int n;
                            while (!Config.getStop() && (n = inReader.read(buffer)) != -1) {
                                // hier wird endlich geladen
                                seite.append(buffer, 0, n);
                                load += n;
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            if (lVersuch) {
                printDebugMessage(meldung, addr, sender, versuch, ex);
            }
        } catch (Exception ex) {
            Log.errorLog(973969801, ex, "");
        }

        if (showLoadTime) {
            stop = System.currentTimeMillis();
            final long diff = stop - start;
            System.out.println("\nDauer: " + diff / 1000 + ',' + diff % 1000);
        }

        updateStatistics(sender, HttpCompressionType.NONE, load);

        performancePoint.collect();

        return seite;
    }

    private void printDebugMessage(String meldung, String addr, String sender, int versuch, Exception ex) {
        String[] text;
        if (meldung.isEmpty()) {
            text = new String[]{"", "Sender: " + sender + " - Versuche: " + versuch,
                    "URL: " + addr};
        } else {
            text = new String[]{"", "Sender: " + sender + " - Versuche: " + versuch,
                    "URL: " + addr,
                    meldung};
        }
        switch (ex.getMessage()) {
            case "Read timed out":
                text[0] = "TimeOut: ";
                Log.errorLog(502739817, text);
                break;
            case "No buffer space available":
                text[0] = "No buffer space available";
                Log.errorLog(915263697, text);
                try {
                    // Pause zum Abbauen von Verbindungen
                    Thread.sleep(WARTEN_NO_BUFFER);
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.NO_BUFFER);
                } catch (Exception ignored) {
                }
                break;
            default:
                Log.errorLog(379861049, ex, text);
                break;
        }
    }

    private enum HttpCompressionType {NONE, DEFLATE, GZIP}
}
