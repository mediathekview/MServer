package mServer.crawler.sender.orfon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OrfHttpClient {
  private static final Logger LOG = LogManager.getLogger(OrfHttpClient.class);
  private static final int THREAD_POOL_SIZE = 1;
  private static final int TIMEOUT = 60;

  protected OkHttpClient client;

  public OrfHttpClient() {
    client =
            new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .callTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(THREAD_POOL_SIZE, 5L, TimeUnit.MINUTES))
                    .build();
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
      LOG.debug("Retry #{} due to {} for {}", retry, httpResponseCode, url);
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
