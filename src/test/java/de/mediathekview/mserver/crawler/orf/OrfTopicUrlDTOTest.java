package de.mediathekview.mserver.crawler.orf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OrfTopicUrlDTOTest {
  
  @Test
  public void equalsTestSameUrlAndTheme() {
    OrfTopicUrlDTO dto1 = new OrfTopicUrlDTO("Thema", "Url");
    OrfTopicUrlDTO dto2 = new OrfTopicUrlDTO("Thema", "Url");
    
    assertThat(dto1.equals(dto2), equalTo(true));
  }
  
  @Test
  public void equalsTestSameUrlAndDifferentTheme() {
    OrfTopicUrlDTO dto1 = new OrfTopicUrlDTO("Thema", "Url");
    OrfTopicUrlDTO dto2 = new OrfTopicUrlDTO("Anders", "Url");
    
    assertThat(dto1.equals(dto2), equalTo(false));
  }
  
  @Test
  public void equalsTestDifferentUrlAndSameTheme() {
    OrfTopicUrlDTO dto1 = new OrfTopicUrlDTO("Thema", "Urls");
    OrfTopicUrlDTO dto2 = new OrfTopicUrlDTO("Thema", "Url");
    
    assertThat(dto1.equals(dto2), equalTo(false));
  }  
}
