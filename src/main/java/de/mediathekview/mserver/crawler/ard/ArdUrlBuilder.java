package de.mediathekview.mserver.crawler.ard;

public class ArdUrlBuilder {

  private final String baseUrl;
  private final String clientName;
  private String clipId;
  private String deviceType;
  private int queryVersion = -1;
  private String hashKey;
  
  public ArdUrlBuilder(final String baseUrl, final String clientName) {
    this.baseUrl = baseUrl;
    this.clientName = clientName;
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
    
    return "";
  }
  
  private String buildExtensionsParameter() {
    
    if (queryVersion > 0) {
      return String.format("&extensions={\"persistedQuery\":{\"version\":%d,\"sha256Hash\":\"%s\"}}", queryVersion, hashKey);
    }
    
    return "";
  }
}
