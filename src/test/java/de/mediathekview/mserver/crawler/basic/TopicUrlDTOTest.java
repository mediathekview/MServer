package de.mediathekview.mserver.crawler.basic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TopicUrlDTOTest {
  
  @Test
  public void equalsTestSameUrlAndTheme() {
    TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Url");
    TopicUrlDTO dto2 = new TopicUrlDTO("Thema", "Url");
    
    assertThat(dto1.equals(dto2), equalTo(true));
  }
  
  @Test
  public void equalsTestSameUrlAndDifferentTheme() {
    TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Url");
    TopicUrlDTO dto2 = new TopicUrlDTO("Anders", "Url");
    
    assertThat(dto1.equals(dto2), equalTo(false));
  }
  
  @Test
  public void equalsTestDifferentUrlAndSameTheme() {
    TopicUrlDTO dto1 = new TopicUrlDTO("Thema", "Urls");
    TopicUrlDTO dto2 = new TopicUrlDTO("Thema", "Url");
    
    assertThat(dto1.equals(dto2), equalTo(false));
  }  
}
