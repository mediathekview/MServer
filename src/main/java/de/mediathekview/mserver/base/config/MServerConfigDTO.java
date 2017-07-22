package de.mediathekview.mserver.base.config;

import de.mediathekview.mlib.config.ConfigDTO;
import de.mediathekview.mlib.daten.Sender;

import java.util.*;

/**
 * A POJO with the configs for MServer.
 */
public class MServerConfigDTO extends MServerBasicConfigDTO implements ConfigDTO
{
    /**
     * The maximum amount of cpu threads to be used.
     */
    private Integer maximumCpuThreads;
    /**
     * The maximum duration in minutes the server should run.<br>
     * If set to 0 the server runs without a time limit.
     */
    private Integer maximumServerDurationInMinutes;
    private Map<Sender, MServerBasicConfigDTO> senderConfigurations;

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

    public Integer getMaximumCpuThreads()
    {
        return maximumCpuThreads;
    }

    public void setMaximumCpuThreads(final Integer aMaximumCpuThreads)
    {
        maximumCpuThreads = aMaximumCpuThreads;
    }

    public Integer getMaximumServerDurationInMinutes()
    {
        return maximumServerDurationInMinutes;
    }

    public void setMaximumServerDurationInMinutes(final Integer aMaximumServerDurationInMinutes)
    {
        maximumServerDurationInMinutes = aMaximumServerDurationInMinutes;
    }

    @Override
    public boolean equals(final Object aO)
    {
        if (this == aO) return true;
        if (aO == null || getClass() != aO.getClass()) return false;
        if (!super.equals(aO)) return false;

        final MServerConfigDTO that = (MServerConfigDTO) aO;

        if (getMaximumCpuThreads() != null ? !getMaximumCpuThreads().equals(that.getMaximumCpuThreads()) : that.getMaximumCpuThreads() != null)
            return false;
        if (getMaximumServerDurationInMinutes() != null ? !getMaximumServerDurationInMinutes().equals(that.getMaximumServerDurationInMinutes()) : that.getMaximumServerDurationInMinutes() != null)
            return false;
        return getSenderConfigurations() != null ? getSenderConfigurations().equals(that.getSenderConfigurations()) : that.getSenderConfigurations() == null;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (getMaximumCpuThreads() != null ? getMaximumCpuThreads().hashCode() : 0);
        result = 31 * result + (getMaximumServerDurationInMinutes() != null ? getMaximumServerDurationInMinutes().hashCode() : 0);
        result = 31 * result + (getSenderConfigurations() != null ? getSenderConfigurations().hashCode() : 0);
        return result;
    }
}
