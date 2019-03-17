package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ZdfEntryDtoTest {

  @Test
  public void equalsTestNull() {
    final ZdfEntryDto target = new ZdfEntryDto("test", "hallo");

    boolean actual = target.equals(null);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalsTestDifferentType() {
    final ZdfEntryDto target = new ZdfEntryDto("test", "hallo");
    final CrawlerUrlDTO other = new CrawlerUrlDTO("test");

    boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalsTestSameReference() {
    final ZdfEntryDto target = new ZdfEntryDto("test", "hallo");

    boolean actual = target.equals(target);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void equalsTestSameUrls() {
    final ZdfEntryDto target = new ZdfEntryDto("test", "hallo");
    final ZdfEntryDto other = new ZdfEntryDto("test", "hallo");

    boolean actual = target.equals(other);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void equalsTestDifferentDetailUrl() {
    final ZdfEntryDto target = new ZdfEntryDto("test", "hallo");
    final ZdfEntryDto other = new ZdfEntryDto("test1", "hallo");

    boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalsTestDifferentVideoUrl() {
    final ZdfEntryDto target = new ZdfEntryDto("test", "hallo");
    final ZdfEntryDto other = new ZdfEntryDto("test", "hallo1");

    boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }
}
