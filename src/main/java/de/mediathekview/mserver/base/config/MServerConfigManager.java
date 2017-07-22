package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigManager;

import java.nio.file.Path;

/**
 * A {@link ConfigManager} for {@link MServerConfigDTO}.
 */
public class MServerConfigManager extends ConfigManager<MServerConfigDTO>
{

    @Override
    protected Path getConfigFilePath()
    {
        //TODO
        return null;
    }

    @Override
    protected String getConfigName()
    {
        //TODO
        return "";
    }

    @Override
    protected Class<MServerConfigDTO> getConfigClass()
    {
        return MServerConfigDTO.class;
    }
}
