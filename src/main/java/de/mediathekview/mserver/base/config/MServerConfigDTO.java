package de.mediathekview.mserver.base.config;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import de.mediathekview.mlib.config.ConfigDTO;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;

/**
 * A POJO with the configs for MServer.
 */
public class MServerConfigDTO extends MServerBasicConfigDTO implements ConfigDTO {
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

  private Set<Sender> senderExcluded;
  private Set<Sender> senderIncluded;

  private Map<String, ScheduleDTO> schedules;

  private Set<FilmlistFormats> filmlistSaveFormats;
  private Map<FilmlistFormats, String> filmlistSavePaths;
  private FilmlistFormats filmlistImportFormat;
  private String filmlistImportLocation;

  private MServerFTPSettings ftpSettings;
  private MServerLogSettingsDTO logSettings;

  public MServerConfigDTO() {
    senderConfigurations = new EnumMap<>(Sender.class);
    senderExcluded = new HashSet<>();
    senderIncluded = new HashSet<>();
    filmlistSaveFormats = new HashSet<>();
    schedules = new HashMap<>();
    filmlistSavePaths = new EnumMap<>(FilmlistFormats.class);
    ftpSettings = new MServerFTPSettings();
    logSettings = new MServerLogSettingsDTO();

    maximumCpuThreads = 80;
    maximumServerDurationInMinutes = 0;
    filmlistSaveFormats.add(FilmlistFormats.JSON);
    filmlistSaveFormats.add(FilmlistFormats.OLD_JSON);
    filmlistSaveFormats.add(FilmlistFormats.JSON_COMPRESSED);
    filmlistSaveFormats.add(FilmlistFormats.OLD_JSON_COMPRESSED);

    filmlistSavePaths.put(FilmlistFormats.JSON, "filmliste.json");
    filmlistSavePaths.put(FilmlistFormats.OLD_JSON, "filmliste_old.json");
    filmlistSavePaths.put(FilmlistFormats.JSON_COMPRESSED, "filmliste.json.xz");
    filmlistSavePaths.put(FilmlistFormats.OLD_JSON_COMPRESSED, "filmliste_old.json.xz");
    filmlistImportFormat = FilmlistFormats.OLD_JSON_COMPRESSED;
    filmlistImportLocation = "https://verteiler1.mediathekview.de/Filmliste-akt.xz";
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof MServerConfigDTO)) {
      return false;
    }
    final MServerConfigDTO other = (MServerConfigDTO) obj;
    if (filmlistImportFormat != other.filmlistImportFormat) {
      return false;
    }
    if (filmlistImportLocation == null) {
      if (other.filmlistImportLocation != null) {
        return false;
      }
    } else if (!filmlistImportLocation.equals(other.filmlistImportLocation)) {
      return false;
    }
    if (schedules == null) {
      if (other.schedules != null) {
        return false;
      }
    } else if (!schedules.equals(other.schedules)) {
      return false;
    }
    if (filmlistSaveFormats == null) {
      if (other.filmlistSaveFormats != null) {
        return false;
      }
    } else if (!filmlistSaveFormats.equals(other.filmlistSaveFormats)) {
      return false;
    }
    if (filmlistSavePaths == null) {
      if (other.filmlistSavePaths != null) {
        return false;
      }
    } else if (!filmlistSavePaths.equals(other.filmlistSavePaths)) {
      return false;
    }
    if (ftpSettings == null) {
      if (other.ftpSettings != null) {
        return false;
      }
    } else if (!ftpSettings.equals(other.ftpSettings)) {
      return false;
    }
    if (logSettings == null) {
      if (other.logSettings != null) {
        return false;
      }
    } else if (!logSettings.equals(other.logSettings)) {
      return false;
    }
    if (maximumCpuThreads == null) {
      if (other.maximumCpuThreads != null) {
        return false;
      }
    } else if (!maximumCpuThreads.equals(other.maximumCpuThreads)) {
      return false;
    }
    if (maximumServerDurationInMinutes == null) {
      if (other.maximumServerDurationInMinutes != null) {
        return false;
      }
    } else if (!maximumServerDurationInMinutes.equals(other.maximumServerDurationInMinutes)) {
      return false;
    }
    if (senderConfigurations == null) {
      if (other.senderConfigurations != null) {
        return false;
      }
    } else if (!senderConfigurations.equals(other.senderConfigurations)) {
      return false;
    }
    if (senderExcluded == null) {
      if (other.senderExcluded != null) {
        return false;
      }
    } else if (!senderExcluded.equals(other.senderExcluded)) {
      return false;
    }
    if (senderIncluded == null) {
      if (other.senderIncluded != null) {
        return false;
      }
    } else if (!senderIncluded.equals(other.senderIncluded)) {
      return false;
    }
    return true;
  }

  public FilmlistFormats getFilmlistImportFormat() {
    return filmlistImportFormat;
  }

  public String getFilmlistImportLocation() {
    return filmlistImportLocation;
  }

  public Set<FilmlistFormats> getFilmlistSaveFormats() {
    return filmlistSaveFormats;
  }

  public Map<FilmlistFormats, String> getFilmlistSavePaths() {
    return filmlistSavePaths;
  }

  public MServerFTPSettings getFtpSettings() {
    return ftpSettings;
  }

  public MServerLogSettingsDTO getLogSettings() {
    return logSettings;
  }

  public Integer getMaximumCpuThreads() {
    return maximumCpuThreads;
  }

  public Integer getMaximumServerDurationInMinutes() {
    return maximumServerDurationInMinutes;
  }

  public Map<String, ScheduleDTO> getSchedules() {
    return schedules;
  }

  public Map<Sender, MServerBasicConfigDTO> getSenderConfigurations() {
    return senderConfigurations;
  }

  public Set<Sender> getSenderExcluded() {
    return senderExcluded;
  }

  public Set<Sender> getSenderIncluded() {
    return senderIncluded;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (filmlistImportFormat == null ? 0 : filmlistImportFormat.hashCode());
    result =
        prime * result + (filmlistImportLocation == null ? 0 : filmlistImportLocation.hashCode());
    result = prime * result + (schedules == null ? 0 : schedules.hashCode());
    result = prime * result + (filmlistSaveFormats == null ? 0 : filmlistSaveFormats.hashCode());
    result = prime * result + (filmlistSavePaths == null ? 0 : filmlistSavePaths.hashCode());
    result = prime * result + (ftpSettings == null ? 0 : ftpSettings.hashCode());
    result = prime * result + (logSettings == null ? 0 : logSettings.hashCode());
    result = prime * result + (maximumCpuThreads == null ? 0 : maximumCpuThreads.hashCode());
    result = prime * result
        + (maximumServerDurationInMinutes == null ? 0 : maximumServerDurationInMinutes.hashCode());
    result = prime * result + (senderConfigurations == null ? 0 : senderConfigurations.hashCode());
    result = prime * result + (senderExcluded == null ? 0 : senderExcluded.hashCode());
    result = prime * result + (senderIncluded == null ? 0 : senderIncluded.hashCode());
    return result;
  }

  public boolean hasSchedules() {
    return schedules != null && !schedules.isEmpty();
  }

  public void setFilmlistImportFormat(final FilmlistFormats filmlistImportFormat) {
    this.filmlistImportFormat = filmlistImportFormat;
  }

  public void setFilmlistImportLocation(final String filmlistImportLocation) {
    this.filmlistImportLocation = filmlistImportLocation;
  }

  public void setFilmlistSaveFormats(final Set<FilmlistFormats> filmlistSaveFormats) {
    this.filmlistSaveFormats = filmlistSaveFormats;
  }

  public void setFilmlistSavePaths(final Map<FilmlistFormats, String> filmlistSavePaths) {
    this.filmlistSavePaths = filmlistSavePaths;
  }

  public void setFtpSettings(final MServerFTPSettings ftpSettings) {
    this.ftpSettings = ftpSettings;
  }

  public void setLogSettings(final MServerLogSettingsDTO logSettings) {
    this.logSettings = logSettings;
  }

  public void setMaximumCpuThreads(final Integer aMaximumCpuThreads) {
    maximumCpuThreads = aMaximumCpuThreads;
  }

  public void setMaximumServerDurationInMinutes(final Integer aMaximumServerDurationInMinutes) {
    maximumServerDurationInMinutes = aMaximumServerDurationInMinutes;
  }

  public void setSchedules(final Map<String, ScheduleDTO> schedules) {
    this.schedules = schedules;
  }

  public void setSenderConfigurations(
      final Map<Sender, MServerBasicConfigDTO> aSenderConfigurations) {
    senderConfigurations = aSenderConfigurations;
  }

  public void setSenderExcluded(final Set<Sender> senderExcluded) {
    this.senderExcluded = senderExcluded;
  }

  public void setSenderIncluded(final Set<Sender> senderIncluded) {
    this.senderIncluded = senderIncluded;
  }

}
