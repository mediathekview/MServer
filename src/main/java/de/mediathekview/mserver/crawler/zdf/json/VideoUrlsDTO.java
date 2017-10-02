package de.mediathekview.mserver.crawler.zdf.json;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import de.mediathekview.mlib.daten.Resolution;

public class VideoUrlsDTO implements Comparable<VideoUrlsDTO> {
  private Map<Resolution, String> urls;

  public VideoUrlsDTO() {
    super();
    urls = new EnumMap<>(Resolution.class);
  }

  @Override
  public int compareTo(final VideoUrlsDTO aO) {
    // TODO Auto-generated method stub
    return 0;
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
    final VideoUrlsDTO other = (VideoUrlsDTO) obj;
    if (urls == null) {
      if (other.urls != null) {
        return false;
      }
    } else if (!urls.equals(other.urls)) {
      return false;
    }
    return true;
  }

  public Optional<Resolution> getHighestResolution() {
    return urls.keySet().stream().sorted(this::compareResolutions).limit(1).findFirst();
  }

  public Map<Resolution, String> getUrls() {
    return urls;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (urls == null ? 0 : urls.hashCode());
    return result;
  }

  public void setUrls(final Map<Resolution, String> aUrls) {
    urls = aUrls;
  }

  private int compareResolutions(final Resolution res1, final Resolution res2) {
    return Integer.compare(res1.getResolutionSize(), res2.getResolutionSize());
  }

}
