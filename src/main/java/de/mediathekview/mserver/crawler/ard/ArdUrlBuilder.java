package de.mediathekview.mserver.crawler.ard;

import static org.glassfish.jersey.uri.UriComponent.Type.QUERY_PARAM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.glassfish.jersey.uri.UriComponent;

public class ArdUrlBuilder {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private final String baseUrl;
  private final String clientName;
  private String clipId;
  private String deviceType;
  private int queryVersion = -1;
  private String hashKey;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String showId;

  public ArdUrlBuilder(final String baseUrl, final String clientName) {
    this.baseUrl = baseUrl;
    this.clientName = clientName;
  }

  public ArdUrlBuilder addSearchDate(final LocalDateTime date) {
    // if start date has time of 00:00:00, the api won't return any result
    LocalDateTime dateAfter = date.plusDays(1);
    startDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 4, 30, 0);
    endDate = LocalDateTime.of(dateAfter.getYear(), dateAfter.getMonth(), dateAfter.getDayOfMonth(), 4, 29, 59);
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
      return String.format(",\"showId\":\"%s\"", showId);
    }
    if (startDate != null) {
      return String.format(",\"startDateTime\":\"%sZ\",\"endDateTime\":\"%sZ\"", startDate.format(DATE_TIME_FORMATTER),
          endDate.format(DATE_TIME_FORMATTER));
    }

    return "";
  }

  private String buildExtensionsParameter() {

    if (queryVersion > 0) {
      String json = String.format("{\"persistedQuery\":{\"version\":%d,\"sha256Hash\":\"%s\"}}", queryVersion, hashKey);
      return (String.format("&extensions=%s", UriComponent.encode(json, QUERY_PARAM)));
    }

    return "";
  }
}
