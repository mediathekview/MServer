package de.mediathekview.mserver.base.utils;

import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class MVHttpClient {
  private static final MVHttpClient ourInstance = new MVHttpClient();
  private final OkHttpClient httpClient;
  private final OkHttpClient copyClient;

  private MVHttpClient() {
    httpClient =
        new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(100, 1, TimeUnit.SECONDS))
            .build();
    httpClient.dispatcher().setMaxRequests(100);

    copyClient =
        httpClient
            .newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
            .build();
  }

  public static MVHttpClient getInstance() {
    return ourInstance;
  }

  public OkHttpClient getHttpClient() {
    return httpClient;
  }

  public OkHttpClient getReducedTimeOutClient() {
    return copyClient;
  }
}
