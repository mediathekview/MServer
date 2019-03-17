package de.mediathekview.mserver.crawler.basic;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class M3U8DtoTest {

  @Test
  public void equalTestSameObject() {
    final M3U8Dto target = new M3U8Dto("test");

    final boolean actual = target.equals(target);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void equalTestNull() {
    final M3U8Dto target = new M3U8Dto("test");

    @SuppressWarnings({"null", "ObjectEqualsNull"})
    final boolean actual = target.equals(null);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalTestOnlyUrlEqual() {
    final M3U8Dto target = new M3U8Dto("test");
    final M3U8Dto other = new M3U8Dto("test");

    final boolean actual = target.equals(other);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void equalTestOnlyUrlDifferent() {
    final M3U8Dto target = new M3U8Dto("test");
    final M3U8Dto other = new M3U8Dto("test1");

    final boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalTestWithMetaDifferentSize() {
    final M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    final M3U8Dto other = new M3U8Dto("test");

    final boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalTestWithMetaSameSizeDifferentKeys() {
    final M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    final M3U8Dto other = new M3U8Dto("test");
    other.addMeta("y", "test");

    final boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalTestWithMetaSameSizeDifferentValues() {
    final M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    final M3U8Dto other = new M3U8Dto("test");
    other.addMeta("x", "test1");

    final boolean actual = target.equals(other);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void equalTestWithMeta() {
    final M3U8Dto target = new M3U8Dto("test");
    target.addMeta("x", "test");
    target.addMeta("y", "5");
    final M3U8Dto other = new M3U8Dto("test");
    other.addMeta("x", "test");
    other.addMeta("y", "5");

    final boolean actual = target.equals(other);

    assertThat(actual, equalTo(true));
  }
}
