package de.mediathekview.mserver.crawler.basic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class M3U8DtoTest {

  @Test
  public void equalTestSameObject() {
    M3U8Dto target = new M3U8Dto("test");
    
    boolean actual = target.equals(target);
    
    assertThat(actual, equalTo(true));
  }
  
  @Test
  public void equalTestNull() {
    M3U8Dto target = new M3U8Dto("test");
    
    @SuppressWarnings({"null", "ObjectEqualsNull"})
    boolean actual = target.equals(null);
    
    assertThat(actual, equalTo(false));
  }
  
  @Test
  public void equalTestOnlyUrlEqual() {
    M3U8Dto target = new M3U8Dto("test");
    M3U8Dto other = new M3U8Dto("test");
    
    boolean actual = target.equals(other);
    
    assertThat(actual, equalTo(true));
  }
  
  @Test
  public void equalTestOnlyUrlDifferent() {
    M3U8Dto target = new M3U8Dto("test");
    M3U8Dto other = new M3U8Dto("test1");
    
    boolean actual = target.equals(other);
    
    assertThat(actual, equalTo(false));
  }
  
  @Test
  public void equalTestWithMetaDifferentSize() {
    M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    M3U8Dto other = new M3U8Dto("test");
    
    boolean actual = target.equals(other);
    
    assertThat(actual, equalTo(false));
  }
  
  @Test
  public void equalTestWithMetaSameSizeDifferentKeys() {
    M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    M3U8Dto other = new M3U8Dto("test");
    other.addMeta("y", "test");
    
    boolean actual = target.equals(other);
    
    assertThat(actual, equalTo(false));
  }
  
  @Test
  public void equalTestWithMetaSameSizeDifferentValues() {
    M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    M3U8Dto other = new M3U8Dto("test");
    other.addMeta("x", "test1");
    
    boolean actual = target.equals(other);
    
    assertThat(actual, equalTo(false));
  }
  
  @Test
  public void equalTestWithMeta() {
    M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    target.addMeta("y", "5");
    M3U8Dto other = new M3U8Dto("test");
    other.addMeta("x", "test");
    other.addMeta("y", "5");    
    
    boolean actual = target.equals(other);
    
    assertThat(actual, equalTo(true));
  }
}
