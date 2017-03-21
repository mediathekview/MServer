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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.tool.MserverDaten;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * ist die Klasse die die HTML-Seite tatsächlich lädt
 */
public class GetUrl {

    private final static int PAUSE = 1_000;
    public static boolean showLoadTime = false; //DEBUG
    private long delayVal = TimeUnit.MILLISECONDS.convert(50, TimeUnit.MILLISECONDS);

    public GetUrl() {
    }

    public GetUrl(long delay) {
        setDelay(delay, TimeUnit.MILLISECONDS);
    }

    public void setDelay(long delay, TimeUnit delayUnit) {
        delayVal = TimeUnit.MILLISECONDS.convert(delay, delayUnit);
    }

    @Deprecated
    public MSStringBuilder getUri_Utf(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, StandardCharsets.UTF_8, 1 /* versuche */, seite, meldung);
    }

    public MSStringBuilder getUri_Iso(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, StandardCharsets.ISO_8859_1, 1 /* versuche */, seite, meldung);
    }

    public MSStringBuilder getUri(String sender, String addr, Charset encoding, int maxVersuche, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, encoding, maxVersuche, seite, meldung, "");
    }

    public MSStringBuilder getUriWithDelay(String sender, String addr, Charset encoding, int maxVersuche, MSStringBuilder seite, String meldung,
            long delay, TimeUnit delayUnit) {
        setDelay(delay, delayUnit);

        return getUri(sender, addr, encoding, maxVersuche, seite, meldung);
    }

    public MSStringBuilder getUri(String sender, String addr, Charset encoding, int maxVersuche, MSStringBuilder seite, String meldung, String token) {
        int aktVer = 0;
        boolean letzterVersuch;

        do {
            ++aktVer;
            try {

                if (delayVal > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayVal);
                    } catch (InterruptedException ignored) {
                    }
                }

                if (aktVer > 1) {
                    // und noch eine Pause vor dem nächsten Versuch
                    TimeUnit.MILLISECONDS.sleep(PAUSE);
                }
                letzterVersuch = (aktVer >= maxVersuche);
                seite = getUriNew(sender, addr, seite, encoding, meldung, maxVersuche, letzterVersuch, token);
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
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.WARTEZEIT_FEHLVERSUCHE, delayVal);
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

    private void updateStatistics(final String sender, final long bytesWritten) {
        FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_DATA_BYTE, bytesWritten);
        FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_BYTE, bytesWritten);

        FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_NIX, bytesWritten);
    }

    private long transferData(ResponseBody body, Charset encoding, MSStringBuilder seite) throws IOException {
        long load = 0;
        if (body.contentType() != null) {
            //valid response
            try (InputStreamReader inReader = new InputStreamReader(body.byteStream(), encoding)) {
                final char[] buffer = new char[16 * 1024];
                int n;
                while (!Config.getStop() && (n = inReader.read(buffer)) != -1) {
                    // hier wird endlich geladen
                    seite.append(buffer, 0, n);
                    load += n;
                }
            }
        }
        return load;
    }

    private long webCall(Request request, MSStringBuilder seite, final Charset encoding) throws IOException {
        long load = 0;
        try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
                ResponseBody body = response.body()) {
            if (response.isSuccessful()) {
                load = transferData(body, encoding, seite);
            }
        }
        return load;
    }

    private Request.Builder createRequestBuilder(String addr, String token) {
        Request.Builder builder = new Request.Builder().url(addr);
        if (!token.isEmpty()) {
            builder.addHeader("Api-Auth", "Bearer " + token);
        }
        //FIXME server user-agent not yet set
        return builder;
    }

    private MSStringBuilder getUriNew(String sender, String addr, MSStringBuilder seite,
            Charset encoding, String meldung, int versuch, boolean lVersuch,
            String token) {

        long load = 0;

        try {
            seite.setLength(0);

            if (MserverDaten.debug) {
                Log.sysLog("Durchsuche: " + addr);
            }

            final Request.Builder builder = createRequestBuilder(addr, token);
            load = webCall(builder.build(), seite, encoding);
        } catch (UnknownHostException | SocketTimeoutException ignored) {
            if (MserverDaten.debug) {
                printDebugMessage(meldung, addr, sender, versuch, ignored);
            }
        } catch (IOException ex) {
            if (lVersuch) {
                printDebugMessage(meldung, addr, sender, versuch, ex);
            }
        } catch (Exception ex) {
            Log.errorLog(973969801, ex, "");
        }

        updateStatistics(sender, load);

        return seite;
    }

    private void printDebugMessage(String meldung, String addr, String sender, int versuch, Exception ex) {
        //FIXME caller anzeige ist falsch in exception
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
            default:
                Log.errorLog(379861049, ex, text);
                break;
        }
    }
}
