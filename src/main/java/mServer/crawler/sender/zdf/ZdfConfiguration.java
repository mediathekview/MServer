package mServer.crawler.sender.zdf;

import java.util.Optional;

public class ZdfConfiguration {

  private Optional<String> searchAuthKey;
  private Optional<String> videoAuthKey;

  public ZdfConfiguration() {
    searchAuthKey = Optional.of("5bb200097db507149612d7d983131d06c79706d5");
    videoAuthKey = Optional.of("20c238b5345eb428d01ae5c748c5076f033dfcc7");
  }

  public Optional<String> getSearchAuthKey() {
    return searchAuthKey;
  }

  public void setSearchAuthKey(final String aAuthKey) {
    searchAuthKey = Optional.of(aAuthKey);
  }

  public Optional<String> getVideoAuthKey() {
    return videoAuthKey;
  }

  public void setVideoAuthKey(final String aAuthKey) {
    videoAuthKey = Optional.of(aAuthKey);
  }
}
