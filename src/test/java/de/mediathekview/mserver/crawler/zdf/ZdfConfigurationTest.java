package de.mediathekview.mserver.crawler.zdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class ZdfConfigurationTest {

  private ZdfConfiguration target;

  @Before
  public void before() {
    target = new ZdfConfiguration();
  }

  @Test
  public void getSearchAuthKeyReturnsEmptyOptionalIfSetNotCalled() {
    assertThat(Optional.empty(), equalTo(target.getSearchAuthKey()));
  }

  @Test
  public void getSearchAuthKeyReturnsValueUsedInSetBefore() {
    final String value = "my bearer";

    target.setSearchAuthKey(value);

    assertThat(Optional.of(value), equalTo(target.getSearchAuthKey()));
    assertThat(Optional.empty(), equalTo(target.getVideoAuthKey()));
  }

  @Test
  public void getVideoAuthKeyReturnssValueUsedInSetBefore() {
    assertThat(Optional.empty(), equalTo(target.getVideoAuthKey()));
  }

  @Test
  public void getVideoAuthKeyReturnsValueUsedInSetBefore() {
    final String value = "my bearer";

    target.setVideoAuthKey(value);

    assertThat(Optional.empty(), equalTo(target.getSearchAuthKey()));
    assertThat(Optional.of(value), equalTo(target.getVideoAuthKey()));
  }
}
