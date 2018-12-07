package de.mediathekview.mserver.crawler.ard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
  
  public ArdUrlBuilder(final String baseUrl, final String clientName) {
    this.baseUrl = baseUrl;
    this.clientName = clientName;
  }
  
  public ArdUrlBuilder addSearchDate(final LocalDateTime date) {
    startDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 0, 0, 0);
    endDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59, 59);
    return this;
  }
  
  public ArdUrlBuilder addClipId(final String id, final String deviceType) {
    this.clipId = id;
    this.deviceType = deviceType;
    return this;
  }
  
  public ArdUrlBuilder addSavedQuery(int queryVersion, final String hashKey) {
    this.queryVersion = queryVersion;
    this.hashKey = hashKey;
    return this;
  }
  
  public String build() {
    return String.format("%s?variables={\"client\":\"%s\"%s}%s", baseUrl, clientName, buildOptionalVariables(), buildExtensionsParameter());
  }
  
  private String buildOptionalVariables() {
    if (clipId != null) {
      return String.format(",\"clipId\":\"%s\",\"deviceType\":\"%s\"", clipId, deviceType);
    }
    if (startDate != null) {
      return String.format(",\"startDateTime\":\"%sZ\",\"endDateTime\":\"%sZ\"", startDate.format(DATE_TIME_FORMATTER), endDate.format(DATE_TIME_FORMATTER));
    }
    
    return "";
  }
  
  private String buildExtensionsParameter() {
    
    if (queryVersion > 0) {
      return String.format("&extensions={\"persistedQuery\":{\"version\":%d,\"sha256Hash\":\"%s\"}}", queryVersion, hashKey);
    }
    
    return "";
  }
}
