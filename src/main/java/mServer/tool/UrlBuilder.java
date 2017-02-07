package mServer.tool;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper to create a URL from a string to increase testability
 */
public class UrlBuilder {
    
    public HttpURLConnection openConnection(String url) throws MalformedURLException, IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }
}
