package de.mediathekview.mserver.base.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LanguageCodeUtilsTest {

  @ParameterizedTest
  @CsvSource({
    // arte liefert zweistellig
    "de, deu",
    "fr, fra",
    "es, spa",
    "it, ita",
    "pl, pol",
    // die ARD dreistellig
    "deu, deu",
    "eng, eng",
    "fra, fra",
    // Grossschreibung und Regionsangaben duerfen nichts aendern
    "DE, deu",
    "de-DE, deu",
    "spa-ES, spa",
    "EN, eng"
  })
  void knownCodesBecomeTheThreeLetterForm(final String input, final String expected) {
    assertThat(LanguageCodeUtils.normalize(input), equalTo(Optional.of(expected)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "ov", // ARDs Marker fuer die Originalfassung
        "und", // arte bei Code "VO" - unbestimmt
        "mul", // arte bei Code "VOEU" - mehrsprachig
        "mis",
        "zxx",
        "OV",
        "UND"
      })
  void placeholdersNameNoLanguage(final String input) {
    assertThat(LanguageCodeUtils.normalize(input), equalTo(Optional.empty()));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "x", "abcd", "12", "123", "d-e"})
  void unusableValuesYieldNothing(final String input) {
    assertThat(LanguageCodeUtils.normalize(input), equalTo(Optional.empty()));
  }
}
