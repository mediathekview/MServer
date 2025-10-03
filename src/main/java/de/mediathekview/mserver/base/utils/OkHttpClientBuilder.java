package de.mediathekview.mserver.base.utils;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class OkHttpClientBuilder {
  private final OkHttpClient.Builder httpClientBuilder;

  OkHttpClientBuilder() {
    httpClientBuilder = new OkHttpClient.Builder();
  }

  OkHttpClientBuilder withConnectTimeout(final long timeoutInSeconds) {
    httpClientBuilder.connectTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  OkHttpClientBuilder withReadTimeout(final long timeoutInSeconds) {
    httpClientBuilder.readTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  public OkHttpClient build() {
    final OkHttpClient client = httpClientBuilder.build();
    client.dispatcher().setMaxRequests(100);
    return client;
  }
}
