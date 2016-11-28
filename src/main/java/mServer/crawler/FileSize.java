package mServer.crawler;

import java.net.*;
import mSearch.Config;
import mServer.crawler.RunSender;
import mSearch.tool.Log;
import mServer.crawler.FilmeSuchen;

public class FileSize {

    final static int TIMEOUT = 3000; // ms //ToDo evtl. wieder kürzen!!

    public static String laengeString(String url) {
        // liefert die Dateigröße einer URL in MB!!
        // Anzeige der Größe in MB und deshalb: Faktor 1000
        return laengeString(url, "");
    }

    public static String laengeString(String url, String ssender) {
        // liefert die Dateigröße einer URL in MB!!
        // Anzeige der Größe in MiB und deshalb: Faktor 1000
        String groesseStr = "";

        long l = laenge(url, ssender);
        if (l > 1000 * 1000) {
            // größer als 1MiB sonst kann ich mirs sparen
            groesseStr = String.valueOf(l / (1000 * 1000));
        } else if (l > 0) {
            groesseStr = "1";
        }
        return groesseStr;
    }

    public static long laengeLong(String url) {
        // liefert die Dateigröße einer URL in MB!!
        // Anzeige der Größe in MiB und deshalb: Faktor 1000
        long groesse = 0;

        long l = laenge(url, "");
        if (l > 1000 * 1000) {
            // größer als 1MiB sonst kann ich mirs sparen
            groesse = l / (1000 * 1000);
        } else if (l > 0) {
            groesse = 1;
        }
        return groesse;
    }

    private static long laenge(String url, String ssender) {
        // liefert die Dateigröße einer URL in BYTE!
        // oder -1
        long ret = -1;
        int retCode;
        if (!url.toLowerCase().startsWith("http")) {
            return ret;
        }
        FilmeSuchen.listeSenderLaufen.inc(ssender, RunSender.Count.GET_SIZE_SUM);
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", Config.getUserAgent());
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            retCode = conn.getResponseCode();
            if (retCode < 400) {
                ret = conn.getContentLengthLong(); //gibts erst seit jdk 7
            }
            conn.disconnect();

            // dann über eine Proxy
            if (retCode == 403) {
                FilmeSuchen.listeSenderLaufen.inc(ssender, RunSender.Count.GET_SIZE_SUM403);
                if (!crawlerConfig.proxyUrl.isEmpty() && crawlerConfig.proxyPort > 0) {
                    // nur dann verwenden, wenn ein Proxy angegeben
                    try {
                        SocketAddress saddr = new InetSocketAddress(crawlerConfig.proxyUrl, crawlerConfig.proxyPort);
                        Proxy proxy = new Proxy(Proxy.Type.SOCKS, saddr);
                        conn = (HttpURLConnection) new URL(url).openConnection(proxy);
                        conn.setRequestProperty("User-Agent", Config.getUserAgent());
                        conn.setReadTimeout(TIMEOUT);
                        conn.setConnectTimeout(TIMEOUT);
                        ret = conn.getContentLengthLong(); //gibts erst seit jdk 7
                        conn.disconnect();
                        if (ret > 0) {
                            FilmeSuchen.listeSenderLaufen.inc(ssender, RunSender.Count.GET_SIZE_PROXY);
                        }
                    } catch (Exception ex) {
                        ret = -1;
                        Log.errorLog(963215478, ex);
                    }
                }
            }
        } catch (Exception ex) {
            ret = -1;
            if (ex.getMessage().equals("Read timed out")) {
                Log.errorLog(825141452, "Read timed out: " + ssender + " url: " + url);
            } else {
                Log.errorLog(643298301, ex, "url: " + url);
            }
        }
        if (ret < 1000 * 1000) {
            // alles unter 1MB sind Playlisten, ORF: Trailer bei im Ausland gesperrten Filmen, ...
            // dann wars nix
            ret = -1;
        }
        return ret;
    }

}
