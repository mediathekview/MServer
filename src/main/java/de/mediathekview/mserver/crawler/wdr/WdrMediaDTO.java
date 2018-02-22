package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Optional;

public class WdrMediaDto extends CrawlerUrlDTO {
  
  private Optional<String> audioDescriptionUrl;
  private Optional<String> signLanguageUrl;
  private Optional<String> subtitle;
  
  public WdrMediaDto(String aUrl) {
    super(aUrl);
    audioDescriptionUrl = Optional.empty();
    signLanguageUrl = Optional.empty();
    subtitle = Optional.empty();
  }

  public Optional<String> getAudioDescriptionUrl() {
    return audioDescriptionUrl;
  }
  
  public void setAudioDescriptionUrl(final String aUrl) {
    audioDescriptionUrl = Optional.of(aUrl);
  }

  public Optional<String> getSignLanguageUrl() {
    return signLanguageUrl;
  }
  
  public void setSignLanguageUrl(final String aUrl) {
    signLanguageUrl = Optional.of(aUrl);
  }

  public Optional<String> getSubtitle() {
    return subtitle;
  }
  
  public void setSubtitle(final String aSubtitle) {
    subtitle = Optional.of(aSubtitle);
  }
}
