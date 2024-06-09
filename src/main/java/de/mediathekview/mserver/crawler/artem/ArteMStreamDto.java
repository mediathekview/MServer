package de.mediathekview.mserver.crawler.artem;

import java.util.Map;
import java.util.Optional;

public class ArteMStreamDto {
  Optional<String> language;
  Optional<String> quality;
  Optional<String> mimeType;
  Optional<String> audioCode;
  Optional<String> url;
  Optional<Map<String,String>> subtitles;
  public ArteMStreamDto(Optional<String> language, Optional<String> quality, Optional<String> mimeType,
      Optional<String> audioCode, Optional<String> url, Optional<Map<String, String>> subtitles) {
    super();
    this.language = language;
    this.quality = quality;
    this.mimeType = mimeType;
    this.audioCode = audioCode;
    this.url = url;
    this.subtitles = subtitles;
  }
  public Optional<String> getLanguage() {
    return language;
  }
  public Optional<String> getQuality() {
    return quality;
  }
  public Optional<String> getMimeType() {
    return mimeType;
  }
  public Optional<String> getAudioCode() {
    return audioCode;
  }
  public Optional<String> getUrl() {
    return url;
  }
  public Optional<Map<String, String>> getSubtitles() {
    return subtitles;
  }
  
  
  
}
