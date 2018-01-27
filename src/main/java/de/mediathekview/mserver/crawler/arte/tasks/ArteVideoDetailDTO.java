package de.mediathekview.mserver.crawler.arte.tasks;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import de.mediathekview.mlib.daten.Resolution;

public class ArteVideoDetailDTO {
  private final Map<Resolution, URL> urls;

  public ArteVideoDetailDTO() {
    urls = new HashMap<>();
  }

  public URL get(final Object aKey) {
    return urls.get(aKey);
  }

  public Map<Resolution, URL> getUrls() {
    return new HashMap<>(urls);
  }

  public URL put(final Resolution aResolution, final URL aUrl) {
    return urls.put(aResolution, aUrl);
  }

  public void putAll(final Map<? extends Resolution, ? extends URL> aUrls) {
    urls.putAll(aUrls);
  }

}
