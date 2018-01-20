package de.mediathekview.mserver.crawler.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Datenstruktur für M3U8-Datei
 */
public class M3U8Dto {

  private final String url;
  private final Map<String, String> meta = new HashMap<>();

  public M3U8Dto(String aUrl) {
    url = aUrl;
  }

  public String getUrl() {
    return url;
  }

  public void addMeta(String key, String value) {
    meta.put(key, value);
  }

  public Optional<String> getMeta(String key) {
    if (!meta.containsKey(key)) {
      return Optional.empty();
    }

    return Optional.of(meta.get(key));
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
    final M3U8Dto other = (M3U8Dto) obj;
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }

    if (meta.size() != other.meta.size()) {
      return false;
    }
    
    for (Entry<String, String> entry : meta.entrySet()) {
      String key = entry.getKey();
      if (!other.meta.containsKey(key)) {
        return false;
      }
      
      if (!other.meta.get(key).equals(entry.getValue())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.url);
    
    for (Map.Entry<String, String> entry : meta.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      hash = 23 * hash + Objects.hashCode(key) + Objects.hashCode(value);
    }
    
    return hash;
  }
}
