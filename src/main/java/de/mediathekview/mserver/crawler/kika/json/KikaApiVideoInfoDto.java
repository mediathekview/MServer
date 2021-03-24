package de.mediathekview.mserver.crawler.kika.json;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;


public class KikaApiVideoInfoDto {
  //
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Map<Resolution, FilmUrl> videoUrls = new HashMap<Resolution, FilmUrl>();
  private Set<URL> subtitles = new HashSet<URL>();

  public KikaApiVideoInfoDto() {
  }
  
  public void addUrl(Resolution aResolution, FilmUrl aFilmUrl) {
    videoUrls.put(aResolution, aFilmUrl);
  }

  public Map<Resolution, FilmUrl> getVideoUrls() {
    return videoUrls;
  }

  public void add(URL aUrl ) {
    subtitles.add(aUrl);
  }
  
  public Set<URL> getSubtitle() {
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
}
