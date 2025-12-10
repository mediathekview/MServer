package mServer.crawler.sender.base;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Helper Class to get rid of static method call for better testability
 */
public class JsoupConnection {

  private final OkHttpClient client;

  @Deprecated
  public JsoupConnection() { client = null; }

  public JsoupConnection(final int timeout, final int threadPoolSize) {
    client =
            new OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .callTimeout(timeout, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(threadPoolSize, 5L, TimeUnit.MINUTES))
                    .build();
  }

  public Connection getConnection(String url) {
    return Jsoup.connect(url);
  }

  public Document getDocument(String url) throws IOException {
    return getConnection(url).get();
  }

  public Document getDocumentTimeoutAfter(String url, int timeoutInMilliseconds) throws IOException {
    return getConnection(url).timeout(timeoutInMilliseconds).get();
  }

  public Document getDocumentTimeoutAfterAlternativeDocumentType(String url, int timeoutInMilliseconds, Parser parser) throws IOException {
    return getConnection(url).timeout(timeoutInMilliseconds).parser(parser).get();
  }

  /**
   * Request an url and receive the body as String. Add headers as a string map.
   * @param url
   * @param headerMap
   * @return
   * @throws IOException
   */
  public String requestBodyAsString(final String url, final Map<String, String> headerMap) throws IOException {
    int retry = 0;
    int httpResponseCode;
    final String responseString = "";
    do {
      okhttp3.Headers.Builder headerBuilder = new Headers.Builder();
      if (headerMap != null) {
        for (Map.Entry<String, String> headerValue : headerMap.entrySet()) {
          headerBuilder.add(headerValue.getKey(), headerValue.getValue());
        }
      }
      Request request = new Request.Builder()
              .url(url)
              .headers(headerBuilder.build())
              .build();

      try (final Response response = client.newCall(request).execute()) {
        httpResponseCode = response.code();
        if (response.body() == null || httpResponseCode == 404 || httpResponseCode == 410) {
          break;
        }
        if (response.isSuccessful()) {
          final ResponseBody responseBody = response.body();
          return responseBody == null ? "" : responseBody.string();
        }
      }
      retry++;
    } while (retry < 3);
    return responseString;
  }

  /**
   * Request an url and receive the body as HTML JSOUP Document
   *
   * @param url The url to request.
   * @return request body as HTML JSOUP Document
   * @throws IOException If no connection to the url could be opened.
   */
  public JsonElement requestBodyAsJsonElement(final String url, final Map<String, String> headerMap) throws IOException {
    return new Gson().fromJson(requestBodyAsString(url, headerMap), JsonElement.class);
  }
}
