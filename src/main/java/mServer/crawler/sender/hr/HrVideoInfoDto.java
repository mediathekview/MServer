package mServer.crawler.sender.hr;

import java.util.Optional;
import mServer.crawler.sender.ard.ArdVideoDTO;
import mServer.crawler.sender.base.Qualities;

public class HrVideoInfoDto extends ArdVideoDTO {

  private Optional<String> subtitle;

  public HrVideoInfoDto() {
    subtitle = Optional.empty();
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = Optional.of(subtitle);
  }

  public Optional<String> getSubtitle() {
    return subtitle;
  }

  public boolean containsQuality(final Qualities quality) {
    return getVideoUrls().containsKey(quality);
  }
}
