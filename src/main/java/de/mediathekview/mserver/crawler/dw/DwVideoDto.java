package de.mediathekview.mserver.crawler.dw;

import java.net.URL;

public class DwVideoDto {

  private int bitrate = 0;
  private String quality = null;
  private String format = null;
  private URL url = null;
  
  public DwVideoDto(int bitrate, String quality, String format, URL url) {
    this.bitrate = bitrate;
    this.quality = quality;
    this.url = url;
    this.format = format;
  }
  
  public int getBitrate() {
    return bitrate;
  }
  
  public String getQuality() {
    return quality;
  }
  
  public URL getUrl() {
    return url;
  }
  
  public String getFormat() {
    return format;
  }
  
  public int getQualityId() {
    if (getQuality().equalsIgnoreCase("high")) {
      return 10;
    } else if (getQuality().equalsIgnoreCase("medium")) {
      return 5;
    }
    return 1;
  }
  
}
