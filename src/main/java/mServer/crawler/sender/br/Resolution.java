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
   * <li>HD = 1280 width</li>
   * <li>Premium = 969 width</li>
   * <li>Large = 640 width</li>
   * <li>Standard = 512 width</li>
   * <li>Mobile = 480 width</li>
   * <li>Mobile_S = 320 width</li>
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
      return Resolution.HD;
    }
    if (profileName.endsWith("Large")) {
      return Resolution.NORMAL;
    }
    if (profileName.endsWith("Standard")) {
      return Resolution.SMALL;
    }
    if (profileName.endsWith("Mobile")) {
      return Resolution.SMALL;
    }

    return Resolution.VERY_SMALL;

  }

  /**
   * The following width size limits are relevant:<br>
   * <ul>
   * <li>HD = >= 969 width</li>
   * <li>Normal = >= 640 width</li>
   * <li>Small = >= 512 width</li>
   * <li>Very Small = < 512 width</li>
   * </ul>
   *
   * @param width
   * @return
   */
  public static Resolution getResolutionFromWidth(final int width) {
    if (width >= 969) {
      return Resolution.HD;
    }
    if (width >= 640) {
      return Resolution.NORMAL;
    }
    if (width >= 512) {
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


  static Resolution getResoultionByResolutionSize(final int searchedResolutionSize)
      throws NoSuchElementException {
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
