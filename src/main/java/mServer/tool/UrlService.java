package mServer.tool;

import mSearch.Config;
import mSearch.tool.Log;

import java.net.HttpURLConnection;

/**
 * Contains functions to use on a URL
 */
public class UrlService {

    final static int TIMEOUT = 3000; // ms    

    private final HttpUrlConnectionWrapper urlBuilder;

    public UrlService() {
        urlBuilder = new HttpUrlConnectionWrapper();
    }

    public UrlService(HttpUrlConnectionWrapper urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    /**
     * determines the size of the content of an URL
     *
     * @param url the URL
     * @return the size of the content or -1 if an error occurs
     */
    public long laengeLong(String url) {
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

    private long laenge(String url, String ssender) {
        long ret = -1;
        int retCode;
        if (!url.toLowerCase().startsWith("http")) {
            return ret;
        }
        try {
            HttpURLConnection conn = urlBuilder.openConnection(url);
            conn.setRequestProperty("User-Agent", Config.getUserAgent());
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            retCode = conn.getResponseCode();
            if (retCode < 400) {
                ret = conn.getContentLengthLong(); //gibts erst seit jdk 7
            }
            conn.disconnect();

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
