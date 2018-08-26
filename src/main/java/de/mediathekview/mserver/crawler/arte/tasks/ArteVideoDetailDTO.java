package de.mediathekview.mserver.crawler.arte.tasks;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import de.mediathekview.mlib.daten.Resolution;

public class ArteVideoDetailDTO {
  private final Map<Resolution, URL> urls;
  private Optional<String> creationDate;

  public ArteVideoDetailDTO() {
    urls = new HashMap<>();
    creationDate = Optional.empty();
  }

  public URL get(final Object aKey) {
    return urls.get(aKey);
  }

  public Optional<String> getCreationDate() {
    return creationDate;
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

  public void setCreationDate(final Optional<String> aCreationDate) {
    creationDate = aCreationDate;
  }

}
