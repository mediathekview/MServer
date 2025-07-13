package de.mediathekview.mserver.crawler.zdf;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ZdfConfigurationTest {

  private ZdfConfiguration target;

  @Before
  public void before() {
    target = new ZdfConfiguration();
  }

  @Test
  public void getSearchAuthKeyReturnsDefaultIfSetNotCalled() {
    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_SEARCH), equalTo(target.getSearchAuthKey()));
  }

  @Test
  public void getSearchAuthKeyReturnsValueUsedInSetBefore() {
    final String value = "my bearer";

    target.setSearchAuthKey(value);

    assertThat(Optional.of(value), equalTo(target.getSearchAuthKey()));
    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_VIDEO), equalTo(target.getVideoAuthKey()));
  }

  @Test
  public void getVideoAuthKeyReturnsDefaultIfSetNotCalled() {
    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_VIDEO), equalTo(target.getVideoAuthKey()));
  }

  @Test
  public void getVideoAuthKeyReturnsValueUsedInSetBefore() {
    final String value = "my bearer";

    target.setVideoAuthKey(value);

    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_SEARCH), equalTo(target.getSearchAuthKey()));
    assertThat(Optional.of(value), equalTo(target.getVideoAuthKey()));
  }
}
