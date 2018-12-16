package mServer.crawler.sender.ard.json;

import java.util.Optional;
import mServer.crawler.sender.base.UrlUtils;

class ArdUrlInfo {

  private final String url;
  private int height;
  private int width;
  private final Optional<String> fileType;

  ArdUrlInfo(final String aUrl) {
    url = aUrl;
    fileType = UrlUtils.getFileType(aUrl);
    width = 0;
    height = 0;
  }

  public String getUrl() {
    return url;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public Optional<String> getFileType() {
    return fileType;
  }

  public void setResolution(final int aWidth, final int aHeight) {
    width = aWidth;
    height = aHeight;
  }
}
