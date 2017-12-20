package de.mediathekview.mserver.base.config;

import org.apache.commons.lang3.StringUtils;
import de.mediathekview.mlib.config.ConfigManager;
import de.mediathekview.mlib.daten.Sender;

/**
 * A {@link ConfigManager} for {@link MServerConfigDTO}.
 */
public class MServerConfigManager extends ConfigManager<MServerConfigDTO>
{
    public static final String DEFAULT_CONFIG_FILE = "MServer-Config.yaml";
    private static MServerConfigManager instance;
    private String configFileName;
    
    public static MServerConfigManager getInstance(String fileName) {
        if(null == instance) {
           instance = new MServerConfigManager(fileName);
        }
        return instance;
    }
    
    public static MServerConfigManager getInstance() {
        return getInstance(DEFAULT_CONFIG_FILE);
    }

    
    private MServerConfigManager(String fileName)
    {
        super();
        this.configFileName = fileName;
        readClasspathConfig();
    }

    private MServerConfigManager()
    {
        this(DEFAULT_CONFIG_FILE);
    }

    /**
     * Loads the {@link Sender} specific configuration and if it not exist the default configuration.
     *
     * @param aSender The {@link Sender} for which to load the configuration.
     * @return The {@link Sender} specific configuration and if it not exist the default configuration.
     */
    public MServerBasicConfigDTO getSenderConfig(Sender aSender)
    {
        return getConfig().getSenderConfigurations().containsKey(aSender) ? getConfig().getSenderConfigurations().get(aSender) : getConfig();
    }

    @Override
    public String getConfigFileName()
    {
        return StringUtils.isNotEmpty(this.configFileName) ? configFileName : DEFAULT_CONFIG_FILE;
    }

    @Override
    protected Class<MServerConfigDTO> getConfigClass()
    {
        return MServerConfigDTO.class;
    }
}
