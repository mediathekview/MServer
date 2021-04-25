package de.mediathekview.mserver.base.webaccess;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;

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
  private static final String PROTOCOL_RTMP = "rtmp";
  private static final String FILE_TYPE_M3U8 = "m3u8";
  protected OkHttpClient client = null;

  public JsoupConnection(int timeout) {
    client = new OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .callTimeout(timeout, TimeUnit.SECONDS)
        .build();
  }

  /**
   * Request an url and receive the body as String
   * @param url
   * @return request body as String
   * @throws IOException
   */
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
        if (response.isSuccessful()) {
            return response.body().string();
        }
      }      
      retry++;
      LOG.debug("Retry #{} due to {} for {}", retry, httpResponseCode, url);
    } while (retry < 3);
    return responseString;
  }

  /**
   * Request an url and receive the body as HTML JSOUP Document
   * @param url
   * @return request body as HTML JSOUP Document
   * @throws IOException
   */
  public Document requestBodyAsHtmlDocument(String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url));
  }

  /**
   * Request an url and receive the body as XML JSOUP Document
   * @param url
   * @return request body as HTML JSOUP Document
   * @throws IOException
   */
  public Document requestBodyAsXmlDocument(String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url), url, Parser.xmlParser());
  }

  /**
   * Try to determine the size of the content of the URL.
   * The size in byte is taken from the HEADER content length field.
   * First we will try to get the size using a HEAD request, if this fails we will try a GET request.
   * 
   * @param url
   * @return size of the response in byte or -1 in case we could not determine the size.
   */
  public Long determineFileSize(String url) {
  	long fileSize = determineFileSize(url, new Request.Builder().url(url).head());
  	if (fileSize == -1) {
  		fileSize = determineFileSize(url, new Request.Builder().url(url).get());
  	}
  	return fileSize;
  }
  
  /**
   * Try to determine the size of the content of the REQUEST.
   * The size in byte is taken from the HEADER content length field.
   * The size of rtmp or m3u8 files is not checked and -1 is returned.
   * 
   * @param url
   * @param requestBuilder
   * @return size of the response in byte as per HEADER CONTENT LENGTH or -1 in case we could not determine the size
   */
  public Long determineFileSize(String url, Request.Builder requestBuilder) {
    long fileSize = -1L;
  	// Cant determine the file size of rtmp and m3u8.
  	if (!url.startsWith(PROTOCOL_RTMP) && !url.endsWith(FILE_TYPE_M3U8)) {
  	  try (final Response response =
  	      client.newCall(requestBuilder.build()).execute()) {
  	    final String contentLengthHeader = response.header(HttpHeaders.CONTENT_LENGTH);
  	    if (contentLengthHeader != null) {
  	      fileSize = Long.parseLong(contentLengthHeader);
  	    }
  	  } catch (final IOException ioException) {
  	    LOG.error(
  	        "Something went wrong determining the file size of {}", url);
  	  }
  	}
  	return fileSize;
  }
  
  /**
   * Try to request a URL resource and return OKHTTP isSuccessful.
   * 
   * @param url
   * @return return true if the request was successfully processed by the server
   */
  public boolean requestUrlExists(String url) {
    boolean exists = false;
    try (final Response response =
        client.newCall(new Request.Builder().url(url).head().build()).execute()) {
      exists = response.isSuccessful();
    } catch (final IOException ioException) {
      LOG.error(
          "Error requeting resource {}", url);
    }
    return exists;
  }
}
