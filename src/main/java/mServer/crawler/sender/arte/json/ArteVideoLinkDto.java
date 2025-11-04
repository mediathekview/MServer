package mServer.crawler.sender.arte.json;

import java.util.Optional;

public class ArteVideoLinkDto {
  private Optional<String> programId;
  private Optional<String> url;
  private Optional<String> quality;
  private Optional<String> audioSlot;
  private Optional<String> audioCode;
  private Optional<String> audioLabel;
  private Optional<String> audioShortLabel;
  private Optional<String> width;
  private Optional<String> height;
  public ArteVideoLinkDto(Optional<String> programId, Optional<String> url, Optional<String> quality, Optional<String> audioSlot, Optional<String> audioCode,
      Optional<String> audioLabel, Optional<String> audioShortLabel, Optional<String> width, Optional<String> height) {
    super();
    this.programId = programId;
    this.url = url;
    this.quality = quality;
    this.audioSlot = audioSlot;
    this.audioCode = audioCode;
    this.audioLabel = audioLabel;
    this.audioShortLabel = audioShortLabel;
    this.width = width;
    this.height = height;
  }
  public Optional<String> getProgramId() {
    return programId;
  }
  public Optional<String> getUrl() {
    return url;
  }
  public Optional<String> getQuality() {
    return quality;
  }
  public Optional<String> getAudioSlot() {
    return audioSlot;
  }
  public Optional<String> getAudioCode() {
    return audioCode;
  }
  public Optional<String> getAudioLabel() {
    return audioLabel;
  }
  public Optional<String> getAudioShortLabel() {
    return audioShortLabel;
  }
  public Optional<String> getWidth() {
    return width;
  }
  public Optional<String> getHeight() {
    return height;
  }
  
  
                   
                   
                   
}
