package de.mediathekview.mserver.crawler.mdr.parser;

public class MdrVideoInfo {

  private String fileName;
  private int width;
  private int height;

  public MdrVideoInfo(final String aFileName, final int aWidth, final int aHeight) {

    fileName = aFileName;
    width = aWidth;
    height = aHeight;
  }

  public String getFileName() {
    return fileName;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
