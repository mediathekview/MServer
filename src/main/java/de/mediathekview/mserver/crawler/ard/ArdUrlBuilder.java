package de.mediathekview.mserver.crawler.ard;

import static org.glassfish.jersey.uri.UriComponent.Type.QUERY_PARAM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.glassfish.jersey.uri.UriComponent;

public class ArdUrlBuilder {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd");

  private final String baseUrl;
  private final String clientName;
  private String clipId;
  private String deviceType;
  private int queryVersion = -1;
  private String hashKey;
  private LocalDateTime startDate;
  private String showId;
  private int pageNumber = -1;

  public ArdUrlBuilder(final String baseUrl, final String clientName) {
    this.baseUrl = baseUrl;
    this.clientName = clientName;
  }

  public ArdUrlBuilder addSearchDate(final LocalDateTime date) {
    startDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 0, 0, 0);
    return this;
  }

  public ArdUrlBuilder addClipId(final String id, final String deviceType) {
    this.clipId = id;
    this.deviceType = deviceType;
    return this;
  }

  public ArdUrlBuilder addShowId(final String id) {
    this.showId = id;
    return this;
  }

  public ArdUrlBuilder addPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
    return this;
  }

  public ArdUrlBuilder addSavedQuery(int queryVersion, final String hashKey) {
    this.queryVersion = queryVersion;
    this.hashKey = hashKey;
    return this;
  }

  public String build() {
    return String.format("%s?%s%s", baseUrl, buildVariables(), buildExtensionsParameter());
  }

  private String buildVariables() {
    String json = String.format("{\"client\":\"%s\"%s}", clientName, buildOptionalVariables());
    return String.format("variables=%s", UriComponent.encode(json, QUERY_PARAM));
  }

  private String buildOptionalVariables() {

    if (clipId != null) {
      return String.format(",\"clipId\":\"%s\",\"deviceType\":\"%s\"", clipId, deviceType);
    }
    if (showId != null) {
      if (pageNumber < 0) {
        return String.format(",\"showId\":\"%s\"", showId);
      }
      return String.format(",\"showId\":\"%s\",\"pageNumber\":%d", showId, pageNumber);
    }
    if (startDate != null) {
      return String.format(",\"startDate\":\"%s\"", startDate.format(DATE_TIME_FORMATTER));
    }

    return "";
  }

  private String buildExtensionsParameter() {

    if (queryVersion > 0) {
      String json = String
          .format("{\"persistedQuery\":{\"version\":%d,\"sha256Hash\":\"%s\"}}", queryVersion,
              hashKey);
      return (String.format("&extensions=%s", UriComponent.encode(json, QUERY_PARAM)));
    }

    return "";
  }
}
