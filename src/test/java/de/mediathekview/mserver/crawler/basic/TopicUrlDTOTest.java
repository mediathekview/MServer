package de.mediathekview.mserver.crawler.basic;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TopicUrlDTOTest {

  @Test
  void equalsTestSameUrlAndTheme() {
    final TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Url");
    final TopicUrlDTO dto2 = new TopicUrlDTO("Thema", "Url");

    assertThat(dto1.equals(dto2), equalTo(true));
  }

  @Test
  void equalsTestSameUrlAndDifferentTheme() {
    final TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Url");
    final TopicUrlDTO dto2 = new TopicUrlDTO("Anders", "Url");

    assertThat(dto1.equals(dto2), equalTo(false));
  }

  @Test
  void equalsTestDifferentUrlAndSameTheme() {
    final TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Urls");
    final TopicUrlDTO dto2 = new TopicUrlDTO("Thema", "Url");

    assertThat(dto1.equals(dto2), equalTo(false));
  }
}
