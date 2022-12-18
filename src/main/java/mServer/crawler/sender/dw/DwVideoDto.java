package mServer.crawler.sender.dw;

public class DwVideoDto {

  private final int bitRate;
  private final String url;
  private final int width;

  public DwVideoDto(String url, int width, int bitRate) {
    this.url = url;
    this.width = width;
    this.bitRate = bitRate;
  }

  public int getBitRate() {
    return bitRate;
  }

  public String getUrl() {
    return url;
  }

  public int getWidth() {
    return width;
  }
}
