package de.mediathekview.mserver.crawler.zdf;

import javax.annotation.Nullable;
import java.util.Optional;

public class ZdfConfiguration {

  @Nullable private String searchAuthKey;
  @Nullable private String videoAuthKey;

  public ZdfConfiguration() {
    searchAuthKey = null;
    videoAuthKey = null;
  }

  public Optional<String> getSearchAuthKey() {
    return Optional.ofNullable(searchAuthKey);
  }

  public void setSearchAuthKey(@Nullable final String authKey) {
    searchAuthKey = authKey;
  }

  public Optional<String> getVideoAuthKey() {
    return Optional.ofNullable(videoAuthKey);
  }

  public void setVideoAuthKey(@Nullable final String authKey) {
    videoAuthKey = authKey;
  }
}
