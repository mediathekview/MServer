package de.mediathekview.mserver.filmlisten;

import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.mediathekview.mserver.daten.Filmlist;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NewFilmlistReadTest {

  private static final FilmlistManager filmlistManager = FilmlistManager.getInstance();
  private static Filmlist referenceData;

  @BeforeAll
  static void intializeReferenceData() throws MalformedURLException {
    referenceData = FilmlistTestData.getInstance().createTestdataNewFormat();
  }

  @ParameterizedTest
  @MethodSource("createReadTestArguments")
  void filmListReadTest(String filename, FilmlistFormats filmlistFormats)
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    final Path testFilePath = new File(classLoader.getResource(filename).getFile()).toPath();

    Optional<Filmlist> classUnderTest = filmlistManager.importList(filmlistFormats, testFilePath);

    assertThat(classUnderTest)
        .isNotEmpty()
        .get().returns(referenceData.getFilms().size(),
        from(Filmlist::getFilms).andThen(ConcurrentMap::size));
  }

  private static Stream<Arguments> createReadTestArguments() {
    return Stream.of(
        Arguments.of("mlib/TestFilmlistNewJson.json", FilmlistFormats.JSON),
        Arguments.of("mlib/TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ),
        Arguments.of("mlib/TestFilmlistNewJson.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP),
        Arguments.of("mlib/TestFilmlistNewJson.json.bz", FilmlistFormats.JSON_COMPRESSED_BZIP),
        Arguments.of("mlib/TestFilmlist.json", FilmlistFormats.OLD_JSON),
        Arguments.of("mlib/TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ),
        Arguments.of("mlib/TestFilmlist.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP),
        Arguments.of("mlib/TestFilmlist.json.bz", FilmlistFormats.OLD_JSON_COMPRESSED_BZIP)
    );
  }

}
