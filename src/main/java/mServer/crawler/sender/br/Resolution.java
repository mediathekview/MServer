package mServer.crawler.sender.br;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public enum Resolution {
  HD(3, "HD"), NORMAL(2, "Normal"), SMALL(1, "Klein"), VERY_SMALL(0, "Sehr klein");

  private enum CountingDirection {
    HIGHER(+1), LOWER(-1);

    int direction;

    private CountingDirection(final int direction) {
      this.direction = direction;
    }

    public int getDirectionValue() {
      return direction;
    }
  }

  /**
   * The bigger the index the better the quality.
   */
  private final int resolutionSize;

  private final String description;

  private Resolution(final int resolutionSize, final String description) {
    this.resolutionSize = resolutionSize;
    this.description = description;
  }

  public static List<Resolution> getFromBestToLowest() {
    return Arrays.asList(Resolution.values()).stream()
        .sorted(Comparator.comparing(Resolution::getResolutionSize).reversed())
        .collect(Collectors.toList());
  }

  public static Resolution getHighestResolution() {
    return Resolution.HD;
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
   * Derzeit sind folgende ARD AudioVideo Ordinals bekannt:<br>
   * <ul>
   * <li>HD = 1280 width x 720 height</li>
   * <li>Premium = 969 width x 540 height</li>
   * <li>Large = 640 width x 360 height</li>
   * <li>Standard = 512 width x 288 height</li>
   * <li>Mobile = 480 width x 270 height</li>
   * <li>Mobile_S = 320 width x 180 height</li>
   * </ul>
   *
   * @param profileName
   * @return
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

  /**
   * The following width size limits are relevant:<br>
   * <ul>
   * <li>HD = >= 1280 width</li>
   * <li>Normal = >= 969 width</li>
   * <li>Small = >= 640 width</li>
   * <li>Very Small = < 640 width</li>
   * </ul>
   *
   * @param profileName
   * @return
   */
  public static Resolution getResolutionFromWidth(final int width) {
    if (width >= 1280) {
      return Resolution.HD;
    }
    if (width >= 969) {
      return Resolution.NORMAL;
    }
    if (width >= 640) {
      return Resolution.SMALL;
    }
    return Resolution.VERY_SMALL;
  }
  
  static Resolution getNextResolutionByDirection(final Resolution startingResolution,
      final CountingDirection direction) {
    try {
      return getResoultionByResolutionSize(
          startingResolution.getResolutionSize() + direction.getDirectionValue());
    } catch (final NoSuchElementException nsee) {
      return startingResolution;
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
