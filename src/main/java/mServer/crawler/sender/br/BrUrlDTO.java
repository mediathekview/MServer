package mServer.crawler.sender.br;

public class BrUrlDTO {
  private final String url;
  private final int width;
  private final String videoProfile;

  public BrUrlDTO(final String aUrl, final int aWidth, final String aVideoProfile) {
    super();
    url = aUrl;
    width = aWidth;
    videoProfile = aVideoProfile;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BrUrlDTO other = (BrUrlDTO) obj;
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }
    if (videoProfile == null) {
      if (other.videoProfile != null) {
        return false;
      }
    } else if (!videoProfile.equals(other.videoProfile)) {
      return false;
    }
    if (width != other.width) {
      return false;
    }
    return true;
  }

  public String getUrl() {
    return url;
  }

  public String getVideoProfile() {
    return videoProfile;
  }

  public int getWidth() {
    return width;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (url == null ? 0 : url.hashCode());
    result = prime * result + (videoProfile == null ? 0 : videoProfile.hashCode());
    result = prime * result + width;
    return result;
  }


}
