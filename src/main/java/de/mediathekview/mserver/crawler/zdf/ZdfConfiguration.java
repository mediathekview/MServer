package de.mediathekview.mserver.crawler.zdf;

import javax.annotation.Nullable;
import java.util.Optional;

public class ZdfConfiguration {

  @Nullable private String searchAuthKey;
  @Nullable private String videoAuthKey;

  public ZdfConfiguration() {
    searchAuthKey = "5bb200097db507149612d7d983131d06c79706d5";
    videoAuthKey = "20c238b5345eb428d01ae5c748c5076f033dfcc7";
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
