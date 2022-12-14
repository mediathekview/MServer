package de.mediathekview.mserver.crawler.kika.json;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.mediathekview.mlib.daten.Resolution;


public class KikaApiVideoInfoDto {
  //
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Map<Resolution, String> videoUrls = new EnumMap<>(Resolution.class);
  private boolean hasSubtitle = false;
  private Set<String> subtitles = new HashSet<>();
 
  public void addUrl(Resolution aResolution, String aFilmUrl) {
    videoUrls.put(aResolution, aFilmUrl);
  }

  public Map<Resolution, String> getVideoUrls() {
    return videoUrls;
  }

  public void addSubtitle(String aUrl ) {
    subtitles.add(aUrl);
  }
  
  public Set<String> getSubtitle() {
    return subtitles;
  }

  public void setError(Optional<String> aErrorCode, Optional<String> aErrorMesssage) {
    errorCode = aErrorCode;
    errorMesssage = aErrorMesssage;
  }
  
  public Optional<String> getErrorMesssage() {
    return errorMesssage;
  }

  public Optional<String> getErrorCode() {
    return errorCode;
  }

  public boolean hasSubtitle() {
    return hasSubtitle;
  }

  public void setSubtitle(boolean hasSub) {
    hasSubtitle = hasSub;
  }
}
