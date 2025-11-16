package de.mediathekview.mserver.base.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TextCleanerTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Hello\u0009World", // Horizontal Tab
        "Hello\nWorld", // Line Feed
        "Hello\u000bWorld", // Vertical Tab
        "Hello\u000cWorld", // Form Feed
        "Hello\rWorld", // Carriage Return
        "Hello\u00a0World", // No Break Space
        "Hello<hr/> World", // Remove Tags
      })
  void testSingleReplaceCharsWillBeReplacedWithBlank(String textToConvert) {
        // given
        String expectedResult = "Hello World";

        // given when
        String result = TextCleaner.clean(textToConvert);

        // then
        assertThat(result).isEqualTo(expectedResult);

    }

}