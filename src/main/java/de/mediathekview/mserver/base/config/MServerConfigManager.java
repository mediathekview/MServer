package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.daten.Sender;
import org.apache.commons.lang3.StringUtils;

/** A {@link ConfigManager} for {@link MServerConfigDTO}. */
public class MServerConfigManager extends ConfigManager<MServerConfigDTO> {
  public static final String DEFAULT_CONFIG_FILE = "MServer-Config.yaml";
  private final String configFileName;

  public MServerConfigManager(final String fileName) {
    super();
    configFileName = fileName;
  }

  /**
   * @param aSender The {@link Sender} for which the config will be loaded.
   * @return The Sender specific config.
   * @see MServerConfigDTO#getSenderConfig(Sender)
   */
  public MServerBasicConfigDTO getSenderConfig(final Sender aSender) {
    return getConfig().getSenderConfig(aSender);
  }

  @Override
  public String getConfigFileName() {
    return StringUtils.isNotEmpty(configFileName) ? configFileName : DEFAULT_CONFIG_FILE;
  }

  @Override
  protected void initializeConfigAfterRead(final MServerConfigDTO config) {
    config.initializeSenderConfigurations();
  }

  @Override
  protected Class<MServerConfigDTO> getConfigClass() {
    return MServerConfigDTO.class;
  }
}
