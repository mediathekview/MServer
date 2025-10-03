package de.mediathekview.mserver.crawler.ard.json;

import de.mediathekview.mserver.daten.Resolution;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Video information from {@literal
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash}.
 */
public class ArdVideoInfoDto {

  private final Map<Resolution, String> videoUrls;
  private final Map<Resolution, String> videoUrlsAD;
  private final Map<Resolution, String> videoUrlsDGS;
  private final Map<Resolution, String> videoUrlsOV;
  
  private Set<String> subtitleUrl;

  public ArdVideoInfoDto() {
    videoUrls = new EnumMap<>(Resolution.class);
    videoUrlsAD = new EnumMap<>(Resolution.class);
    videoUrlsDGS = new EnumMap<>(Resolution.class);
    videoUrlsOV = new EnumMap<>(Resolution.class);
    subtitleUrl = new HashSet<>();
  }

  public Resolution getDefaultQuality() {
    if (videoUrls.containsKey(Resolution.NORMAL) || 
        videoUrlsAD.containsKey(Resolution.NORMAL) ||
        videoUrlsDGS.containsKey(Resolution.NORMAL) ||
        videoUrlsOV.containsKey(Resolution.NORMAL)) {
      return Resolution.NORMAL;
    }
    return Stream.of(videoUrls.keySet(), videoUrlsAD.keySet(), videoUrlsDGS.keySet(), videoUrlsOV.keySet())
        .flatMap(Set<Resolution>::stream)
        .findFirst()
        .orElse(Resolution.SMALL);
  }

  public String getDefaultVideoUrl() {
    if (videoUrls.containsKey(getDefaultQuality())) {
      return videoUrls.get(getDefaultQuality());
    } else if (videoUrlsAD.containsKey(getDefaultQuality())) {
      return videoUrlsAD.get(getDefaultQuality());
    } else if (videoUrlsDGS.containsKey(getDefaultQuality())) {
      return videoUrlsDGS.get(getDefaultQuality());
    } else if (videoUrlsOV.containsKey(getDefaultQuality())) {
      return videoUrlsOV.get(getDefaultQuality());
    }
    return Stream.of(videoUrls.values(), videoUrlsAD.values(), videoUrlsDGS.values(), videoUrlsOV.values())
        .flatMap(Collection<String>::stream)
        .findFirst()
        .orElse(null);
    
  }

  public Set<String> getSubtitleUrl() {
    return subtitleUrl;
  }

  public void setSubtitleUrl(final Set<String> subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }

  public Map<Resolution, String> getVideoUrls() {
    return videoUrls;
  }

  public boolean containsResolution(final Resolution key) {
    return videoUrls.containsKey(key);
  }

  public String put(final Resolution key, final String value) {
    return videoUrls.put(key, value);
  }
  
  public void putAll(Map<Resolution, String> entries) {
    for (Entry<Resolution, String> e : entries.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }
  
  public Map<Resolution, String> getVideoUrlsAD() {
    return videoUrlsAD;
  }

  public String putAD(final Resolution key, final String value) {
    return videoUrlsAD.put(key, value);
  }
  
  public void putAllAD(Map<Resolution, String> entries) {
    for (Entry<Resolution, String> e : entries.entrySet()) {
      putAD(e.getKey(), e.getValue());
    }
  }
  
  public Map<Resolution, String> getVideoUrlsDGS() {
    return videoUrlsDGS;
  }

  public String putDGS(final Resolution key, final String value) {
    return videoUrlsDGS.put(key, value);
  }
  
  public void putAllDGS(Map<Resolution, String> entries) {
    for (Entry<Resolution, String> e : entries.entrySet()) {
      putDGS(e.getKey(), e.getValue());
    }
  }
  
  public Map<Resolution, String> getVideoUrlsOV() {
    return videoUrlsOV;
  }

  public String putOV(final Resolution key, final String value) {
    return videoUrlsOV.put(key, value);
  }
  
  public void putAllOV(Map<Resolution, String> entries) {
    for (Entry<Resolution, String> e : entries.entrySet()) {
      putOV(e.getKey(), e.getValue());
    }
  }
}
