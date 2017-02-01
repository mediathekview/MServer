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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import mSearch.Config;
import mSearch.Const;
import mSearch.tool.Log;
import mSearch.tool.MSStringBuilder;

/**
 * ist die Klasse die die HTML-Seite tatsächllich lädt
 *
 */
public class GetUrl {

    public static boolean showLoadTime = false;

    private static final long UrlWartenBasis = 500;//ms, Basiswert zu dem dann der Faktor multipliziert wird
    private int timeout = 10000;
    private long wartenBasis = UrlWartenBasis;
    public static final int LADE_ART_NIX = 1;
    public static final int LADE_ART_DEFLATE = 2;
    public static final int LADE_ART_GZIP = 3;
    final static Lock lock = new ReentrantLock();

    public GetUrl(long wwartenBasis) {
        wartenBasis = wwartenBasis;
    }

    //===================================
    // public
    //===================================
    public MSStringBuilder getUri_Utf(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, Const.KODIERUNG_UTF, 1 /* versuche */, seite, meldung);
    }

    public MSStringBuilder getUri_Iso(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, Const.KODIERUNG_ISO15, 1 /* versuche */, seite, meldung);
    }

    public synchronized MSStringBuilder getUri(String sender, String addr, String kodierung, int maxVersuche, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, kodierung, maxVersuche, seite, meldung, "");
    }

    public synchronized MSStringBuilder getUri(String sender, String addr, String kodierung, int maxVersuche, MSStringBuilder seite, String meldung, String token) {
        final int PAUSE = 1000;
        int aktTimeout = timeout;
        int aktVer = 0;
        boolean letzterVersuch;

        do {
            ++aktVer;
            try {
                if (aktVer > 1) {
                    // und noch eine Pause vor dem nächsten Versuch
                    this.wait(PAUSE);
                }
                letzterVersuch = (aktVer >= maxVersuche);
                seite = getUri(sender, addr, seite, kodierung, aktTimeout, meldung, maxVersuche, letzterVersuch, token);
                if (seite.length() > 0) {
                    // und nix wie weiter 
                    if (Config.debug && aktVer > 1) {
                        String text = sender + " [" + aktVer + "/" + maxVersuche + "] ~~~> " + addr;
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

    public void setTimeout(int ttimeout) {
        timeout = ttimeout;
    }

    public int getTimeout() {
        return timeout;
    }

    private synchronized MSStringBuilder getUri(String sender, String addr, MSStringBuilder seite,
            String kodierung, int timeout, String meldung, int versuch, boolean lVersuch,
            String token) {
        seite.setLength(0);
        HttpURLConnection conn = null;
        InputStream in = null;
        InputStreamReader inReader = null;
        int retCode;
        int ladeArt;
        MVInputStream mvIn = null;
        String encoding;
        Date start = null;
        Date stop;
        if (showLoadTime) {
            start = new Date();
        }

        // immer etwas bremsen
        try {
            this.wait(wartenBasis);
        } catch (Exception ex) {
            Log.errorLog(976120379, ex, sender);
        }

        try {
            Log.sysLog("Durchsuche: "+addr);
            // conn = url.openConnection(Proxy.NO_PROXY);
            conn = (HttpURLConnection) new URL(addr).openConnection();
            conn.setRequestProperty("User-Agent", Config.getUserAgent());
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

            if (!token.isEmpty()) {
                conn.setRequestProperty("Api-Auth", "Bearer " + token);
            }
            if (timeout > 0) {
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
            }
            // the encoding returned by the server
            encoding = conn.getContentEncoding();
            if ((retCode = conn.getResponseCode()) < 400) {
                mvIn = new MVInputStream(conn);
            } else if (retCode == 403 || retCode == 408) {
                if (!CrawlerConfig.proxyUrl.isEmpty() && CrawlerConfig.proxyPort > 0) {
                    // nur dann verwenden, ein anderer Versuch und wenn möglich, einen Proxy einrichten
                    SocketAddress saddr = new InetSocketAddress(CrawlerConfig.proxyUrl, CrawlerConfig.proxyPort);
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, saddr);
                    conn = (HttpURLConnection) new URL(addr).openConnection(proxy);

                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
                    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

                    if (timeout > 0) {
                        conn.setReadTimeout(timeout);
                        conn.setConnectTimeout(timeout);
                    }
                    encoding = conn.getContentEncoding();
                    mvIn = new MVInputStream(conn);
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.PROXY);
                }
            } else {
                // dann wars das
                Log.errorLog(974532107, new String[]{"HTTP-Fehlercode: " + retCode, "Sender: " + sender, "URL: " + addr,});
            }
            if (mvIn == null) {
                return seite;
            }
            if (mvIn.getInputStream() == null) {
                return seite;
            }
            if (encoding == null) {
                ladeArt = LADE_ART_NIX;
                in = mvIn;
            } else if (encoding.equalsIgnoreCase("gzip")) {
                ladeArt = LADE_ART_GZIP;
                in = new GZIPInputStream(mvIn);
            } else if (encoding.equalsIgnoreCase("deflate")) {
                ladeArt = LADE_ART_DEFLATE;
                in = new InflaterInputStream(mvIn, new Inflater(true));
            } else {
                ladeArt = LADE_ART_NIX;
                in = mvIn;
            }
            inReader = new InputStreamReader(in, kodierung);
            char[] buffer = new char[1024];
            int n;
            long load = 0;
            while (!Config.getStop() && (n = inReader.read(buffer)) != -1) {
                // hier wird andlich geladen
                seite.append(buffer, 0, n);
                load += n;
            }

            if (showLoadTime) {
                stop = new Date();
                if (start != null) {
                    long diff = stop.getTime() - start.getTime();
                    System.out.println("\nDauer: " + diff / 1000 + "," + diff % 1000);
                }
            }
            FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_DATA_BYTE.ordinal(), load);
            FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_BYTE.ordinal(), mvIn.summe);
            switch (ladeArt) {
                case GetUrl.LADE_ART_NIX:
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_NIX.ordinal(), mvIn.summe);
                    break;
                case GetUrl.LADE_ART_DEFLATE:
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_DEFLATE.ordinal(), mvIn.summe);
                    break;
                case GetUrl.LADE_ART_GZIP:
                    FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.SUM_TRAFFIC_LOADART_GZIP.ordinal(), mvIn.summe);
                    break;
                default:
            }
        } catch (IOException ex) {
            if (conn != null) {
                try {
                    InputStream i = conn.getErrorStream();
                    if (i != null) {
                        i.close();
                    }
                    if (inReader != null) {
                        inReader.close();
                    }
                } catch (Exception e) {
                    Log.errorLog(645105987, e, "");
                }
            }
            if (lVersuch) {
                String[] text;
                if (meldung.equals("")) {
                    text = new String[]{"", "Sender: " + sender + " - timout: " + timeout + " Versuche: " + versuch,
                        "URL: " + addr};
                } else {
                    text = new String[]{"", "Sender: " + sender + " - timout: " + timeout + " Versuche: " + versuch,
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
                            final int WARTEN_NO_BUFFER = 2 * 1000;
                            this.wait(WARTEN_NO_BUFFER);
                            FilmeSuchen.listeSenderLaufen.inc(sender, RunSender.Count.NO_BUFFER);
                        } catch (Exception ignored) {
                        }
                        break;
                    default:
                        Log.errorLog(379861049, ex, text);
                        break;
                }
            }
        } catch (Exception ex) {
            Log.errorLog(973969801, ex, "");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                Log.errorLog(696321478, ex, "");
            }
        }
        return seite;
    }

    private class MVInputStream extends InputStream {

        InputStream in = null;
        long summe = 0;
        int nr = 0;

        public MVInputStream(HttpURLConnection con) {
            try {
                if (con != null) {
                    in = con.getInputStream();
                }
            } catch (Exception ignored) {
            }
        }

        public InputStream getInputStream() {
            return in;
        }

        /*public long getSumme() {
         return summe;
         }*/
        @Override
        public int read() throws IOException {
            nr = in.read();
            if (nr != -  1) {
                ++summe;
            }
            return nr;
        }

        @Override
        public int read(byte[] b) throws IOException {
            nr = in.read(b);
            if (nr != -  1) {
                summe += nr;
            }
            return nr;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            nr = in.read(b, off, len);
            if (nr != -1) {
                summe += nr;
            }
            return nr;
        }
    }
}
