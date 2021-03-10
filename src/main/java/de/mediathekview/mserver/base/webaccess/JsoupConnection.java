package de.mediathekview.mserver.base.webaccess;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Helper Class to get rid of static method call for better testability
 */
public class JsoupConnection {
  private static final Logger LOG = LogManager.getLogger(JsoupConnection.class);
  protected OkHttpClient client = null;
  
  
  public JsoupConnection(int timeout) {
    client = new OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .callTimeout(timeout, TimeUnit.SECONDS)
        .build();
  }

  public String getString(String url) throws IOException {
    int retry = 0;
    int httpResponseCode = 0;
    String responseString = "";
    while (retry < 3) {
      Request request = new Request.Builder()
          .url(url)
          .build();
      try (final Response response = client.newCall(request).execute();
          final ResponseBody body = response.body()) {  
        httpResponseCode = response.code();
        if (response.isSuccessful()) {
          if (response.body() != null) {
            responseString = response.body().string();
          }
          break;
        } else if (httpResponseCode == 404 || httpResponseCode == 410) {
          break;
        }
      }      
      retry++;
      LOG.debug("Retry #{} due to {} for {}", retry, httpResponseCode, url);
    }
    return responseString;
  }
  
  public Document getDocument(String url) throws IOException {
    return Jsoup.parse(getString(url));
  }
  
  public Document getDocument(String url, Parser parser) throws IOException {
    return Jsoup.parse(getString(url),url,parser);
  }

}
