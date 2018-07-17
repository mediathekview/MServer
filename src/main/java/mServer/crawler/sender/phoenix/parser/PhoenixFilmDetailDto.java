package mServer.crawler.sender.phoenix.parser;

import java.util.Optional;

public class PhoenixFilmDetailDto {

  private String baseName;
  private String topic;
  private String title;
  private String description;
  private String website;

  public String getBaseName() {
    return baseName;
  }

  public void setBaseName(String baseName) {
    this.baseName = baseName;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Optional<String> getWebsite() {
    if (website == null || website.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(website);
  }

  public void setWebsite(String website) {
    this.website = website;
  }
}
