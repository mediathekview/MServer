package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mlib.daten.Resolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/** Datenstruktur für M3U8-Datei */
public class M3U8Dto {

  private static final Logger LOG = LogManager.getLogger(M3U8Dto.class);

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

  public Optional<Resolution> getResolution() {
    Optional<String> codecMeta = getMeta(M3U8Constants.M3U8_CODECS);
    Optional<String> resolution = getMeta(M3U8Constants.M3U8_RESOLUTION);

    // Codec muss "avcl" beinhalten, sonst ist es kein Video
    if (codecMeta.isPresent() && !codecMeta.get().contains("avc1")) {
      return Optional.empty();
    }

    // Auflösung verwenden, wenn vorhanden
    if (resolution.isPresent()) {
      switch (resolution.get()) {
        case "192x144":
        case "240x180":
        case "256x144":
        case "288x216":
        case "320x180":
        case "320x240":
        case "360x270":
        case "384x288":
        case "424x240":
        case "426x240":
        case "442x240":
        case "480x240":
        case "480x270":
        case "480x272":
        case "480x320":
        case "480x360":
        case "512x288":
          return Optional.of(Resolution.SMALL);
        case "636x360":
        case "640x360":
        case "640x480":
        case "664x360":
        case "720x360":
        case "720x540":
        case "720x544":
        case "768x576":
        case "852x480":
        case "960x540":
        case "960x544":
        case "1024x576":
        case "1064x576":
        case "1152x576":
          return Optional.of(Resolution.NORMAL);
        case "1280x720":
        case "1330x720":
        case "1440x720":
        case "1920x1080":
          // todo this resolution is qhd
        case "2560x1440":
          return Optional.of(Resolution.HD);
        case "3840x2160":
          return Optional.of(Resolution.UHD);
        default:
          LOG.debug("Unknown resolution: {}", resolution.get());
      }
    }

    return Optional.empty();
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

  public Optional<String> getNormalizedMeta(String m3u8Resolution) {
    final Optional<String> resolutionMeta = getMeta(m3u8Resolution);
    if (resolutionMeta.isEmpty()) {
      return Optional.empty();
    }

    final String metaValue = resolutionMeta.get();
    final int index = metaValue.indexOf('x');
    if (index == 4) {
      return resolutionMeta;
    }
    return Optional.of(
        String.format("0%sx0%s", metaValue.substring(0, index), metaValue.substring(index + 1)));
  }
}
