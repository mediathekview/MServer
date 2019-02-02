package de.mediathekview.mserver.crawler.arte.tasks;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import de.mediathekview.mlib.daten.Resolution;

public class ArteVideoDetailDTO {

  private final Map<Resolution, String> urls;

  public ArteVideoDetailDTO() {
    urls = new HashMap<>();
  }

  public String get(final Object aKey) {
    return urls.get(aKey);
  }

  public Map<Resolution, String> getUrls() {
    return new HashMap<>(urls);
  }

  public String put(final Resolution aResolution, final String aUrl) {
    return urls.put(aResolution, aUrl);
  }
}
