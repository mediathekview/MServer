package de.mediathekview.mserver.crawler.arte.json;

import java.util.Optional;

public class ArteSubtitleLinkDto {
  private Optional<String> arteCode;
  private Optional<String> version;
  private Optional<String> iso6392Code;
  private Optional<String> iso6391Code;
  private Optional<String> label;
  private Optional<String> closedCaptioning;
  private Optional<String> burned;
  private Optional<String> filename;
  public ArteSubtitleLinkDto(Optional<String> arteCode, Optional<String> version, Optional<String> iso6392Code,
      Optional<String> iso6391Code, Optional<String> label, Optional<String> closedCaptioning, Optional<String> burned,
      Optional<String> filename) {
    super();
    this.arteCode = arteCode;
    this.version = version;
    this.iso6392Code = iso6392Code;
    this.iso6391Code = iso6391Code;
    this.label = label;
    this.closedCaptioning = closedCaptioning;
    this.burned = burned;
    this.filename = filename;
  }
  public Optional<String> getArteCode() {
    return arteCode;
  }
  public Optional<String> getVersion() {
    return version;
  }
  public Optional<String> getIso6392Code() {
    return iso6392Code;
  }
  public Optional<String> getIso6391Code() {
    return iso6391Code;
  }
  public Optional<String> getLabel() {
    return label;
  }
  public Optional<String> getClosedCaptioning() {
    return closedCaptioning;
  }
  public Optional<String> getBurned() {
    return burned;
  }
  public Optional<String> getFilename() {
    return filename;
  }
   
  
  
  
}
