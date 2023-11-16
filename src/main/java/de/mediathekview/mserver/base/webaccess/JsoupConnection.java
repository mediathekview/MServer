package de.mediathekview.mserver.base.webaccess;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;

/** Helper Class to get rid of static method call for better testability */
public class JsoupConnection {
  private static final Logger LOG = LogManager.getLogger(JsoupConnection.class);
  private static final String PROTOCOL_RTMP = "rtmp";
  private static final String FILE_TYPE_M3U8 = "m3u8";
  protected OkHttpClient client;

  public JsoupConnection(final int timeout, final int threadPoolSize) {
    client =
        new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .callTimeout(timeout, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(threadPoolSize, 5L, TimeUnit.MINUTES))
            .build();
  }

  /**
   * Request an url and receive the body as String
   *
   * @param url The url to request.
   * @return request body as String
   * @throws IOException If no connection to the url could be opened.
   */
  public String requestBodyAsString(final String url) throws IOException {
    int retry = 0;
    int httpResponseCode;
    final String responseString = "";
    do {
      final Request request = new Request.Builder().url(url).build();
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
  public Document requestBodyAsHtmlDocument(final String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url));
  }

  /**
   * Request an url and receive the body as XML JSOUP Document
   *
   * @param url The url to request.
   * @return request body as HTML JSOUP Document
   * @throws IOException If no connection to the url could be opened.
   */
  public Document requestBodyAsXmlDocument(final String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url), url, Parser.xmlParser());
  }

  /**
   * Try to determine the size of the content of the URL. The size in byte is taken from the HEADER
   * content length field. First we will try to get the size using a HEAD request, if this fails we
   * will try a GET request.
   *
   * @param url The url to request.
   * @return size of the response in byte or -1 in case we could not determine the size.
   */
  public Long determineFileSize(final String url) {
    long fileSize = determineFileSize(url, new Request.Builder().url(url).head());
    if (fileSize == -1) {
      fileSize = determineFileSize(url, new Request.Builder().url(url).get());
    }
    return fileSize;
  }

  /**
   * Try to determine the size of the content of the REQUEST. The size in byte is taken from the
   * HEADER content length field. The size of rtmp or m3u8 files is not checked and -1 is returned.
   *
   * @param url The url to request.
   * @param requestBuilder Builder to build the http request.
   * @return size of the response in byte as per HEADER CONTENT LENGTH or -1 in case we could not
   *     determine the size
   */
  public Long determineFileSize(final String url, final Request.Builder requestBuilder) {
    long fileSize = -1L;
    // Cant determine the file size of rtmp and m3u8.
    if (!url.startsWith(PROTOCOL_RTMP) && !url.endsWith(FILE_TYPE_M3U8)) {
      try (final Response response = client.newCall(requestBuilder.build()).execute()) {
        final String contentLengthHeader = response.header(CONTENT_LENGTH);
        if (contentLengthHeader != null) {
          fileSize = Long.parseLong(contentLengthHeader);
        }
      } catch (final IOException ioException) {
        LOG.error("Something went wrong determining the file size of {}", url);
      }
    }
    return fileSize;
  }

  /**
   * Try to request a URL resource and return OKHTTP isSuccessful.
   *
   * @param url The url to request.
   * @return return true if the request was successfully processed by the server
   */
  public boolean requestUrlExists(final String url) {
    boolean exists = false;
    try (final Response response =
        client.newCall(new Request.Builder().url(url).head().build()).execute()) {
      exists = response.isSuccessful();
    } catch (final IOException ioException) {
      LOG.error("Error requeting resource {}", url);
    }
    return exists;
  }
}
