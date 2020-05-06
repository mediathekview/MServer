package mServer.crawler.sender.zdf;

import java.util.Optional;

public class ZdfConfiguration {

  private Optional<String> searchAuthKey;
  private Optional<String> videoAuthKey;

  public ZdfConfiguration() {
    searchAuthKey = Optional.empty();
    videoAuthKey = Optional.empty();
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
