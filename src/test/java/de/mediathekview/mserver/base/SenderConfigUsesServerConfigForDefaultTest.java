package de.mediathekview.mserver.base;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class SenderConfigUsesServerConfigForDefaultTest {

  private MServerConfigManager rootConfig;

  @Before
  public void setUp() {
    rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
  }

  @Test
  public void testNotOverriddenSenderConfigUsesServerConfig() {
    rootConfig.getConfig().setSocketTimeoutInSeconds(42);
    final MServerBasicConfigDTO kikaConfig = rootConfig.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);

    assertThat(
        kikaConfig.getSocketTimeoutInSeconds(),
        equalTo(rootConfig.getConfig().getSocketTimeoutInSeconds()));
  }

  @Test
  public void testNotOverriddenSenderConfigUsesServerConfigChangedAfterInitialisation() {
    final MServerBasicConfigDTO kikaConfig = rootConfig.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);
    rootConfig.getConfig().setSocketTimeoutInSeconds(42);

    assertThat(
        kikaConfig.getSocketTimeoutInSeconds(),
        equalTo(rootConfig.getConfig().getSocketTimeoutInSeconds()));
  }

  @Test
  public void testOverriddenSenderConfigDontUsesServerConfig() {
    rootConfig.getConfig().setMaximumSubpages(21);
    final MServerBasicConfigDTO kikaConfig = rootConfig.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);

    assertThat(
        kikaConfig.getMaximumSubpages(), not(equalTo(rootConfig.getConfig().getMaximumSubpages())));
  }

  @Test
  public void testOverriddenSenderConfigDontUsesServerConfigChangedAfterInitialisation() {
    final MServerBasicConfigDTO kikaConfig = rootConfig.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);
    rootConfig.getConfig().setMaximumSubpages(21);

    assertThat(
        kikaConfig.getMaximumSubpages(), not(equalTo(rootConfig.getConfig().getMaximumSubpages())));
  }
}
