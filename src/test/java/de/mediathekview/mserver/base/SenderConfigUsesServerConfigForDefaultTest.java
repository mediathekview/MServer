package de.mediathekview.mserver.base;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import org.junit.Before;
import org.junit.Test;

import static de.mediathekview.mlib.daten.Sender.ARD;
import static de.mediathekview.mlib.daten.Sender.BR;
import static org.assertj.core.api.Assertions.assertThat;

public class SenderConfigUsesServerConfigForDefaultTest {

  private MServerConfigManager configManager;

  @Before
  public void setUp() {
    configManager = new MServerConfigManager("ConfigTest.yaml");
  }

  @Test
  public void senderConfig_NotOverriddenValue_ValueFromRootConfig() {
    configManager.getConfig().setSocketTimeoutInSeconds(42);
    final MServerBasicConfigDTO kikaConfig = configManager.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);

    assertThat(kikaConfig.getSocketTimeoutInSeconds())
        .isEqualTo(configManager.getConfig().getSocketTimeoutInSeconds());
  }

  @Test
  public void
      senderConfig_NotOverriddenRootConfigValueChangedAfterInitialization_NewValueFromRootConfig() {
    final MServerBasicConfigDTO kikaConfig = configManager.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);
    configManager.getConfig().setSocketTimeoutInSeconds(42);

    assertThat(kikaConfig.getSocketTimeoutInSeconds())
        .isEqualTo(configManager.getConfig().getSocketTimeoutInSeconds());
  }

  @Test
  public void senderConfig_OverrideValue_OverriddenValue() {
    configManager.getConfig().setMaximumSubpages(21);
    final MServerBasicConfigDTO kikaConfig = configManager.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);

    assertThat(kikaConfig.getMaximumSubpages())
        .isNotEqualTo(configManager.getConfig().getMaximumSubpages());
  }

  @Test
  public void senderConfig_OverriddenRootConfigValueChangedAfterInitialization_OverriddenValue() {
    final MServerBasicConfigDTO kikaConfig = configManager.getSenderConfig(Sender.KIKA);
    kikaConfig.setMaximumSubpages(42);
    configManager.getConfig().setMaximumSubpages(21);

    assertThat(kikaConfig.getMaximumSubpages())
        .isNotEqualTo(configManager.getConfig().getMaximumSubpages());
  }

  @Test
  public void configFromFile_NotOverridden_ValueFromRootConfig() {
    assertThat(configManager.getSenderConfig(ARD).getMaximumSubpages())
        .isEqualTo(configManager.getConfig().getMaximumSubpages());
  }

  @Test
  public void configFromFile_Overridden_OverriddenValue() {
    assertThat(configManager.getSenderConfig(ARD).getMaximumUrlsPerTask())
        .isNotEqualTo(configManager.getConfig().getMaximumUrlsPerTask());
  }

  @Test
  public void configFromFile_NoDirectConfigForSender_ValueFromRootConfig() {
    assertThat(configManager.getSenderConfig(BR).getMaximumUrlsPerTask())
        .isEqualTo(configManager.getConfig().getMaximumUrlsPerTask());
  }
}
