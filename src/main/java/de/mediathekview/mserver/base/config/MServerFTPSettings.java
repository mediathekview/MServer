package de.mediathekview.mserver.base.config;

import java.util.EnumMap;
import java.util.Map;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;

public class MServerFTPSettings {

  private Boolean ftpEnabled;
  private String ftpUrl;
  private Map<FilmlistFormats, String> ftpTargetFilePaths;
  private final Map<FilmlistFormats, String> ftpTargetDiffFilePaths;
  private Integer ftpPort;
  private String ftpUsername;
  private String ftpPassword;

  public MServerFTPSettings() {
    super();
    ftpTargetFilePaths = new EnumMap<>(FilmlistFormats.class);
    ftpTargetDiffFilePaths = new EnumMap<>(FilmlistFormats.class);
    ftpEnabled = false;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MServerFTPSettings other = (MServerFTPSettings) obj;
    if (ftpEnabled == null) {
      if (other.ftpEnabled != null) {
        return false;
      }
    } else if (!ftpEnabled.equals(other.ftpEnabled)) {
      return false;
    }
    if (ftpPassword == null) {
      if (other.ftpPassword != null) {
        return false;
      }
    } else if (!ftpPassword.equals(other.ftpPassword)) {
      return false;
    }
    if (ftpPort == null) {
      if (other.ftpPort != null) {
        return false;
      }
    } else if (!ftpPort.equals(other.ftpPort)) {
      return false;
    }
    if (ftpTargetDiffFilePaths == null) {
      if (other.ftpTargetDiffFilePaths != null) {
        return false;
      }
    } else if (!ftpTargetDiffFilePaths.equals(other.ftpTargetDiffFilePaths)) {
      return false;
    }
    if (ftpTargetFilePaths == null) {
      if (other.ftpTargetFilePaths != null) {
        return false;
      }
    } else if (!ftpTargetFilePaths.equals(other.ftpTargetFilePaths)) {
      return false;
    }
    if (ftpUrl == null) {
      if (other.ftpUrl != null) {
        return false;
      }
    } else if (!ftpUrl.equals(other.ftpUrl)) {
      return false;
    }
    if (ftpUsername == null) {
      if (other.ftpUsername != null) {
        return false;
      }
    } else if (!ftpUsername.equals(other.ftpUsername)) {
      return false;
    }
    return true;
  }

  public Boolean getFtpEnabled() {
    return ftpEnabled;
  }

  public String getFtpPassword() {
    return ftpPassword;
  }

  public Integer getFtpPort() {
    return ftpPort;
  }

  public Map<FilmlistFormats, String> getFtpTargetDiffFilePaths() {
    return ftpTargetDiffFilePaths;
  }

  public Map<FilmlistFormats, String> getFtpTargetFilePaths() {
    return ftpTargetFilePaths;
  }

  public String getFtpUrl() {
    return ftpUrl;
  }

  public String getFtpUsername() {
    return ftpUsername;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (ftpEnabled == null ? 0 : ftpEnabled.hashCode());
    result = prime * result + (ftpPassword == null ? 0 : ftpPassword.hashCode());
    result = prime * result + (ftpPort == null ? 0 : ftpPort.hashCode());
    result =
        prime * result + (ftpTargetDiffFilePaths == null ? 0 : ftpTargetDiffFilePaths.hashCode());
    result = prime * result + (ftpTargetFilePaths == null ? 0 : ftpTargetFilePaths.hashCode());
    result = prime * result + (ftpUrl == null ? 0 : ftpUrl.hashCode());
    result = prime * result + (ftpUsername == null ? 0 : ftpUsername.hashCode());
    return result;
  }

  public void setFtpEnabled(final Boolean ftpEnabled) {
    this.ftpEnabled = ftpEnabled;
  }

  public void setFtpPassword(final String ftpPassword) {
    this.ftpPassword = ftpPassword;
  }

  public void setFtpPort(final Integer ftpPort) {
    this.ftpPort = ftpPort;
  }

  public void setFtpTargetFilePaths(final Map<FilmlistFormats, String> ftpTargetFilePaths) {
    this.ftpTargetFilePaths = ftpTargetFilePaths;
  }

  public void setFtpUrl(final String ftpUrl) {
    this.ftpUrl = ftpUrl;
  }

  public void setFtpUsername(final String ftpUsername) {
    this.ftpUsername = ftpUsername;
  }
}
