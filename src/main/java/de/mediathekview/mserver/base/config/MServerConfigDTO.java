package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigDTO;
import de.mediathekview.mlib.daten.Sender;

import java.util.*;

/**
 * A POJO with the configs for MServer.
 */
public class MServerConfigDTO extends MServerBasicConfigDTO implements ConfigDTO
{

    private Map<Sender,MServerBasicConfigDTO> senderConfigurations;

    public MServerConfigDTO()
    {
        senderConfigurations = new EnumMap<>(Sender.class);
    }

    public Map<Sender, MServerBasicConfigDTO> getSenderConfigurations()
    {
        return senderConfigurations;
    }

    public void setSenderConfigurations(final Map<Sender, MServerBasicConfigDTO> aSenderConfigurations)
    {
        senderConfigurations = aSenderConfigurations;
    }

    @Override
    public boolean equals(final Object aO)
    {
        if (this == aO) return true;
        if (aO == null || getClass() != aO.getClass()) return false;
        if (!super.equals(aO)) return false;

        final MServerConfigDTO that = (MServerConfigDTO) aO;

        return getSenderConfigurations() != null ? getSenderConfigurations().equals(that.getSenderConfigurations()) : that.getSenderConfigurations() == null;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (getSenderConfigurations() != null ? getSenderConfigurations().hashCode() : 0);
        return result;
    }
}
