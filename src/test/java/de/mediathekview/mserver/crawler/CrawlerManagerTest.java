package de.mediathekview.mserver.crawler;

import de.mediathekview.mserver.filmlisten.FilmlistFormats;
import de.mediathekview.mserver.base.messages.Message;
import de.mediathekview.mserver.base.messages.MessageTypes;
import de.mediathekview.mserver.base.messages.MessageUtil;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.ImportFilmlistConfiguration;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.testhelper.FileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class CrawlerManagerTest implements MessageListener {

  private Logger logger;
  private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_TEST_%d";
  private static Path testFileFolderPath;

  private CrawlerManager crawlerManager;
  private String filmlistPath;
  private FilmlistFormats format;
  private int expectedSize;

  public void initCrawlerManagerTest(final String aFilmlistPath, final FilmlistFormats aFormat, final int aExpectedSize) {
    filmlistPath = aFilmlistPath;
    expectedSize = aExpectedSize;
    format = aFormat;
    crawlerManager = new CrawlerManager(new MServerConfigManager(MServerConfigManager.DEFAULT_CONFIG_FILE));
    logger = LogManager.getLogger(CrawlerManagerTest.class);
  }

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"filmlists/TestFilmlistNewJson.json", FilmlistFormats.JSON,3},
          {"filmlists/TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED_XZ, 3},
          {"filmlists/TestFilmlistNewJson.json.bz", FilmlistFormats.JSON_COMPRESSED_BZIP, 3},
          {"filmlists/TestFilmlistNewJson.json.gz", FilmlistFormats.JSON_COMPRESSED_GZIP, 3},
          {"filmlists/TestFilmlist.json", FilmlistFormats.OLD_JSON, 3},
          {"filmlists/TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED_XZ, 3},
          {"filmlists/TestFilmlist.json.bz", FilmlistFormats.OLD_JSON_COMPRESSED_BZIP, 3},
          {"filmlists/TestFilmlist.json.gz", FilmlistFormats.OLD_JSON_COMPRESSED_GZIP, 3}
        });
  }

  @AfterAll
  static void deleteTempFiles() throws IOException {
    Files.walk(testFileFolderPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeAll
  static void initTestData() throws Exception {
    testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMP_FOLDER_NAME_PATTERN));
    Files.createDirectory(testFileFolderPath.resolve("filmlists"));
  }

  private static String formatWithDate(final String aPattern) {
    return String.format(aPattern, new Date().getTime());
  }

  @Override
  public void consumeMessage(final Message aMessage, final Object... aParameters) {
    if (MessageTypes.FATAL_ERROR.equals(aMessage.getMessageType())) {
      Assertions.fail(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters));
    } else {
      logger.info(
          String.format(
              "%s: %s",
              aMessage.getMessageType().name(),
              String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters)));
    }
  }

  @MethodSource("data")
  @ParameterizedTest(name = "Test {index} Filmlist for {0} with {1}")
  void testSaveAndImport(final String aFilmlistPath, final FilmlistFormats aFormat, final int aExpectedSize) {
    initCrawlerManagerTest(aFilmlistPath, aFormat, aExpectedSize);
    final Path filmListFilePath = FileReader.getPath(filmlistPath);
    synchronized (crawlerManager) {
      crawlerManager.addMessageListener(this);
      crawlerManager.importFilmlist(new ImportFilmlistConfiguration(true, filmListFilePath.toAbsolutePath().toString(), format, false, false));
      assertThat(crawlerManager.getFilmlist().getFilms()).hasSize(expectedSize);
      crawlerManager.saveFilmlist(testFileFolderPath.resolve(filmlistPath), format);
      assertThat(testFileFolderPath.resolve(filmlistPath)).exists();
    }
  }
}
