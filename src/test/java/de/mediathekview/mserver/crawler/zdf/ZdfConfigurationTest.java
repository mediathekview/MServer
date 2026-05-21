package de.mediathekview.mserver.crawler.zdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ZdfConfigurationTest {

  private ZdfConfiguration target;

  @BeforeEach
  void before() {
    target = new ZdfConfiguration();
  }

  @Test
  void getSearchAuthKeyReturnsDefaultIfSetNotCalled() {
    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_SEARCH), equalTo(target.getSearchAuthKey()));
  }

  @Test
  void getSearchAuthKeyReturnsValueUsedInSetBefore() {
    final String value = "my bearer";

    target.setSearchAuthKey(value);

    assertThat(Optional.of(value), equalTo(target.getSearchAuthKey()));
    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_VIDEO), equalTo(target.getVideoAuthKey()));
  }

  @Test
  void getVideoAuthKeyReturnsDefaultIfSetNotCalled() {
    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_VIDEO), equalTo(target.getVideoAuthKey()));
  }

  @Test
  void getVideoAuthKeyReturnsValueUsedInSetBefore() {
    final String value = "my bearer";

    target.setVideoAuthKey(value);

    assertThat(Optional.of(ZdfConfiguration.AUTH_KEY_SEARCH), equalTo(target.getSearchAuthKey()));
    assertThat(Optional.of(value), equalTo(target.getVideoAuthKey()));
  }
}
