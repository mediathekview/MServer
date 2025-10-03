package de.mediathekview.mserver.base.config;

import java.util.EnumMap;
import java.util.Map;
import de.mediathekview.mserver.filmlisten.FilmlistFormats;

public class MServerCopySettings {

  private Boolean copyEnabled;
  private Map<FilmlistFormats, String> copyTargetFilePaths;
  private Map<FilmlistFormats, String> copyTargetDiffFilePaths;

  public MServerCopySettings() {
    super();
    copyTargetFilePaths = new EnumMap<>(FilmlistFormats.class);
    copyTargetDiffFilePaths = new EnumMap<>(FilmlistFormats.class);
    copyEnabled = false;
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
    final MServerCopySettings other = (MServerCopySettings) obj;
    if (copyEnabled == null) {
      if (other.copyEnabled != null) {
        return false;
      }
    } else if (!copyEnabled.equals(other.copyEnabled)) {
      return false;
    }
    if (copyTargetDiffFilePaths == null) {
      if (other.copyTargetDiffFilePaths != null) {
        return false;
      }
    } else if (!copyTargetDiffFilePaths.equals(other.copyTargetDiffFilePaths)) {
      return false;
    }
    if (copyTargetFilePaths == null) {
      if (other.copyTargetFilePaths != null) {
        return false;
      }
    } else if (!copyTargetFilePaths.equals(other.copyTargetFilePaths)) {
      return false;
    }
    return true;
  }

  public Boolean getCopyEnabled() {
    return copyEnabled;
  }

  public Map<FilmlistFormats, String> getCopyTargetDiffFilePaths() {
    return copyTargetDiffFilePaths;
  }

  public Map<FilmlistFormats, String> getCopyTargetFilePaths() {
    return copyTargetFilePaths;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (copyEnabled == null ? 0 : copyEnabled.hashCode());
    result =
        prime * result + (copyTargetDiffFilePaths == null ? 0 : copyTargetDiffFilePaths.hashCode());
    result = prime * result + (copyTargetFilePaths == null ? 0 : copyTargetFilePaths.hashCode());
    return result;
  }

  public void setCopyEnabled(final Boolean aCopyEnabled) {
    copyEnabled = aCopyEnabled;
  }

  public void setCopyTargetDiffFilePaths(
      final Map<FilmlistFormats, String> aCopyTargetDiffFilePaths) {
    copyTargetDiffFilePaths = aCopyTargetDiffFilePaths;
  }

  public void setCopyTargetFilePaths(final Map<FilmlistFormats, String> aCopyTargetFilePaths) {
    copyTargetFilePaths = aCopyTargetFilePaths;
  }

}
