package de.mediathekview.mserver.base.utils;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class FileSizeDeterminer {
  private static final Logger LOG = LogManager.getLogger(FileSizeDeterminer.class);
  private final OkHttpClient client;
  //

  
  public FileSizeDeterminer() {
    this(30L, 30L, 10);
  }

  public FileSizeDeterminer(
      final long connectTimeoutInSeconds, final long readTimeoutInSeconds, final int threadPoolSize) {
    OkHttpClient.Builder b = new OkHttpClient.Builder();
    b.readTimeout(readTimeoutInSeconds,TimeUnit.SECONDS);
    b.connectTimeout(connectTimeoutInSeconds,TimeUnit.SECONDS);
    b.connectionPool(new ConnectionPool(threadPoolSize, 5L, TimeUnit.MINUTES));
    client = b.build();
  }

  public RespoonseInfo getRequestInfo(final String url) {
    return getRequestInfo(url, RequestType.HEAD);
  }

  public RespoonseInfo getRequestInfo(final String url, final RequestType requestType) {
      try (final Response response =
          client.newCall(createRequestBuilderForRequestType(url, requestType).build()).execute()) {
        RespoonseInfo respoonseInfo = new RespoonseInfo(
            parseContentLength(response.header(CONTENT_LENGTH)),
            response.code(),
            response.header(CONTENT_TYPE, ""),
            response.request().url().encodedPath()
            );
        return respoonseInfo;
      } catch (final IOException ioException) {
        LOG.error(
            "Something went wrong determining the file size of \"{}\" with {} request.",
            url,
            requestType);
        if (requestType.equals(RequestType.HEAD)) {
          LOG.info("Retrying the file size determination with GET request.");
          return getRequestInfo(url, RequestType.GET);
        }
      }
    return null;
  }

  @NotNull
  private Request.Builder createRequestBuilderForRequestType(final String url, final RequestType requestType) {
    final Request.Builder requestBuilder;
    switch (requestType) {
      case GET:
        requestBuilder = new Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Language", "en-US,en;q=0.5")
            .addHeader("Accept-Encoding", "gzip, deflate, br");
        break;
      case HEAD:
        requestBuilder = new Request.Builder()
            .url(url)
            .head() // Indicates that this is a HEAD request
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Language", "en-US,en;q=0.5")
            .addHeader("Accept-Encoding", "gzip, deflate, br");
        break;
      default:
        throw new IllegalStateException("Unsupported request type for determining the file size.");
    }
    return requestBuilder;
  }

  private enum RequestType {
    GET,
    HEAD
  }
  
  @NotNull
  private Long parseContentLength(final String contentLengthHeader) {
    try {
      return contentLengthHeader == null ? -1L : Long.parseLong(contentLengthHeader);
    } catch (final NumberFormatException numberFormatException) {
      LOG.error(
          "The Content-Length \"{}\" isn't a valid number.",
          contentLengthHeader,
          numberFormatException);
      return -1L;
    }
  }
  
  public class RespoonseInfo {
    private Long size;
    private int code;
    private String contentType;
    private String path;
    
    public RespoonseInfo(Long size, int code, String contentType, String path) {
      super();
      this.size = size;
      this.code = code;
      this.contentType = contentType;
      this.path = path;
    }
    
    public Long getSize() {
      return size;
    }
    public int getCode() {
      return code;
    }
    public String getContentType() {
      return contentType;
    }
    public String getPath() {
      return path;
    }    
  }

}
