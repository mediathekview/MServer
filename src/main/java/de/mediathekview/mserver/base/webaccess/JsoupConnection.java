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

  public String requestBodyAsString(String url) throws IOException {
    int retry = 0;
    int httpResponseCode = 0;
    String responseString = "";
    do {
      Request request = new Request.Builder()
          .url(url)
          .build();
      try (final Response response = client.newCall(request).execute();
          final ResponseBody body = response.body()) {  
        httpResponseCode = response.code();
        if (response.body() == null || httpResponseCode == 404 || httpResponseCode == 410) {
          break;
        }
        if (response.isSuccessful() && response.body() != null) {
            return response.body().string();
        }
      }      
      retry++;
      LOG.debug("Retry #{} due to {} for {}", retry, httpResponseCode, url);
    } while (retry < 3);
    return responseString;
  }
  
  public Document requestBodyAsHtmlDocument(String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url));
  }

  public Document requestBodyAsXmlDocument(String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url), url, Parser.xmlParser());
  }

}
