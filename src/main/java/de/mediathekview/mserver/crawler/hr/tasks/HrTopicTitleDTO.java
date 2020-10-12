package de.mediathekview.mserver.crawler.hr.tasks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class HrTopicTitleDTO {
  @Nullable private final String topic;
  @Nullable private final String title;

  public HrTopicTitleDTO(@Nullable final String topic, @Nullable final String title) {
    this.topic = topic;
    this.title = title;
  }

  public Optional<String> getTopic() {
    return Optional.ofNullable(topic);
  }

  public Optional<String> getTitle() {
    return Optional.ofNullable(title);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof HrTopicTitleDTO)) {
      return false;
    }
    final HrTopicTitleDTO that = (HrTopicTitleDTO) o;
    return Objects.equals(getTopic(), that.getTopic())
        && Objects.equals(getTitle(), that.getTitle());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTopic(), getTitle());
  }

  @Override
  public String toString() {
    return "HrTopicTitleDTO{" + "topic='" + topic + '\'' + ", title='" + title + '\'' + '}';
  }
}
