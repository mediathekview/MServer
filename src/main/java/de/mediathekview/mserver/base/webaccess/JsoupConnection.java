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
  
  public Document requestBodyAsHtmlDocument(String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url));
  }

  public Document requestBodyAsXmlDocument(String url) throws IOException {
    return Jsoup.parse(requestBodyAsString(url), url, Parser.xmlParser());
  }

  public Long determineFileSize(String url) {
  	long fileSize = determineFileSize(url, new Request.Builder().url(url).head());
  	if (fileSize == -1) {
  		fileSize = determineFileSize(url, new Request.Builder().url(url).get());
  	}
  	return fileSize;
  }
  
  public Long determineFileSize(String url, Request.Builder requestBuilder) {
	long fileSize = -1L;
	// Cant determine the file size of rtmp and m3u8.
	if (!url.startsWith(PROTOCOL_RTMP) && !url.endsWith(FILE_TYPE_M3U8)) {
	  try (final Response response =
	      client.newCall(requestBuilder.build()).execute()) {
	    final String contentLengthHeader = response.header(HttpHeaders.CONTENT_LENGTH);
	    fileSize = Long.parseLong(contentLengthHeader);
	  } catch (final IOException ioException) {
	    LOG.error(
	        "Something went wrong determining the file size of {}", url);
	  }
	}
	return fileSize;
  }
}
