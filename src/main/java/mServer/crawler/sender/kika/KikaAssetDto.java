package mServer.crawler.sender.kika;

import mServer.crawler.sender.base.Qualities;

import java.util.Objects;
import java.util.Optional;

public class KikaAssetDto {
  Optional<String> videoSubtitle = Optional.empty();
  Optional<String> webvttUrl = Optional.empty();
  Optional<String> profileName = Optional.empty(); // "Video 2014 | MP4 Web L | 16:9 | 960x540"
  Optional<String> fileName = Optional.empty(); //  "FCMS-5858cc7f-3288-4101-b92e-f496f2063a37-31e0be270130_58.mp4"
  Optional<Integer> fileSize = Optional.empty(); //  30301969
  Optional<String> mediaType = Optional.empty(); // null
  Optional<Integer> frameWidth = Optional.empty(); //  960
  Optional<Integer> frameHeight = Optional.empty(); // 540
  Optional<Integer> bitrateVideo = Optional.empty(); //  1800000
  Optional<Integer> bitrateAudio = Optional.empty(); //  192000
  Optional<String> type = Optional.empty(); //  "progressive"
  Optional<String> url = Optional.empty(); // "https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/5/FCMS-5858cc7f-3288-4101-b92e-f496f2063a37-31e0be270130_58.mp4"
  Optional<Qualities> resolution = Optional.empty();
  
  public KikaAssetDto(Optional<String> profileName, Optional<String> fileName, Optional<Integer> fileSize,
      Optional<String> mediaType, Optional<Integer> frameWidth, Optional<Integer> frameHeight,
      Optional<Integer> bitrateVideo, Optional<Integer> bitrateAudio, Optional<String> type, Optional<String> url,
      Optional<String> videoSubtitle, Optional<String> webvttUrl) {
    super();
    this.profileName = profileName;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.mediaType = mediaType;
    this.frameWidth = frameWidth;
    this.frameHeight = frameHeight;
    this.bitrateVideo = bitrateVideo;
    this.bitrateAudio = bitrateAudio;
    this.type = type;
    this.videoSubtitle = videoSubtitle;
    this.webvttUrl = webvttUrl;
    this.url = url;
    if (frameWidth.isPresent()) {
      this.resolution = Optional.of(Qualities.getResolutionFromWidth(frameWidth.get()));
    }
  }
  public Optional<String> getProfileName() {
    return profileName;
  }
  public Optional<String> getFileName() {
    return fileName;
  }
  public Optional<Integer> getFileSize() {
    return fileSize;
  }
  public Optional<String> getMediaType() {
    return mediaType;
  }
  public Optional<Integer> getFrameWidth() {
    return frameWidth;
  }
  public Optional<Integer> getFrameHeight() {
    return frameHeight;
  }
  public Optional<Integer> getBitrateVideo() {
    return bitrateVideo;
  }
  public Optional<Integer> getBitrateAudio() {
    return bitrateAudio;
  }
  public Optional<String> getType() {
    return type;
  }
  public Optional<String> getUrl() {
    return url;
  }
  public Optional<Qualities> getResolution() { return resolution; }
  public Optional<String> getVideoSubtitle() {
    return videoSubtitle;
  }
  public Optional<String> getWebvttUrl() {
    return webvttUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof KikaAssetDto)) return false;
    KikaAssetDto that = (KikaAssetDto) o;
    return Objects.equals(profileName, that.profileName)
        && Objects.equals(fileName, that.fileName)
        && Objects.equals(fileSize, that.fileSize)
        && Objects.equals(mediaType, that.mediaType)
        && Objects.equals(frameWidth, that.frameWidth)
        && Objects.equals(frameHeight, that.frameHeight)
        && Objects.equals(bitrateVideo, that.bitrateVideo)
        && Objects.equals(bitrateAudio, that.bitrateAudio)
        && Objects.equals(type, that.type)
        && Objects.equals(url, that.url)
        && Objects.equals(videoSubtitle, that.videoSubtitle)
        && Objects.equals(webvttUrl, that.webvttUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profileName, fileName, fileSize, mediaType, frameWidth,
        frameHeight, bitrateVideo, bitrateAudio, type, url, videoSubtitle, webvttUrl);
  }

  @Override
  public String toString() {
    return "KikaAssetDto{" +
        "profileName=" + profileName +
        ", fileName=" + fileName +
        ", fileSize=" + fileSize +
        ", mediaType=" + mediaType +
        ", frameWidth=" + frameWidth +
        ", frameHeight=" + frameHeight +
        ", bitrateVideo=" + bitrateVideo +
        ", bitrateAudio=" + bitrateAudio +
        ", type=" + type +
        ", url=" + url +
        ", videoSubtitle=" + videoSubtitle +
        ", webvttUrl=" + webvttUrl +
        '}';
  }
  
}
