/*
 * ResolutionTest.java
 *
 * Projekt    : MLib
 * erstellt am: 18.09.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.daten;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ResolutionTest {

  @ParameterizedTest
  @MethodSource("generateRaisedExpectedResolutionPair")
  void testCompleteNextHigherResolutions(Pair<Resolution, Resolution> inputAndExpectedResolutionPair) {
    Resolution inputResolution = inputAndExpectedResolutionPair.getLeft();
    Resolution expectedResolution = inputAndExpectedResolutionPair.getRight();

    assertThat(Resolution.getNextHigherResolution(inputResolution)).isEqualTo(expectedResolution);
  }

  @ParameterizedTest
  @MethodSource("generateReducedExpectedResolutionPair")
  void testCompleteNextLowerResolutions(Pair<Resolution, Resolution> inputAndExpectedResolutionPair) {
    Resolution inputResolution = inputAndExpectedResolutionPair.getLeft();
    Resolution expectedResolution = inputAndExpectedResolutionPair.getRight();

    assertThat(Resolution.getNextLowerResolution(inputResolution)).isEqualTo(expectedResolution);
  }

  @Test
  void testResolutionTextVerySmall() {
    assertThat(Resolution.VERY_SMALL.getDescription()).isEqualTo("Sehr klein");
  }

  @Test
  void testResolutionTextSmall() {
    assertThat(Resolution.SMALL.getDescription()).isEqualTo("Klein");
  }

  @Test
  void testResolutionTextNormal() {
    assertThat(Resolution.NORMAL.getDescription()).isEqualTo("Normal");
  }

  @Test
  void testResolutionTextHd() {
    assertThat(Resolution.HD.getDescription()).isEqualTo("HD");
  }

  @Test
  void testGetReversedListOfResoltions() {
    final List<Resolution> descendingList = Resolution.getFromBestToLowest();

    assertThat(descendingList)
        .hasSize(6)
        .containsSequence(Resolution.UHD,
            Resolution.WQHD,
            Resolution.HD,
            Resolution.NORMAL,
            Resolution.SMALL,
            Resolution.VERY_SMALL);

  }

  @Test
  void testGetHighestResolution() {
    assertThat(Resolution.getHighestResolution()).isEqualTo(Resolution.UHD);
  }

  @Test
  void testGetLowestResolution() {
    assertThat(Resolution.getLowestResolution()).isEqualTo(Resolution.VERY_SMALL);
  }

  @Test
  void testGetUnknownResoultionByResolutionSize() {
    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(() -> { Resolution.getResoultionByResolutionSize(42); })
        .withMessage("Resolution with ResolutionIndex 42 not found");
  }

  private static List<Pair<Resolution, Resolution>> generateRaisedExpectedResolutionPair() {
    return List.of(
            Pair.of(Resolution.VERY_SMALL, Resolution.SMALL),
            Pair.of(Resolution.SMALL, Resolution.NORMAL),
            Pair.of(Resolution.NORMAL, Resolution.HD),
            Pair.of(Resolution.HD, Resolution.WQHD),
            Pair.of(Resolution.WQHD, Resolution.UHD),
            Pair.of(Resolution.UHD, Resolution.UHD)
    );
  }

  private static List<Pair<Resolution, Resolution>> generateReducedExpectedResolutionPair() {
    return List.of(
            Pair.of(Resolution.UHD, Resolution.WQHD),
            Pair.of(Resolution.WQHD, Resolution.HD),
            Pair.of(Resolution.HD, Resolution.NORMAL),
            Pair.of(Resolution.NORMAL, Resolution.SMALL),
            Pair.of(Resolution.SMALL, Resolution.VERY_SMALL),
            Pair.of(Resolution.VERY_SMALL, Resolution.VERY_SMALL)
    );
  }
}
