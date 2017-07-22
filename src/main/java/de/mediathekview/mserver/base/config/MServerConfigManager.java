package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigManager;
import de.mediathekview.mlib.daten.Sender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link ConfigManager} for {@link MServerConfigDTO}.
 */
public class MServerConfigManager extends ConfigManager<MServerConfigDTO>
{
    private static final Logger LOG = LogManager.getLogger(MServerConfigManager.class);
    private static final String DEFAULT_CONFIG_FILE = "MServer-Config.yml";
    private static final String CONFIG_NAME_MSERVER = "MServer";
    private static MServerConfigManager instance;

    public static MServerConfigManager getInstance()
    {
        if (instance == null)
        {
            instance = new MServerConfigManager();
        }
        return instance;
    }

    private Path configFilePath;

    private MServerConfigManager()
    {
        try
        {
            configFilePath = Paths.get(getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE).toURI());
        } catch (URISyntaxException exception)
        {
            LOG.debug("Can't load the default config file path.", exception);
            configFilePath = Paths.get(DEFAULT_CONFIG_FILE);
        }
    }

    public void setConfigFilePath(final Path aConfigFilePath)
    {
        configFilePath = aConfigFilePath;
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
    public Path getConfigFilePath()
    {
        return configFilePath;
    }

    @Override
    protected String getConfigName()
    {
        return CONFIG_NAME_MSERVER;
    }

    @Override
    protected Class<MServerConfigDTO> getConfigClass()
    {
        return MServerConfigDTO.class;
    }
}
