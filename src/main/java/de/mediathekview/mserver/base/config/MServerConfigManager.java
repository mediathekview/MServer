package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigManager;
import de.mediathekview.mlib.daten.Sender;

/**
 * A {@link ConfigManager} for {@link MServerConfigDTO}.
 */
public class MServerConfigManager extends ConfigManager<MServerConfigDTO>
{
    private static final String DEFAULT_CONFIG_FILE = "MServer-Config.yaml";
    private static MServerConfigManager instance;

    public static MServerConfigManager getInstance()
    {
        if (instance == null)
        {
            instance = new MServerConfigManager();
        }
        return instance;
    }


    private MServerConfigManager()
    {
        super();
    }

    /**
     * Loads the {@link Sender} specific configuration and if it not exist the default configuration.
     *
     * @param aSender The {@link Sender} for which to load the configuration.
     * @return The {@link Sender} specific configuration and if it not exist the default configuration.
     */
    public MServerBasicConfigDTO getConfig(Sender aSender)
    {
        return getConfig().getSenderConfigurations().containsKey(aSender) ? getConfig().getSenderConfigurations().get(aSender) : getConfig();
    }

    @Override
    public String getConfigFileName()
    {
        return DEFAULT_CONFIG_FILE;
    }



    @Override
    protected Class<MServerConfigDTO> getConfigClass()
    {
        return MServerConfigDTO.class;
    }
}
