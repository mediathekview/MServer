package mServer.crawler.sender.orf;

import java.time.Duration;
import java.util.Optional;

public class OrfEpisodeInfoDTO {

  private final OrfVideoInfoDTO videoInfo;
  private final Optional<String> description;
  private final Optional<Duration> duration;
  private final Optional<String> title;

  public OrfEpisodeInfoDTO(final OrfVideoInfoDTO aVideoInfo,
          final Optional<String> aTitle,
          final Optional<String> aDescription,
          final Optional<Duration> aDuration
  ) {
    title = aTitle;
    description = aDescription;
    duration = aDuration;
    videoInfo = aVideoInfo;
  }

  public OrfVideoInfoDTO getVideoInfo() {
    return videoInfo;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public Optional<Duration> getDuration() {
    return duration;
  }

  public Optional<String> getTitle() {
    return title;
  }
}
