package mServer.crawler.sender.base;

import java.util.Objects;
import java.util.Optional;

public class FilmUrlInfoDto {

  private final String url;
  private int height;
  private int width;
  private final Optional<String> fileType;

  public FilmUrlInfoDto(final String aUrl) {
    this(aUrl, 0, 0);
  }

  public FilmUrlInfoDto(final String aUrl, final int aWidth, final int aHeight) {
    url = aUrl;
    fileType = UrlUtils.getFileType(aUrl);
    width = aWidth;
    height = aHeight;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FilmUrlInfoDto that = (FilmUrlInfoDto) o;
    return height == that.height
            && width == that.width
            && Objects.equals(url, that.url)
            && Objects.equals(fileType, that.fileType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, height, width, fileType);
  }

  @Override
  public String toString() {
    return "FilmUrlInfoDto{"
            + "url='"
            + url
            + '\''
            + ", height="
            + height
            + ", width="
            + width
            + ", fileType="
            + fileType
            + '}';
  }
}
