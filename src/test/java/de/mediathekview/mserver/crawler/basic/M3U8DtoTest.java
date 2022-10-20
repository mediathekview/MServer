package de.mediathekview.mserver.crawler.basic;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

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

  @Test
  public void getNormalizedMetaWithoutModification() {
    final M3U8Dto target = new M3U8Dto("test");
    target.addMeta(M3U8Constants.M3U8_RESOLUTION, "1920x1200");

    final Optional<String> actual = target.getNormalizedMeta(M3U8Constants.M3U8_RESOLUTION);

    assertTrue(actual.isPresent());
    assertThat(actual.get(), equalTo("1920x1200"));
  }

  @Test
  public void getNormalizedMetaWithModification() {
    final M3U8Dto target = new M3U8Dto("test");
    target.addMeta(M3U8Constants.M3U8_RESOLUTION, "960x540");

    final Optional<String> actual = target.getNormalizedMeta(M3U8Constants.M3U8_RESOLUTION);

    assertTrue(actual.isPresent());
    assertThat(actual.get(), equalTo("0960x0540"));
  }
}
