package de.mediathekview.mserver.base.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Version {
  private static final Logger LOG = LogManager.getLogger(Version.class);

  private int major;
  private int minor;
  private int patch;

  public Version() {
    major = 0;
    minor = 0;
    patch = 0;
  }

  public Version(final int major, final int minor, final int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public Version(final String versionsstring) {
    final String[] versions = versionsstring.replace("-SNAPSHOT", "").split("\\.");
    if (versions.length == 3) {
      try {
        major = Integer.parseInt(versions[0]);
        minor = Integer.parseInt(versions[1]);
        patch = Integer.parseInt(versions[2]);
      } catch (final NumberFormatException ex) {
        LOG.error("Fehler beim Parsen der Version {}.", versionsstring, ex);
        major = 0;
        minor = 0;
        patch = 0;
      }
    }
  }

  /**
   * Nimmt ein Objekt vom Typ Version an und vergleicht ihn mit sich selbst
   *
   * @param versionzwei Versionsobjekt welches zum vergleich rangezogen werden soll
   * @return 1 Version a ist größer, 0 Versionen sind gleich oder -1 Version a ist kleiner
   */
  public int compare(final Version versionzwei) {
    if (toNumber() < versionzwei.toNumber()) {
      return 1;
    } else if (toNumber() == versionzwei.toNumber()) {
      return 0;
    } else {
      return -1;
    }
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getPatch() {
    return patch;
  }

  public void setMajor(final int major) {
    this.major = major;
  }

  public void setMinor(final int minor) {
    this.minor = minor;
  }

  public void setPatch(final int patch) {
    this.patch = patch;
  }

  /**
   * Gibt die Version als gewichtete Zahl zurück.
   *
   * @return gewichtete Zahl als Integer
   */
  public int toNumber() {
    return major * 100 + minor * 10 + patch;
  }

  /**
   * Gibt die Version als String zurück
   *
   * @return String mit der Version
   */
  @Override
  public String toString() {
    return String.format("%d.%d.%d", major, minor, patch);
  }
}
