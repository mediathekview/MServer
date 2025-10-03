package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mserver.base.messages.Message;
import de.mediathekview.mserver.base.messages.MessageTypes;
import de.mediathekview.mserver.base.messages.MessageUtil;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.ImportFilmlistConfiguration;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.testhelper.FileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class CrawlerManagerTest implements MessageListener {

  private Logger LOG;
  private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_TEST_%d";
  private static Path testFileFolderPath;

  private final CrawlerManager CRAWLER_MANAGER;
  private final String filmlistPath;
  private final FilmlistFormats format;
  private final int expectedSize;

  public CrawlerManagerTest(final String aFilmlistPath, final FilmlistFormats aFormat, final int aExpectedSize) {
    filmlistPath = aFilmlistPath;
    expectedSize = aExpectedSize;
    format = aFormat;
    CRAWLER_MANAGER = new CrawlerManager(new MServerConfigManager(MServerConfigManager.DEFAULT_CONFIG_FILE));
    LOG = LogManager.getLogger(CrawlerManagerTest.class);
  }

  @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
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

  @AfterClass
  public static void deleteTempFiles() throws IOException {
    Files.walk(testFileFolderPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeClass
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
      Assert.fail(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters));
    } else {
      LOG.info(
          String.format(
              "%s: %s",
              aMessage.getMessageType().name(),
              String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters)));
    }
  }

  @Test
  public void testSaveAndImport() {
    final Path filmListFilePath = FileReader.getPath(filmlistPath);
    synchronized (CRAWLER_MANAGER) {
      CRAWLER_MANAGER.addMessageListener(this);
      CRAWLER_MANAGER.importFilmlist(new ImportFilmlistConfiguration(true, filmListFilePath.toAbsolutePath().toString(), format, false, false));
      assertThat(CRAWLER_MANAGER.getFilmlist().getFilms()).hasSize(expectedSize);
      CRAWLER_MANAGER.saveFilmlist(testFileFolderPath.resolve(filmlistPath), format);
      assertThat(testFileFolderPath.resolve(filmlistPath)).exists();
    }
  }
}
