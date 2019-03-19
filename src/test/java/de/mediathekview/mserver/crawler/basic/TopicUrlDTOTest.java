package de.mediathekview.mserver.crawler.basic;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TopicUrlDTOTest {

  @Test
  public void equalsTestSameUrlAndTheme() {
    final TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Url");
    final TopicUrlDTO dto2 = new TopicUrlDTO("Thema", "Url");

    assertThat(dto1.equals(dto2), equalTo(true));
  }

  @Test
  public void equalsTestSameUrlAndDifferentTheme() {
    final TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Url");
    final TopicUrlDTO dto2 = new TopicUrlDTO("Anders", "Url");

    assertThat(dto1.equals(dto2), equalTo(false));
  }

  @Test
  public void equalsTestDifferentUrlAndSameTheme() {
    final TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Urls");
    final TopicUrlDTO dto2 = new TopicUrlDTO("Thema", "Url");

    assertThat(dto1.equals(dto2), equalTo(false));
  }
}
