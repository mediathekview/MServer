package de.mediathekview.mserver.daten;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public enum Resolution {
  UHD(5, "UHD"),
  WQHD(4, "WQHD"),
  HD(3, "HD"),
  NORMAL(2, "Normal"),
  SMALL(1, "Klein"),
  VERY_SMALL(0, "Sehr klein");

  Resolution(final int resolutionSize, final String description) {
    this.resolutionSize = resolutionSize;
    this.description = description;
  }

  /*
   * The bigger the index the better the quality.
   */
  private final int resolutionSize;

  private final String description;

  public static Resolution getHighestResolution() {
    return Resolution.UHD;
  }

  public static List<Resolution> getFromBestToLowest() {
    return Arrays.stream(Resolution.values())
        .sorted(Comparator.comparing(Resolution::getResolutionSize).reversed())
        .toList();
  }

  /**
   * Derzeit sind folgende ARD AudioVideo Ordinals bekannt:<br>
   *
   * <ul>
   *   <LI>HD = 1280 width x 720 height
   *   <LI>Premium = 969 width x 540 height
   *   <LI>Large = 640 width x 360 height
   *   <LI>Standard = 512 width x 288 height
   *   <LI>Mobile = 480 width x 270 height
   *   <LI>Mobile_S = 320 width x 180 height
   * </ul>
   *
   * @param profileName Angabe des Profilenamens per String.
   * @return Gibt als Enum die entsprechende Enumeration zurück. Oder per Default VERY_SMALL.
   */
  public static Resolution getResolutionFromArdAudioVideoOrdinalsByProfileName(
      final String profileName) {
    if (profileName.endsWith("HD")) {
      return Resolution.HD;
    }
    if (profileName.endsWith("Premium")) {
      return Resolution.NORMAL;
    }
    if (profileName.endsWith("Large")) {
      return Resolution.SMALL;
    }
    if (profileName.endsWith("Standard")) {
      return Resolution.VERY_SMALL;
    }
    if (profileName.endsWith("Mobile")) {
      return Resolution.VERY_SMALL;
    }
    if (profileName.endsWith("Mobile_S")) {
      return Resolution.VERY_SMALL;
    }

    return Resolution.VERY_SMALL;
  }

  public static Resolution getLowestResolution() {
    return Resolution.VERY_SMALL;
  }

  public static Resolution getNextHigherResolution(final Resolution startingResolution) {
    return getNextResolutionByDirection(startingResolution, CountingDirection.HIGHER);
  }

  public static Resolution getNextLowerResolution(final Resolution startingResolution) {
    return getNextResolutionByDirection(startingResolution, CountingDirection.LOWER);
  }

  /**
   * The following width size limits are relevant:<br>
   *
   * <UL>
   *   <li>UHD = &gt;= 2160 width
   *   <li>WQHD = &gt;= 1440 width
   *   <li>HD = &gt;= 1280 width
   *   <li>Normal = &gt;= 969 width
   *   <li>Small = &gt;= 640 width
   *   <li>Very Small = &lt; 640 width
   * </UL>
   *
   * @param width Try to get an Resolution based on the horizontal width of the screen Resolution
   * @return returns the Resolution best working for the searched Resolution.
   */
  public static Resolution getResolutionFromWidth(final int width) {
    if (width >= 2160) {
      return Resolution.UHD;
    }
    if (width >= 1440) {
      return Resolution.WQHD;
    }
    if (width >= 1280) {
      return Resolution.HD;
    }
    if (width >= 720) {
      return Resolution.NORMAL;
    }
    if (width >= 500) {
      return Resolution.SMALL;
    }
    return Resolution.VERY_SMALL;
  }

  static Resolution getNextResolutionByDirection(
      final Resolution startingResolution, final CountingDirection direction) {
    try {
      return getResoultionByResolutionSize(
          startingResolution.getResolutionSize() + direction.getDirectionValue());
    } catch (final NoSuchElementException nsee) {
      return startingResolution;
    }
  }

  private enum CountingDirection {
    HIGHER(+1),
    LOWER(-1);

    int direction;

    CountingDirection(final int direction) {
      this.direction = direction;
    }

    public int getDirectionValue() {
      return direction;
    }
  }

  static Resolution getResoultionByResolutionSize(final int searchedResolutionSize) {
    for (final Resolution currentResolution : Resolution.values()) {
      if (searchedResolutionSize == currentResolution.getResolutionSize()) {
        return currentResolution;
      }
    }

    throw new NoSuchElementException(
        String.format("Resolution with ResolutionIndex %d not found", searchedResolutionSize));
  }

  public String getDescription() {
    return description;
  }

  public int getResolutionSize() {
    return resolutionSize;
  }
}
