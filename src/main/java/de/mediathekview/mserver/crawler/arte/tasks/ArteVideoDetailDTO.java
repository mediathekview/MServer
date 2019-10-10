package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mlib.daten.Resolution;
import java.util.HashMap;
import java.util.Map;

public class ArteVideoDetailDTO {

  private final Map<Resolution, String> urls;
  private final Map<Resolution, String> urlsWithSubtitle;
  private final Map<Resolution, String> urlsAudioDescription;

  public ArteVideoDetailDTO() {
    urls = new HashMap<>();
    urlsWithSubtitle = new HashMap<>();
    urlsAudioDescription = new HashMap<>();
  }

  public String get(final Object aKey) {
    return urls.get(aKey);
  }

  public String getSubtitle(final Object aKey) {
    return urlsWithSubtitle.get(aKey);
  }

  public String getAudioDescription(final Object aKey) {
    return urlsAudioDescription.get(aKey);
  }

  public Map<Resolution, String> getUrls() {
    return new HashMap<>(urls);
  }

  public Map<Resolution, String> getUrlsWithSubtitle() {
    return urlsWithSubtitle;
  }

  public Map<Resolution, String> getUrlsAudioDescription() {
    return urlsAudioDescription;
  }

  public String put(final Resolution aResolution, final String aUrl) {
    return urls.put(aResolution, aUrl);
  }

  public String putSubtitle(final Resolution aResolution, final String aUrl) {
    return urlsWithSubtitle.put(aResolution, aUrl);
  }

  public String putAudioDescription(final Resolution aResolution, final String aUrl) {
    return urlsAudioDescription.put(aResolution, aUrl);
  }

}
