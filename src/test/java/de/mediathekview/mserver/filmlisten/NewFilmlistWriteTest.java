package de.mediathekview.mserver.filmlisten;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import de.mediathekview.mserver.daten.Filmlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NewFilmlistWriteTest {
  private static Filmlist testData;

  @TempDir
  static Path testFileFolderPath;

  private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();

  private static Stream<Arguments> createReadTestArguments() {
    return Stream.of(
        Arguments.of("TestFilmlistNewJson.json", FilmlistFormats.JSON),
        Arguments.of("TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ),
        Arguments.of("TestFilmlistNewJson.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP),
        Arguments.of("TestFilmlistNewJson.json.bz", FilmlistFormats.JSON_COMPRESSED_BZIP),
        Arguments.of("TestFilmlist.json", FilmlistFormats.OLD_JSON),
        Arguments.of("TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ),
        Arguments.of("TestFilmlist.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP),
        Arguments.of("TestFilmlist.json.bz", FilmlistFormats.OLD_JSON_COMPRESSED_BZIP)
    );
  }

  @BeforeAll
  static void initTestData() throws IOException {
    testData = FilmlistTestData.getInstance().createTestdataNewFormat();
  }

  @ParameterizedTest
  @MethodSource("createReadTestArguments")
  void testWrite(String jsonName, FilmlistFormats format) {
    final Path testFilePath = testFileFolderPath.resolve(jsonName);
    System.out.println(testFilePath.toString());
    filmlistManager.save(format, testData, testFilePath);

    if (format.getFileExtension().contains("xz")) {
      assertThat(Files.exists(testFilePath.resolveSibling(testFilePath.getFileName().toString()))).isTrue();
    } else {
      assertThat(Files.exists(testFilePath)).isTrue();
    }
  }

}
