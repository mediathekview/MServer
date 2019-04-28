package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigManager;
import de.mediathekview.mlib.daten.Sender;
import org.apache.commons.lang3.StringUtils;

/** A {@link ConfigManager} for {@link MServerConfigDTO}. */
public class MServerConfigManager extends ConfigManager<MServerConfigDTO> {
  public static final String DEFAULT_CONFIG_FILE = "MServer-Config.yaml";
  private static MServerConfigManager instance;
  private final String configFileName;

  private MServerConfigManager(final String fileName) {
    super();
    this.configFileName = fileName;
    readConfig();
  }

  private MServerConfigManager() {
    this(DEFAULT_CONFIG_FILE);
  }

  public static MServerConfigManager getInstance(final String fileName) {
    if (null == instance) {
      instance = new MServerConfigManager(fileName);
    }
    return instance;
  }

  public static MServerConfigManager getInstance() {
    return getInstance(DEFAULT_CONFIG_FILE);
  }

  /** @see MServerConfigDTO#getSenderConfig(Sender). */
  public MServerBasicConfigDTO getSenderConfig(final Sender aSender) {
    return getConfig().getSenderConfig(aSender);
  }

  @Override
  public String getConfigFileName() {
    return StringUtils.isNotEmpty(this.configFileName) ? configFileName : DEFAULT_CONFIG_FILE;
  }

  @Override
  protected Class<MServerConfigDTO> getConfigClass() {
    return MServerConfigDTO.class;
  }
}
