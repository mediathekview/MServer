package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.ImportFilmlistConfiguration;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.testhelper.FileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.fail;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;


public class CrawlerManagerLivestreamTest implements MessageListener {

  private static final Logger LOG = LogManager.getLogger(CrawlerManagerLivestreamTest.class);
  private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_TEST_%d";
  private static Path testFileFolderPath;

  static Stream<Arguments> getTestArgumentForFilmlistsInDifferentFormats() {
    return Stream.of(
        arguments(FilmlistFormats.JSON, "filmlists/TestFilmlistNewJson.json", "filmlists/livestream/live-streams.json", 3, 50),
        arguments(FilmlistFormats.JSON_COMPRESSED_XZ, "filmlists/TestFilmlistNewJson.json.xz", "filmlists/livestream/live-streams.json.xz", 3, 50),
        arguments(FilmlistFormats.JSON_COMPRESSED_BZIP, "filmlists/TestFilmlistNewJson.json.bz", "filmlists/livestream/live-streams.json.bz", 3, 50),
        arguments(FilmlistFormats.JSON_COMPRESSED_GZIP, "filmlists/TestFilmlistNewJson.json.gz", "filmlists/livestream/live-streams.json.gz", 3, 50),
        arguments(FilmlistFormats.OLD_JSON, "filmlists/TestFilmlist.json", "filmlists/livestream/live-streams_old.json", 3, 50),
        arguments(FilmlistFormats.OLD_JSON_COMPRESSED_XZ, "filmlists/TestFilmlist.json.xz", "filmlists/livestream/live-streams_old.json.xz", 3, 50),
        arguments(FilmlistFormats.OLD_JSON_COMPRESSED_BZIP, "filmlists/TestFilmlist.json.bz", "filmlists/livestream/live-streams_old.json.bz", 3, 50),
        arguments(FilmlistFormats.OLD_JSON_COMPRESSED_GZIP, "filmlists/TestFilmlist.json.gz", "filmlists/livestream/live-streams_old.json.gz", 3, 50),
        arguments(FilmlistFormats.JSON, "filmlists/livestream/live-streams.json", "filmlists/livestream/live-streams.json", 47, 47)
    );
  }
  
  @ParameterizedTest
  @Execution(ExecutionMode.SAME_THREAD)
  @MethodSource("getTestArgumentForFilmlistsInDifferentFormats")
  public void testSaveAndImport(final FilmlistFormats format, final String filmlistPath,final String livestreamPath,  final int expectedInitialSize, final int expectedAfterImport) {
    CrawlerManager crawlerManagerForEachRun = createEmptyCrawlerManager();
    final Path filmListFilePath = FileReader.getPath(filmlistPath);
    final Path livestreamFilmListFilePath = FileReader.getPath(livestreamPath);
    crawlerManagerForEachRun.addMessageListener(this);
    crawlerManagerForEachRun.importFilmlist(new ImportFilmlistConfiguration(true, filmListFilePath.toAbsolutePath().toString(), format, false, false));
    //    
    assertThat(crawlerManagerForEachRun.getFilmlist().getFilms()).hasSize(expectedInitialSize);
    //
    crawlerManagerForEachRun.importLivestreamFilmlist(format, livestreamFilmListFilePath.toAbsolutePath().toString());
    //
    assertThat(crawlerManagerForEachRun.getFilmlist().getFilms()).hasSize(expectedAfterImport);
    //
    crawlerManagerForEachRun.saveFilmlist(testFileFolderPath.resolve(filmlistPath), format);
    //
    assertThat(testFileFolderPath.resolve(filmlistPath)).exists();
  }

  public CrawlerManager createEmptyCrawlerManager() {
    return new CrawlerManager(new MServerConfigManager(MServerConfigManager.DEFAULT_CONFIG_FILE));
  }

  @AfterAll
  public static void deleteTempFiles() throws IOException {
    Files.walk(testFileFolderPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeAll
  public static void initTestData() throws Exception {
    testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMP_FOLDER_NAME_PATTERN));
    Files.createDirectory(testFileFolderPath.resolve("filmlists"));
  }

  private static String formatWithDate(final String aPattern) {
    return String.format(aPattern, new Date().getTime());
  }

  @Override
  public void consumeMessage(final Message aMessage, final Object... aParameters) {
    if (MessageTypes.FATAL_ERROR.equals(aMessage.getMessageType())) {
      fail(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters));
    } else {
      LOG.info(
          String.format(
              "%s: %s",
              aMessage.getMessageType().name(),
              String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters)));
    }
  }

}
